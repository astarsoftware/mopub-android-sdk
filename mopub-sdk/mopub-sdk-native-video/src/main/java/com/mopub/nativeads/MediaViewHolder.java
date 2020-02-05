// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.nativeads;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import static com.mopub.common.logging.MoPubLog.SdkLogEvent.CUSTOM;

class MediaViewHolder {
    @Nullable View mainView;
    @Nullable MediaLayout mediaLayout;
    @Nullable TextView titleView;
    @Nullable TextView textView;
    @Nullable ImageView iconImageView;
    @Nullable TextView callToActionView;
    @Nullable ImageView privacyInformationIconImageView;
    @Nullable TextView sponsoredTextView;

    @VisibleForTesting
    static final MediaViewHolder EMPTY_MEDIA_VIEW_HOLDER = new MediaViewHolder();

    // Use fromViewBinder instead of a constructor
    private MediaViewHolder() {}

    @NonNull
    static MediaViewHolder fromViewBinder(@NonNull final View view,
            @NonNull final MediaViewBinder mediaViewBinder) {
        final MediaViewHolder mediaViewHolder = new MediaViewHolder();
        mediaViewHolder.mainView = view;
        try {
            mediaViewHolder.titleView = view.findViewById(mediaViewBinder.titleId);
            mediaViewHolder.textView = view.findViewById(mediaViewBinder.textId);
            mediaViewHolder.callToActionView = view.findViewById(mediaViewBinder.callToActionId);
            mediaViewHolder.mediaLayout = view.findViewById(mediaViewBinder.mediaLayoutId);
            mediaViewHolder.iconImageView = view.findViewById(mediaViewBinder.iconImageId);
            mediaViewHolder.privacyInformationIconImageView =
                    view.findViewById(mediaViewBinder.privacyInformationIconImageId);
            mediaViewHolder.sponsoredTextView = view.findViewById(mediaViewBinder.sponsoredTextId);
            return mediaViewHolder;
        } catch (ClassCastException exception) {
            MoPubLog.log(CUSTOM, "Could not cast from id in MediaViewBinder to expected View type",
                    exception);
            return EMPTY_MEDIA_VIEW_HOLDER;
        }
    }
}
