// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.network;

import android.app.Activity;
import android.webkit.WebSettings;

import com.mopub.common.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({WebSettings.class})
@RunWith(SdkTestRunner.class)
public class NetworkingTest {
    private Activity context;

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        mockStatic(WebSettings.class);

        PowerMockito.when(WebSettings.getDefaultUserAgent(context)).thenReturn("some android user agent");
    }

    @After
    public void tearDown() {
        Networking.clearForTesting();
    }

    @Test
    public void getUserAgent_usesCachedUserAgent() {
        Networking.setUserAgentForTesting("some cached user agent");
        String userAgent = Networking.getUserAgent(context);

        assertThat(userAgent).isEqualTo("some cached user agent");
    }

    @Test
    public void getUserAgent_shouldIncludeAndroid() {
        String userAgent = Networking.getUserAgent(context);

        assertThat(userAgent).isEqualTo("some android user agent");
    }

    @Test
    public void getUserAgent_whenOnABackgroundThread_shouldReturnHttpAgent() throws InterruptedException {
        final String[] userAgent = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                userAgent[0] = Networking.getUserAgent(context);

                latch.countDown();
            }
        }.start();

        latch.await(500, TimeUnit.MILLISECONDS);
        // Robolectric's default http agent is null which gets rewritten to an empty String.
        assertThat(userAgent[0]).isEqualTo("");

    }

    @Test
    public void getCachedUserAgent_usesCachedUserAgent() {
        Networking.setUserAgentForTesting("some cached user agent");
        String userAgent = Networking.getCachedUserAgent();

        assertThat(userAgent).isEqualTo("some cached user agent");
    }
}
