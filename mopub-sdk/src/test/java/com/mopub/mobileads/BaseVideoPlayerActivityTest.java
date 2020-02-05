// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.View;

import com.mopub.common.CreativeOrientation;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.common.util.Utils;
import com.mopub.common.util.UtilsTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;

import static com.mopub.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static com.mopub.mobileads.BaseVideoPlayerActivity.VIDEO_URL;
import static com.mopub.mobileads.BaseVideoPlayerActivity.startMraid;
import static com.mopub.mobileads.BaseVideoPlayerActivity.startVast;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(SdkTestRunner.class)
public class BaseVideoPlayerActivityTest {
    private static final String MRAID_VIDEO_URL = "https://mraidVideo";

    private long testBroadcastIdentifier;
    private VastVideoConfig mVastVideoConfig;
    private VastVideoConfigTwo mVastVideoConfigTwo;
    private CreativeOrientation mOrientation;

    @Before
    public void setup() throws Exception {
        mVastVideoConfig = mock(VastVideoConfig.class, withSettings().serializable());
        mVastVideoConfigTwo = mock(VastVideoConfigTwo.class, withSettings().serializable());
        testBroadcastIdentifier = 1234;
        mOrientation = CreativeOrientation.DEVICE;
    }

    @Test
    public void create_callsHideNavigationBar(){
        Activity subject = Robolectric.buildActivity(BaseVideoPlayerActivity.class).create().get();
        View decorView = subject.getWindow().getDecorView();

        assertThat(decorView.getSystemUiVisibility()).isEqualTo(UtilsTest.FLAGS);
    }

    @Test
    public void startMraid_shouldStartMraidVideoPlayerActivity() throws Exception {
        startMraid(Robolectric.buildActivity(Activity.class).create().get(), MRAID_VIDEO_URL);
        assertMraidVideoPlayerActivityStarted(MraidVideoPlayerActivity.class, MRAID_VIDEO_URL);
    }

    @Test
    public void startVast_withVastVideoConfig_shouldStartMraidVideoPlayerActivity() throws Exception {
        startVast(Robolectric.buildActivity(Activity.class).create().get(), mVastVideoConfig,
                testBroadcastIdentifier, mOrientation);
        assertVastVideoPlayerActivityStartedWithVastVideoConfig(MraidVideoPlayerActivity.class, mVastVideoConfig,
                testBroadcastIdentifier);
    }

    @Test
    public void startVast_withVastVideoConfigTwo_shouldStartMraidVideoPlayerActivity() throws Exception {
        startVast(Robolectric.buildActivity(Activity.class).create().get(), mVastVideoConfig,
                testBroadcastIdentifier, mOrientation);
        assertVastVideoPlayerActivityStartedWithVastVideoConfig(MraidVideoPlayerActivity.class, mVastVideoConfig,
                testBroadcastIdentifier);
    }

    @Test
    public void onDestroy_shouldReleaseAudioFocus() throws Exception {
        BaseVideoPlayerActivity subject = spy(
                Robolectric.buildActivity(BaseVideoPlayerActivity.class).create().get());
        AudioManager mockAudioManager = mock(AudioManager.class);
        when(subject.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mockAudioManager);

        subject.onDestroy();

        verify(mockAudioManager).abandonAudioFocus(null);
        verifyNoMoreInteractions(mockAudioManager);
    }

    static void assertVastVideoPlayerActivityStartedWithVastVideoConfig(final Class clazz,
            final VastVideoConfig vastVideoConfig,
            final long broadcastIdentifier) {
        final Intent intent = ShadowApplication.getInstance().getNextStartedActivity();
        assertIntentAndBroadcastIdentifierAreCorrect(intent, clazz, broadcastIdentifier);

        final VastVideoConfig expectedVastVideoConfig =
                (VastVideoConfig) intent.getSerializableExtra(VastVideoViewController.VAST_VIDEO_CONFIG);
        assertThat(expectedVastVideoConfig).isEqualsToByComparingFields(vastVideoConfig);
    }

    static void assertVastVideoPlayerActivityStartedWithVastVideoConfigTwo(final Class clazz,
                                                     final VastVideoConfigTwo vastVideoConfig,
                                                     final long broadcastIdentifier) {
        final Intent intent = ShadowApplication.getInstance().getNextStartedActivity();
        assertIntentAndBroadcastIdentifierAreCorrect(intent, clazz, broadcastIdentifier);

        final VastVideoConfigTwo expectedVastVideoConfig =
                (VastVideoConfigTwo) intent.getSerializableExtra(VastVideoViewController.VAST_VIDEO_CONFIG);
        assertThat(expectedVastVideoConfig).isEqualsToByComparingFields(vastVideoConfig);
    }

    public static void assertMraidVideoPlayerActivityStarted(final Class clazz, final String url) {
        final Intent intent = ShadowApplication.getInstance().getNextStartedActivity();
        assertIntentAndBroadcastIdentifierAreCorrect(intent, clazz, null);

        assertThat(intent.getStringExtra(VIDEO_URL)).isEqualTo(url);
    }

    static void assertIntentAndBroadcastIdentifierAreCorrect(final Intent intent,
            final Class clazz,
            final Long expectedBroadcastId) {
        assertThat(intent.getComponent().getClassName()).isEqualTo(clazz.getCanonicalName());
        assertThat(Utils.bitMaskContainsFlag(intent.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK)).isTrue();

        if (expectedBroadcastId != null) {
            final long actualBroadcastId = (Long) intent.getSerializableExtra(BROADCAST_IDENTIFIER_KEY);
            assertThat(actualBroadcastId).isEqualTo(expectedBroadcastId);
        }
    }
}
