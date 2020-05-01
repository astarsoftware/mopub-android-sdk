// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import android.app.Activity;

import com.mopub.common.privacy.MoPubIdentifier;
import com.mopub.common.privacy.MoPubIdentifierTest;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.common.util.test.support.TestDateAndTime;
import com.mopub.network.AdResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class AdReportTest {

    @Mock
    private ClientMetadata mockClientMetadata;
    @Mock
    private AdResponse mockAdResponse;

    private Activity context;
    public AdReport subject;

    @Before
    public void setup() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        TestDateAndTime.getInstance().setNow(new Date());
        MoPubIdentifierTest.writeAdvertisingInfoToSharedPreferences(context, true);
        when(mockClientMetadata.getMoPubIdentifier()).thenReturn(new MoPubIdentifier(context));
    }

    @After
    public void tearDown(){
        MoPubIdentifierTest.clearPreferences(context);
    }

    @Test
    public void getResponseString_shouldReturnAdResponseStringBody() {
        final String stringBody = "this is the ad response string body";
        when(mockAdResponse.getStringBody()).thenReturn(stringBody);

        subject = new AdReport("testAdunit", mockClientMetadata, mockAdResponse);

        assertThat(subject.getResponseString()).isEqualTo(stringBody);
    }

    @Test
    public void getResponseString_withNullStringBody_shouldReturnNull() {
        when(mockAdResponse.getStringBody()).thenReturn(null);

        subject = new AdReport("testAdunit", mockClientMetadata, mockAdResponse);

        assertThat(subject.getResponseString()).isNull();
    }

    @Test
    public void isAllowCustomClose_true() {
        when(mockAdResponse.allowCustomClose()).thenReturn(true);

        subject = new AdReport("testAdunit", mockClientMetadata, mockAdResponse);

        assertThat(subject.shouldAllowCustomClose()).isTrue();
    }

    @Test
    public void isAllowCustomClose_false() {
        when(mockAdResponse.allowCustomClose()).thenReturn(false);

        subject = new AdReport("testAdunit", mockClientMetadata, mockAdResponse);

        assertThat(subject.shouldAllowCustomClose()).isFalse();
    }
}
