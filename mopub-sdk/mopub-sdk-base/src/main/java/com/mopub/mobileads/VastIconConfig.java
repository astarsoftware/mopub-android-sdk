// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mopub.common.Constants;
import com.mopub.common.MoPubBrowser;
import com.mopub.common.Preconditions;
import com.mopub.common.UrlAction;
import com.mopub.common.UrlHandler;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Intents;
import com.mopub.exceptions.IntentNotResolvableException;

import java.io.Serializable;
import java.util.List;

import static com.mopub.common.logging.MoPubLog.SdkLogEvent.CUSTOM;
import static com.mopub.network.TrackingRequest.makeVastTrackingHttpRequest;

/**
 * The data and event handlers for the icon displayed during a VAST 3.0 video.
 */
class VastIconConfig implements Serializable {
    private static final long serialVersionUID = 0L;

    @Expose @SerializedName(Constants.VAST_WIDTH)
    private final int mWidth;
    @Expose @SerializedName(Constants.VAST_HEIGHT)
    private final int mHeight;
    @Expose @SerializedName(Constants.VAST_SKIP_OFFSET_MS)
    private final int mOffsetMS;
    @Expose @SerializedName(Constants.VAST_DURATION_MS)
    @Nullable private final Integer mDurationMS;
    @Expose @SerializedName(Constants.VAST_RESOURCE)
    @NonNull private final VastResource mVastResource;
    @Expose @SerializedName(Constants.VAST_TRACKERS_CLICK)
    @NonNull private final List<VastTracker> mClickTrackingUris;
    @Expose @SerializedName(Constants.VAST_URL_CLICKTHROUGH)
    @Nullable private final String mClickThroughUri;
    @Expose @SerializedName(Constants.VAST_VIDEO_VIEWABILITY_TRACKER)
    @NonNull private final List<VastTracker> mViewTrackingUris;

    VastIconConfig(int width,
            int height,
            @Nullable Integer offsetMS,
            @Nullable Integer durationMS,
            @NonNull VastResource vastResource,
            @NonNull List<VastTracker> clickTrackingUris,
            @Nullable String clickThroughUri,
            @NonNull List<VastTracker> viewTrackingUris) {
        Preconditions.checkNotNull(vastResource);
        Preconditions.checkNotNull(clickTrackingUris);
        Preconditions.checkNotNull(viewTrackingUris);

        mWidth = width;
        mHeight = height;
        mOffsetMS = offsetMS == null ? 0 : offsetMS;
        mDurationMS = durationMS;
        mVastResource = vastResource;
        mClickTrackingUris = clickTrackingUris;
        mClickThroughUri = clickThroughUri;
        mViewTrackingUris = viewTrackingUris;
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    /**
     * The offset time into a video that the icon is displayed.
     *
     * @return the offset in milliseconds.
     */
    int getOffsetMS() {
        return mOffsetMS;
    }

    /**
     * The duration that the icon is displayed during a video.
     *
     * @return the duration in milliseconds.
     */
    @Nullable
    Integer getDurationMS() {
        return mDurationMS;
    }

    /**
     * The resource describing the icon's type.
     *
     * @return the resource.
     */
    @NonNull
    VastResource getVastResource() {
        return mVastResource;
    }

    @NonNull
    List<VastTracker> getClickTrackingUris() {
        return mClickTrackingUris;
    }

    @Nullable
    String getClickThroughUri() {
        return mClickThroughUri;
    }

    @NonNull
    List<VastTracker> getViewTrackingUris() {
        return mViewTrackingUris;
    }

    /**
     * Called when the icon is displayed during the video. Handles firing the impression trackers.
     *
     * @param context the context.
     * @param contentPlayHead the time into the video.
     * @param assetUri the uri of the video.
     */
    void handleImpression(@NonNull Context context, int contentPlayHead, @NonNull String assetUri) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(assetUri);

        makeVastTrackingHttpRequest(
                mViewTrackingUris,
                null,
                contentPlayHead,
                assetUri,
                context
        );
    }

    /**
     * Called when the icon is clicked. Handles forwarding the user to the click through uri.
     *
     * @param context                the context.
     * @param webViewClickThroughUri The click through uri for Javascript, HTML and IFrame resources
     *                               from the WebView
     */
    void handleClick(@NonNull final Context context, @Nullable String webViewClickThroughUri,
            @Nullable final String dspCreativeId) {
        Preconditions.checkNotNull(context);


        final String correctClickThroughUri = mVastResource.getCorrectClickThroughUrl(
                mClickThroughUri, webViewClickThroughUri);

        if (TextUtils.isEmpty(correctClickThroughUri)) {
            return;
        }

        new UrlHandler.Builder()
                .withSupportedUrlActions(
                        UrlAction.IGNORE_ABOUT_SCHEME,
                        UrlAction.OPEN_NATIVE_BROWSER,
                        UrlAction.OPEN_IN_APP_BROWSER)
                .withResultActions(new UrlHandler.ResultActions() {
                    @Override
                    public void urlHandlingSucceeded(@NonNull String url,
                            @NonNull UrlAction urlAction) {
                        if (urlAction == UrlAction.OPEN_IN_APP_BROWSER) {
                            Bundle bundle = new Bundle();
                            bundle.putString(MoPubBrowser.DESTINATION_URL_KEY, url);
                            if (!TextUtils.isEmpty(dspCreativeId)) {
                                bundle.putString(MoPubBrowser.DSP_CREATIVE_ID, dspCreativeId);
                            }
                            Intent intent = Intents.getStartActivityIntent(
                                    context, MoPubBrowser.class, bundle);
                            try {
                                Intents.startActivity(context, intent);
                            } catch (IntentNotResolvableException e) {
                                MoPubLog.log(CUSTOM, e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void urlHandlingFailed(@NonNull String url,
                            @NonNull UrlAction lastFailedUrlAction) {
                    }
                })
                .withoutMoPubBrowser()
                .build()
                .handleUrl(context, correctClickThroughUri);
    }
}
