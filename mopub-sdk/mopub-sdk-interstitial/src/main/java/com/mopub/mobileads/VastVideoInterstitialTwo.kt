// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.mopub.common.CacheService
import com.mopub.common.CreativeOrientation
import com.mopub.common.DataKeys.*
import com.mopub.common.VisibleForTesting
import com.mopub.common.logging.MoPubLog.AdapterLogEvent.*
import com.mopub.common.logging.MoPubLog.SdkLogEvent.ERROR_WITH_THROWABLE
import com.mopub.common.logging.MoPubLog.log
import com.mopub.common.util.Json
import com.mopub.mobileads.MoPubErrorCode.VIDEO_CACHE_ERROR
import com.mopub.mobileads.MoPubErrorCode.VIDEO_DOWNLOAD_ERROR
import com.mopub.mobileads.VastManager.VastManagerListener
import com.mopub.mobileads.factories.VastManagerFactory
import org.json.JSONException
import org.json.JSONObject

open class VastVideoInterstitialTwo : ResponseBodyInterstitial(),
    VastManagerListener {

    @VisibleForTesting
    var vastResponse: String? = null
    @VisibleForTesting
    var vastManager: VastManager? = null
    @VisibleForTesting
    var vastVideoConfig: VastVideoConfigTwo? = null

    private var customEventInterstitialListener: CustomEventInterstitialListener? = null
    private var videoTrackers: JSONObject? = null
    private var externalViewabilityTrackers: Map<String, String>? = null
    private var orientation: CreativeOrientation? = null
    private var enableClickExperiment: Boolean = false

    override fun extractExtras(serverExtras: Map<String, String>) {
        vastResponse = serverExtras[HTML_RESPONSE_BODY_KEY]
        orientation =
            CreativeOrientation.fromString(serverExtras[CREATIVE_ORIENTATION_KEY])
        enableClickExperiment = serverExtras[VAST_CLICK_EXP_ENABLED_KEY]?.toBoolean() ?: false

        externalViewabilityTrackers = serverExtras[EXTERNAL_VIDEO_VIEWABILITY_TRACKERS_KEY]?.let {
            try {
                Json.jsonStringToMap(it)
            } catch (e: JSONException) {
                log(CUSTOM, "Failed to parse video viewability trackers to JSON: $it")
                null
            }
        }

        videoTrackers = serverExtras[VIDEO_TRACKERS_KEY]?.takeIf {
            it.isNotEmpty()
        }?.let {
            try {
                JSONObject(it)
            } catch (e: JSONException) {
                log(ERROR_WITH_THROWABLE, "Failed to parse video trackers to JSON: $it", e)
                null
            }
        }
    }

    override fun preRenderHtml(listener: CustomEventInterstitialListener) {
        customEventInterstitialListener = listener
        if (!CacheService.initializeDiskCache(mContext)) {
            log(LOAD_FAILED, ADAPTER_NAME, VIDEO_CACHE_ERROR.intCode, VIDEO_CACHE_ERROR)
            listener.onInterstitialFailed(VIDEO_CACHE_ERROR)
            return
        }
        vastManager = VastManagerFactory.create(mContext).also {
            it.prepareVastVideoConfiguration(
                vastResponse, this,
                mAdReport.dspCreativeId, mContext
            )
        }
        log(LOAD_SUCCESS, ADAPTER_NAME)
    }

    override fun showInterstitial() {
        log(SHOW_ATTEMPTED, ADAPTER_NAME)
        MraidVideoPlayerActivity.startVast(
            mContext,
            vastVideoConfig,
            mBroadcastIdentifier,
            orientation
        )
    }

    override fun onInvalidate() {
        vastManager?.cancel()
        super.onInvalidate()
    }

    /*
     * VastManager.VastManagerListener implementation
     */
    override fun onVastVideoConfigurationPrepared(vastVideoConfig: VastVideoConfig?) {
        vastVideoConfig?.let {
            val gson = GsonBuilder()
                .registerTypeAdapterFactory(VastVideoConfig.VastVideoConfigTypeAdapterFactory())
                .create()
            val vvConfigJson = gson.toJson(it)
            gson.fromJson(vvConfigJson, VastVideoConfigTwo::class.java)
        }?.also {
            this.vastVideoConfig = it
            it.addVideoTrackers(videoTrackers)
            it.addExternalViewabilityTrackers(externalViewabilityTrackers)
            it.enableClickExperiment = enableClickExperiment
            customEventInterstitialListener?.onInterstitialLoaded()
        } ?: customEventInterstitialListener?.onInterstitialFailed(VIDEO_DOWNLOAD_ERROR)
    }

    companion object {
        val ADAPTER_NAME = VastVideoInterstitialTwo::class.java.simpleName
    }
}
