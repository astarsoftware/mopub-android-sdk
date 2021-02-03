// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.network

import android.app.Activity
import android.webkit.WebSettings

import com.mopub.common.test.support.SdkTestRunner

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import org.robolectric.Robolectric

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.fest.assertions.api.Assertions.assertThat
import org.powermock.api.mockito.PowerMockito.mockStatic

@PowerMockIgnore("org.mockito.*", "org.robolectric.*", "android.*", "org.json.*")
@PrepareForTest(WebSettings::class)
@RunWith(SdkTestRunner::class)
class NetworkingTest {
    private lateinit var context: Activity

    @get:Rule
    val rule = PowerMockRule()

    @Before
    fun setUp() {
        context = Robolectric.buildActivity(Activity::class.java).create().get()
        mockStatic(WebSettings::class.java)

        PowerMockito.`when`(WebSettings.getDefaultUserAgent(context))
            .thenReturn("some android user agent")
    }

    @After
    fun tearDown() {
        Networking.clearForTesting()
    }

    @Test
    fun getUserAgent_usesCachedUserAgent() {
        Networking.setUserAgentForTesting("some cached user agent")
        val userAgent = Networking.getUserAgent(context)

        assertThat(userAgent).isEqualTo("some cached user agent")
    }

    @Test
    fun getUserAgent_shouldIncludeAndroid() {
        val userAgent = Networking.getUserAgent(context)

        assertThat(userAgent).isEqualTo("some android user agent")
    }

    @Test
    @Throws(InterruptedException::class)
    fun getUserAgent_whenOnABackgroundThread_shouldReturnHttpAgent() {
        val userAgent = arrayOfNulls<String>(1)
        val latch = CountDownLatch(1)
        object : Thread() {
            override fun run() {
                userAgent[0] = Networking.getUserAgent(context)
                latch.countDown()
            }
        }.start()

        latch.await(500, TimeUnit.MILLISECONDS)
        // Robolectric's default http agent is null which gets rewritten to an empty String.
        assertThat(userAgent[0]).isEqualTo("")
    }

    @Test
    fun getCachedUserAgent_usesCachedUserAgent() {
        Networking.setUserAgentForTesting("some cached user agent")
        val userAgent = Networking.cachedUserAgent

        assertThat(userAgent).isEqualTo("some cached user agent")
    }
}
