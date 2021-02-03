// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.network

import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import android.text.TextUtils
import android.webkit.WebSettings

import androidx.collection.LruCache

import com.mopub.common.Constants
import com.mopub.common.VisibleForTesting
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.SdkLogEvent.CUSTOM
import com.mopub.common.util.DeviceUtils
import com.mopub.volley.toolbox.BasicNetwork
import com.mopub.volley.toolbox.DiskBasedCache
import com.mopub.volley.toolbox.HurlStack
import com.mopub.volley.toolbox.ImageLoader

import java.io.File

object Networking {
    @VisibleForTesting
    private const val CACHE_DIRECTORY_NAME = "mopub-volley-cache"
    private val DEFAULT_USER_AGENT: String

    init {
        var ua : String? = ""
        try {
            ua = System.getProperty("http.agent", "")
        } catch (e: SecurityException) {
            MoPubLog.log(CUSTOM, "Unable to get system user agent.")
        }
        DEFAULT_USER_AGENT = ua ?: ""
    }

    // These are volatile so that double-checked locking works.
    // See https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
    // for more information.
    @JvmStatic
    @Volatile
    var requestQueue: MoPubRequestQueue? = null
        private set
    @Volatile
    private var userAgent: String? = null
    @Volatile
    private var maxWidthImageLoader: MaxWidthImageLoader? = null
    private var urlRewriter : HurlStack.UrlRewriter? = null

    /**
     * Gets the previously cached WebView user agent. This returns the default userAgent if the
     * WebView user agent has not been initialized yet.
     *
     * @return Best-effort String WebView user agent.
     */
    @JvmStatic
    val cachedUserAgent: String
        get() = userAgent ?: DEFAULT_USER_AGENT

    /**
     * Retrieve the scheme that should be used for all urls.
     *
     * @return "https"
     */
    @JvmStatic
    val scheme = Constants.HTTPS

    // No synchronization done here since it's fine to create the same rewriter more than once.
    @JvmStatic
    fun getUrlRewriter() = urlRewriter ?: PlayServicesUrlRewriter().also {
        urlRewriter = it
    }

    @JvmStatic
    fun getRequestQueue(context: Context) =
        // Double-check locking to initialize.
        requestQueue ?: synchronized(Networking::class) {
            requestQueue ?: {
                val socketFactory = CustomSSLSocketFactory.getDefault(Constants.TEN_SECONDS_MILLIS)

                val userAgent = getUserAgent(context.applicationContext)
                val httpStack = RequestQueueHttpStack(userAgent, getUrlRewriter(), socketFactory)

                val network = BasicNetwork(httpStack)
                val volleyCacheDir = File(context.cacheDir.path + File.separator + CACHE_DIRECTORY_NAME)
                val cache = DiskBasedCache(
                    volleyCacheDir, DeviceUtils.diskCacheSizeBytes(
                        volleyCacheDir,
                        Constants.TEN_MB.toLong()
                    ).toInt()
                )

                MoPubRequestQueue(cache, network).also {
                    requestQueue = it
                    it.start()
                }
            }()
        }

    @JvmStatic
    fun getImageLoader(context: Context) =
        // Double-check locking to initialize.
        maxWidthImageLoader ?: synchronized(Networking::class) {
            maxWidthImageLoader ?: {
                val queue = getRequestQueue(context)
                val cacheSize = DeviceUtils.memoryCacheSizeBytes(context)
                val imageCache = object : LruCache<String, Bitmap>(cacheSize) {
                    override fun sizeOf(key: String, value: Bitmap): Int {
                        return value.rowBytes * value.height
                    }
                }

                MaxWidthImageLoader(queue, context, object : ImageLoader.ImageCache {
                    override fun getBitmap(key: String) = imageCache.get(key)
                    override fun putBitmap(key: String, bitmap: Bitmap) {
                        imageCache.put(key, bitmap)
                    }
                }).also {
                    maxWidthImageLoader = it
                }
            }()
        }

    /**
     * Caches and returns the WebView user agent to be used across all SDK requests. This is
     * important because advertisers expect the same user agent across all request, impression, and
     * click events.
     */
    @JvmStatic
    fun getUserAgent(context: Context): String {
        val volatileUserAgentCopy = userAgent
        if (!volatileUserAgentCopy.isNullOrEmpty()) {
            return volatileUserAgentCopy
        }

        // WebViews may only be instantiated on the UI thread. If anything goes
        // wrong with getting a user agent, use the system-specific user agent.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // Since we are not on the main thread, return the default user agent
            // for now. Defer to when this is run on the main thread to actually
            // set the user agent.
            return DEFAULT_USER_AGENT
        }

        // Some custom ROMs may fail to get a user agent. If that happens, return
        // the Android system user agent.
        var tempUserAgent = DEFAULT_USER_AGENT
        try {
            tempUserAgent = WebSettings.getDefaultUserAgent(context)
        } catch (e : Exception) {
            MoPubLog.log(CUSTOM,
                "Failed to get a user agent. Defaulting to the system user agent.")
        }

        userAgent = tempUserAgent
        return tempUserAgent
    }

    @VisibleForTesting
    @Synchronized
    @JvmStatic
    fun clearForTesting() {
        requestQueue = null
        maxWidthImageLoader = null
        userAgent = null
    }

    @VisibleForTesting
    @Synchronized
    @JvmStatic
    fun setRequestQueueForTesting(queue: MoPubRequestQueue?) {
        this.requestQueue = queue
    }

    @VisibleForTesting
    @Synchronized
    @JvmStatic
    fun setImageLoaderForTesting(imageLoader: MaxWidthImageLoader?) {
        maxWidthImageLoader = imageLoader
    }

    @VisibleForTesting
    @Synchronized
    @JvmStatic
    fun setUserAgentForTesting(userAgent: String?) {
        this.userAgent = userAgent
    }
}
