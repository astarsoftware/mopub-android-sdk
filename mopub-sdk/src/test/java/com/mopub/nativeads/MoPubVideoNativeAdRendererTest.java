// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.nativeads;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.network.MaxWidthImageLoader;
import com.mopub.network.MoPubRequestQueue;
import com.mopub.network.Networking;
import com.mopub.volley.toolbox.ImageLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class MoPubVideoNativeAdRendererTest {
    private MoPubVideoNativeAdRenderer subject;
    private VideoNativeAd videoNativeAd;
    @Mock private RelativeLayout relativeLayout;
    @Mock private ViewGroup viewGroup;
    private MediaViewBinder mediaViewBinder;
    @Mock private TextView titleView;
    @Mock private TextView textView;
    @Mock private TextView callToActionView;
    @Mock private MediaLayout mediaLayoutView;
    @Mock private ImageView iconImageView;
    @Mock private ImageView privacyInformationIconImageView;
    @Mock private TextView sponsoredView;
    @Mock private ImageView badView;
    @Mock private MoPubRequestQueue mockRequestQueue;
    @Mock private MaxWidthImageLoader mockImageLoader;
    @Mock private ImageLoader.ImageContainer mockImageContainer;
    @Mock private Context context;

    @Before
    public void setUp() throws Exception {
        Networking.setRequestQueueForTesting(mockRequestQueue);
        Networking.setImageLoaderForTesting(mockImageLoader);
        when(mockImageContainer.getBitmap()).thenReturn(mock(Bitmap.class));

        when(relativeLayout.getId()).thenReturn(View.generateViewId());

        videoNativeAd = new VideoNativeAd() {

            @Override
            public void onStateChanged(final boolean playWhenReady, final int playbackState) {
            }

            @Override
            public void onError(final Exception e) {
            }
        };
        videoNativeAd.setTitle("test title");
        videoNativeAd.setText("test text");
        videoNativeAd.setCallToAction("test call to action");
        videoNativeAd.setClickDestinationUrl("destinationUrl");
        videoNativeAd.setMainImageUrl("testUrl");
        videoNativeAd.setIconImageUrl("testUrl");
        videoNativeAd.setVastVideo("test video");
        videoNativeAd.setSponsored("sponsored");

        setViewIdInLayout(titleView, relativeLayout);
        setViewIdInLayout(textView, relativeLayout);
        setViewIdInLayout(callToActionView, relativeLayout);
        setViewIdInLayout(mediaLayoutView, relativeLayout);
        setViewIdInLayout(iconImageView, relativeLayout);
        setViewIdInLayout(privacyInformationIconImageView, relativeLayout);
        setViewIdInLayout(sponsoredView, relativeLayout);
        setViewIdInLayout(badView, relativeLayout);

        mediaViewBinder = new MediaViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(textView.getId())
                .callToActionId(callToActionView.getId())
                .mediaLayoutId(mediaLayoutView.getId())
                .iconImageId(iconImageView.getId())
                .privacyInformationIconImageId(privacyInformationIconImageView.getId())
                .sponsoredTextId(sponsoredView.getId())
                .build();

        when(context.getString(anyInt(), eq("sponsored"))).thenReturn("sponsored by string plus " +
                "sponsored");
        when(sponsoredView.getContext()).thenReturn(context);

        subject = new MoPubVideoNativeAdRenderer(mediaViewBinder);
    }

    private void setViewIdInLayout(View mockView, RelativeLayout mockLayout) {
        int id = View.generateViewId();
        when(mockView.getId()).thenReturn(id);
        when(mockLayout.findViewById(eq(id))).thenReturn(mockView);
    }

    @Test(expected = NullPointerException.class)
    public void createAdView_withNullContext_shouldThrowNPE() {
        subject.createAdView(null, viewGroup);
    }

    @Test(expected = NullPointerException.class)
    public void renderAdView_withNullView_shouldThrowNPE() {
        subject.renderAdView(null, videoNativeAd);
    }

    @Test(expected = NullPointerException.class)
    public void renderAdView_withNullNativeAd_shouldThrowNPE() {
        subject.renderAdView(relativeLayout, null);
    }

    @Rule public ExpectedException exception = ExpectedException.none();

    @Test
    public void renderAdView_withNullViewBinder_shouldThrowNPE() {
        subject = new MoPubVideoNativeAdRenderer(null);

        exception.expect(NullPointerException.class);
        subject.renderAdView(relativeLayout, videoNativeAd);
    }

    @Test
    public void renderAdView_shouldReturnPopulatedView() {
        subject.renderAdView(relativeLayout, videoNativeAd);

        verify(titleView).setText(eq("test title"));
        verify(textView).setText(eq("test text"));
        verify(callToActionView).setText(eq("test call to action"));
        verify(sponsoredView).setText("sponsored by string plus sponsored");
        verify(sponsoredView).setVisibility(View.VISIBLE);

        // not testing images due to testing complexity
    }

    @Test
    public void renderAdView_withFailedViewBinder_shouldNotWriteViews() {
        mediaViewBinder = new MediaViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(badView.getId())
                .callToActionId(callToActionView.getId())
                .mediaLayoutId(mediaLayoutView.getId())
                .iconImageId(iconImageView.getId())
                .build();

        subject = new MoPubVideoNativeAdRenderer(mediaViewBinder);
        subject.renderAdView(relativeLayout, videoNativeAd);

        verify(titleView, never()).setText(anyString());
        verify(textView, never()).setText(anyString());
        verify(callToActionView, never()).setText(anyString());
        verify(mediaLayoutView, times(2)).getId();
        verifyNoMoreInteractions(mediaLayoutView);
        verify(iconImageView, times(2)).getId();
        verifyNoMoreInteractions(iconImageView);
    }

    @Test
    public void renderAdView_withNoViewHolder_shouldCreateNativeViewHolder() {
        subject.renderAdView(relativeLayout, videoNativeAd);

        MediaViewHolder expectedViewHolder = MediaViewHolder.fromViewBinder
                (relativeLayout,
                mediaViewBinder);
        MediaViewHolder viewHolder = subject.mMediaViewHolderMap.get(relativeLayout);
        compareNativeViewHolders(expectedViewHolder, viewHolder);
    }

    @Test
    public void renderAdView_withNoSponsoredText_shouldSetViewInvisible() {
        videoNativeAd.setSponsored(null);
        subject.renderAdView(relativeLayout, videoNativeAd);

        verify(sponsoredView).setVisibility(View.INVISIBLE);
    }

    @Test
    public void getOrCreateNativeViewHolder_withViewHolder_shouldNotReCreateNativeViewHolder() {
        subject.renderAdView(relativeLayout, videoNativeAd);
        MediaViewHolder expectedViewHolder = subject.mMediaViewHolderMap.get(relativeLayout);
        subject.renderAdView(relativeLayout, videoNativeAd);

        MediaViewHolder viewHolder = subject.mMediaViewHolderMap.get(relativeLayout);
        assertThat(viewHolder).isEqualTo(expectedViewHolder);
    }

    static private void compareNativeViewHolders(final MediaViewHolder actualViewHolder,
            final MediaViewHolder expectedViewHolder) {
        assertThat(actualViewHolder.titleView).isEqualTo(expectedViewHolder.titleView);
        assertThat(actualViewHolder.textView).isEqualTo(expectedViewHolder.textView);
        assertThat(actualViewHolder.callToActionView).isEqualTo(expectedViewHolder.callToActionView);
        assertThat(actualViewHolder.mediaLayout).isEqualTo(expectedViewHolder.mediaLayout);
        assertThat(actualViewHolder.iconImageView).isEqualTo(expectedViewHolder.iconImageView);
        assertThat(actualViewHolder.privacyInformationIconImageView).isEqualTo(
                expectedViewHolder.privacyInformationIconImageView);
        assertThat(actualViewHolder.sponsoredTextView).isEqualTo(expectedViewHolder.sponsoredTextView);
    }

    @Test
    public void supports_withCorrectInstanceOfBaseNativeAd_shouldReturnTrue() throws Exception {
        assertThat(subject.supports(new VideoNativeAd() {
            @Override
            public void onStateChanged(final boolean playWhenReady, final int playbackState) {
            }

            @Override
            public void onError(final Exception e) {
            }
        })).isTrue();
        assertThat(subject.supports(
                mock(MoPubCustomEventVideoNative.MoPubVideoNativeAd.class))).isTrue();
        assertThat(subject.supports(mock(BaseNativeAd.class))).isFalse();
        assertThat(subject.supports(mock(MoPubCustomEventNative.MoPubStaticNativeAd.class)))
                .isFalse();
    }
}
