// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mopub.common.Constants;
import com.mopub.common.Preconditions;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A Vast tracking URL with an "absolute" trigger threshold. The tracker should be triggered
 * after a fixed number of milliseconds have been played.
 */
public class VastAbsoluteProgressTracker extends VastTracker
        implements Comparable<VastAbsoluteProgressTracker>, Serializable {
    private static final long serialVersionUID = 0L;
    private static Pattern absolutePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}(.\\d{3})?");

    @Expose @SerializedName(Constants.VAST_TRACKER_TRACKING_MS)
    private final int mTrackingMilliseconds;

    public VastAbsoluteProgressTracker(@NonNull final MessageType messageType,
            @NonNull final String content, int trackingMilliseconds) {
        super(messageType, content);
        Preconditions.checkArgument(trackingMilliseconds >= 0);
        mTrackingMilliseconds = trackingMilliseconds;
    }

    public VastAbsoluteProgressTracker(@NonNull final String trackingUrl,
            int trackingMilliseconds) {
        this(MessageType.TRACKING_URL, trackingUrl, trackingMilliseconds);
    }

    public int getTrackingMilliseconds() {
        return mTrackingMilliseconds;
    }

    @Override
    public int compareTo(@NonNull final VastAbsoluteProgressTracker other) {
        int you = other.getTrackingMilliseconds();
        int me = getTrackingMilliseconds();

        return me - you;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%dms: %s", mTrackingMilliseconds, getContent());
    }

    public static boolean isAbsoluteTracker(String progressValue) {
        return !TextUtils.isEmpty(progressValue)
                && absolutePattern.matcher(progressValue).matches();
    }

    public static Integer parseAbsoluteOffset(@Nullable String progressValue) {
        if (progressValue == null) {
            return null;
        }

        final String[] split = progressValue.split(":");
        if (split.length != 3) {
            return null;
        }

        return Integer.parseInt(split[0]) * 60 * 60 * 1000 // Hours
                + Integer.parseInt(split[1]) * 60 * 1000 // Minutes
                + (int)(Float.parseFloat(split[2]) * 1000);
    }
}
