// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import androidx.annotation.NonNull;

import com.mopub.common.privacy.AdvertisingId;
import com.mopub.network.AdResponse;

import java.io.Serializable;
import java.util.Locale;

/**
 * A value class used for generating reports to send data back to MoPub
 */
public class AdReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    private final AdResponse mAdResponse;
    private final String mAdUnitId;
    private final String mSdkVersion;
    private final String mDeviceModel;
    private final Locale mDeviceLocale;
    private final AdvertisingId mAdvertisingId;

    public AdReport(@NonNull String adUnitId, @NonNull ClientMetadata clientMetadata, @NonNull AdResponse adResponse) {
        mAdUnitId = adUnitId;
        mSdkVersion = clientMetadata.getSdkVersion();
        mDeviceModel = clientMetadata.getDeviceModel();
        mDeviceLocale = clientMetadata.getDeviceLocale();
        mAdvertisingId = clientMetadata.getMoPubIdentifier().getAdvertisingInfo();
        mAdResponse = adResponse;
    }

    public String getResponseString() {
        return mAdResponse.getStringBody();
    }

    public String getDspCreativeId() {
        return mAdResponse.getDspCreativeId();
    }

    public boolean shouldAllowCustomClose() {
        return mAdResponse.allowCustomClose();
    }
}
