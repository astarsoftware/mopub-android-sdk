// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mopub.common.Constants;

public class VideoViewabilityTracker extends VastTracker {
    @Expose @SerializedName(Constants.VAST_TRACKER_PLAYTIME_MS)
    private final int mViewablePlaytimeMS;

    @Expose @SerializedName(Constants.VAST_TRACKER_PERCENT_VIEWABLE)
    private final int mPercentViewable;

    public VideoViewabilityTracker(final int viewablePlaytimeMS, final int percentViewable,
            @NonNull final String trackerUrl) {
        super(trackerUrl);
        mViewablePlaytimeMS = viewablePlaytimeMS;
        mPercentViewable = percentViewable;
    }

    public int getViewablePlaytimeMS() {
        return mViewablePlaytimeMS;
    }

    public int getPercentViewable() {
        return mPercentViewable;
    }
}
