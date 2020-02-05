// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads.test.support;

import android.content.Context;

import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.factories.MoPubViewFactory;

import static org.mockito.Mockito.mock;

public class TestMoPubViewFactory extends MoPubViewFactory {
    private final MoPubView mockMoPubView = mock(MoPubView.class);

    public static MoPubView getSingletonMock() {
        return getTestFactory().mockMoPubView;
    }

    private static TestMoPubViewFactory getTestFactory() {
        return (TestMoPubViewFactory) instance;
    }

    @Override
    protected MoPubView internalCreate(Context context) {
        return mockMoPubView;
    }
}
