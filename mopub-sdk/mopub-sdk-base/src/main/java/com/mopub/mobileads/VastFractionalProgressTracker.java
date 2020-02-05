// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mopub.common.Constants;
import com.mopub.common.Preconditions;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A Vast tracking URL with a "fractional" tracking threshold on the interval [0.0, 1.0].
 * The tracker should be triggered after the given fraction of the video has been played.
 */
public class VastFractionalProgressTracker extends VastTracker implements Comparable<VastFractionalProgressTracker>, Serializable {
    private static final long serialVersionUID = 0L;
    private static Pattern percentagePattern = Pattern.compile("((\\d{1,2})|(100))%");

    @Expose @SerializedName(Constants.VAST_TRACKER_TRACKING_FRACTION)
    private final float mFraction;

    public VastFractionalProgressTracker(@NonNull final MessageType messageType,
            @NonNull final String content, float trackingFraction) {
        super(messageType, content);
        Preconditions.checkArgument(trackingFraction >= 0);
        mFraction = trackingFraction;
    }

    public VastFractionalProgressTracker(@NonNull final String trackingUrl, float trackingFraction) {
        this(MessageType.TRACKING_URL, trackingUrl, trackingFraction);
    }

    public float trackingFraction() {
        return mFraction;
    }

    @Override
    public int compareTo(@NonNull final VastFractionalProgressTracker other) {
        float you = other.trackingFraction();
        float me = trackingFraction();

        return Double.compare(me, you);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%2f: %s", mFraction, getContent());
    }

    public static boolean isPercentageTracker(@NonNull final String progressValue) {
        Preconditions.checkNotNull(progressValue);

        return !TextUtils.isEmpty(progressValue)
                && percentagePattern.matcher(progressValue).matches();
    }

    public static int parsePercentageOffset(@NonNull final String progressValue, final int videoDuration) {
        Preconditions.checkNotNull(progressValue);

        final String percentage = progressValue.replace("%", "");
        return Math.round(videoDuration * Float.parseFloat(percentage) / 100f);
    }
}
