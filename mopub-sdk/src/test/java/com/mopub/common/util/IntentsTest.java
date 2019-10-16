// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.AndroidRuntimeException;

import com.mopub.common.MoPub;
import com.mopub.common.MoPub.BrowserAgent;
import com.mopub.common.MoPubBrowser;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.exceptions.IntentNotResolvableException;
import com.mopub.exceptions.UrlParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(SdkTestRunner.class)
public class IntentsTest {
    private Activity activityContext;
    private Context applicationContext;

    @Before
    public void setUp() {
        activityContext = Robolectric.buildActivity(Activity.class).create().get();
        applicationContext = activityContext.getApplicationContext();
        MoPub.resetBrowserAgent();
    }

    @After
    public void tearDown() {
        MoPub.resetBrowserAgent();
    }

    @Test
    public void startActivity_withActivityContext_shouldStartActivityWithNoNewFlags() throws IntentNotResolvableException {
        Intents.startActivity(activityContext, new Intent());

        final Intent intent = ShadowApplication.getInstance().peekNextStartedActivity();
        assertThat(Utils.bitMaskContainsFlag(intent.getFlags(), FLAG_ACTIVITY_NEW_TASK)).isFalse();
    }

    @Test
    public void getStartActivityIntent_withActivityContext_shouldReturnIntentWithoutNewTaskFlag() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();

        final Intent intent = Intents.getStartActivityIntent(context, MoPubBrowser.class, null);

        assertThat(intent.getComponent().getClassName()).isEqualTo(MoPubBrowser.class.getName());
        assertThat(Utils.bitMaskContainsFlag(intent.getFlags(), FLAG_ACTIVITY_NEW_TASK)).isFalse();
        assertThat(intent.getExtras()).isNull();
    }

    @Test
    public void getStartActivityIntent_withApplicationContext_shouldReturnIntentWithNewTaskFlag() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class)
                .create().get().getApplicationContext();

        final Intent intent = Intents.getStartActivityIntent(context, MoPubBrowser.class, null);

        assertThat(intent.getComponent().getClassName()).isEqualTo(MoPubBrowser.class.getName());
        assertThat(Utils.bitMaskContainsFlag(intent.getFlags(), FLAG_ACTIVITY_NEW_TASK)).isTrue();
        assertThat(intent.getExtras()).isNull();
    }

    @Test
    public void getStartActivityIntent_withBundle_shouldReturnIntentWithExtras() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        Bundle bundle = new Bundle();
        bundle.putString("arbitrary key", "even more arbitrary value");

        final Intent intent = Intents.getStartActivityIntent(context, MoPubBrowser.class, bundle);

        assertThat(intent.getComponent().getClassName()).isEqualTo(MoPubBrowser.class.getName());
        assertThat(Utils.bitMaskContainsFlag(intent.getFlags(), FLAG_ACTIVITY_NEW_TASK)).isFalse();
        assertThat(intent.getExtras().size()).isEqualTo(1);
        assertThat(intent.getExtras().get("arbitrary key")).isEqualTo("even more arbitrary value");
    }

    @Test
    public void deviceCanHandleIntent_whenActivityCanResolveIntent_shouldReturnTrue() throws Exception {
        Context context = mock(Context.class);
        PackageManager packageManager = mock(PackageManager.class);

        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        resolveInfos.add(new ResolveInfo());

        when(context.getPackageManager()).thenReturn(packageManager);
        Intent specificIntent = new Intent();
        specificIntent.setData(Uri.parse("specificIntent:"));

        when(packageManager.queryIntentActivities(eq(specificIntent), eq(0))).thenReturn(resolveInfos);

        assertThat(Intents.deviceCanHandleIntent(context, specificIntent)).isTrue();
    }

    @Test
    public void deviceCanHandleIntent_whenActivityCanNotResolveIntent_shouldReturnFalse() throws Exception {
        Context context = mock(Context.class);
        PackageManager packageManager = mock(PackageManager.class);

        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        resolveInfos.add(new ResolveInfo());

        when(context.getPackageManager()).thenReturn(packageManager);
        Intent specificIntent = new Intent();
        specificIntent.setData(Uri.parse("specificIntent:"));

        Intent otherIntent = new Intent();
        otherIntent.setData(Uri.parse("other:"));
        when(packageManager.queryIntentActivities(eq(specificIntent), eq(0))).thenReturn(resolveInfos);

        assertThat(Intents.deviceCanHandleIntent(context, otherIntent)).isFalse();
    }

    @Test
    public void intentForNativeBrowserScheme_shouldProperlyHandleEncodedUrls() throws UrlParseException {
        Intent intent;

        intent = Intents.intentForNativeBrowserScheme(Uri.parse("mopubnativebrowser://navigate?url=https%3A%2F%2Fwww.example.com"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(intent.getDataString()).isEqualTo("https://www.example.com");

        intent = Intents.intentForNativeBrowserScheme(Uri.parse("mopubnativebrowser://navigate?url=https://www.example.com/?query=1&two=2"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(intent.getDataString()).isEqualTo("https://www.example.com/?query=1");

        intent = Intents.intentForNativeBrowserScheme(Uri.parse("mopubnativebrowser://navigate?url=https%3A%2F%2Fwww.example.com%2F%3Fquery%3D1%26two%3D2"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(intent.getDataString()).isEqualTo("https://www.example.com/?query=1&two=2");
    }

    @Test(expected = UrlParseException.class)
    public void intentForNativeBrowserScheme_whenNotMoPubNativeBrowser_shouldThrowException() throws UrlParseException {
        Intents.intentForNativeBrowserScheme(Uri.parse("mailto://navigate?url=https://www.example.com"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForNativeBrowserScheme_whenNotNavigate_shouldThrowException() throws UrlParseException {
        Intents.intentForNativeBrowserScheme(Uri.parse("mopubnativebrowser://getout?url=https://www.example.com"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForNativeBrowserScheme_whenUrlParameterMissing_shouldThrowException() throws UrlParseException {
        Intents.intentForNativeBrowserScheme(Uri.parse("mopubnativebrowser://navigate"));
    }

    @Test
    public void intentForNativeBrowserScheme_whenBrowserAgentSetToNative_whenSchemeIsMoPubNativeBrowser_shouldProperlyHandleEncodedUrls() throws UrlParseException {
        MoPub.setBrowserAgent(BrowserAgent.NATIVE);

        intentForNativeBrowserScheme_shouldProperlyHandleEncodedUrls();
    }

    @Test(expected = UrlParseException.class)
    public void intentForNativeBrowserScheme_whenBrowserAgentSetToNative_whenSchemeIsMoPubNativeBrowser_whenHostIsNotNavigate_shouldThrowException() throws UrlParseException {
        MoPub.setBrowserAgent(BrowserAgent.NATIVE);

        intentForNativeBrowserScheme_whenNotNavigate_shouldThrowException();
    }

    @Test(expected = UrlParseException.class)
    public void intentForNativeBrowserScheme_whenBrowserAgentSetToNative_whenSchemeIsMoPubNativeBrowserButUrlParameterMissing_shouldThrowException() throws UrlParseException {
        MoPub.setBrowserAgent(BrowserAgent.NATIVE);

        intentForNativeBrowserScheme_whenUrlParameterMissing_shouldThrowException();
    }

    @Test
    public void intentForNativeBrowserScheme_whenBrowserAgentSetToNative_whenSchemeIsHttpOrHttps_shouldProperlyHandleEncodedUrls() throws UrlParseException {
        MoPub.setBrowserAgent(BrowserAgent.NATIVE);

        Intent intent;

        intent = Intents.intentForNativeBrowserScheme(Uri.parse("http://www.example.com"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(intent.getDataString()).isEqualTo("http://www.example.com");

        intent = Intents.intentForNativeBrowserScheme(Uri.parse("https://www.example.com/?query=1&two=2"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(intent.getDataString()).isEqualTo("https://www.example.com/?query=1&two=2");

        intent = Intents.intentForNativeBrowserScheme(Uri.parse("https://www.example.com/?query=1%26two%3D2"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(intent.getDataString()).isEqualTo("https://www.example.com/?query=1%26two%3D2");
    }

    @Test(expected = UrlParseException.class)
    public void intentForNativeBrowserScheme_whenBrowserAgentSetToNative_whenSchemeNotMoPubNativeBrowserOrHttpOrHttps_shouldThrowException() throws UrlParseException {
        MoPub.setBrowserAgent(BrowserAgent.NATIVE);

        Intents.intentForNativeBrowserScheme(Uri.parse("foo://www.example.com"));
    }

    @Test
    public void intentForShareTweetScheme_whenValidUri_shouldReturnShareTweetIntent() throws UrlParseException {
        Intent intent;
        final String shareMessage = "Check out @SpaceX's Tweet: https://twitter.com/SpaceX/status/596026229536460802";

        intent = Intents.intentForShareTweet(Uri.parse("mopubshare://tweet?screen_name=SpaceX&tweet_id=596026229536460802"));
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_SEND);
        assertThat(intent.getType()).isEqualTo("text/plain");
        assertThat(intent.getStringExtra(Intent.EXTRA_SUBJECT)).isEqualTo(shareMessage);
        assertThat(intent.getStringExtra(Intent.EXTRA_TEXT)).isEqualTo(shareMessage);
    }

    @Test(expected = UrlParseException.class)
    public void intentForShareTweetScheme_whenWrongScheme_shouldThrowException() throws UrlParseException {
        Intents.intentForShareTweet(Uri.parse("mailto://tweet?screen_name=SpaceX&tweet_id=596026229536460802"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForShareTweetScheme_whenWrongHost_shouldThrowException() throws UrlParseException {
        Intents.intentForShareTweet(Uri.parse("mopubshare://twat?screen_name=SpaceX&tweet_id=596026229536460802"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForShareTweetScheme_whenScreenNameParameterMissing_shouldThrowException() throws UrlParseException {
        Intents.intentForShareTweet(Uri.parse("mopubshare://tweet?foo=SpaceX&tweet_id=596026229536460802"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForShareTweetScheme_whenScreenNameParameterIsEmpty_shouldThrowException() throws UrlParseException {
        Intents.intentForShareTweet(Uri.parse("mopubshare://tweet?screen_name=&tweet_id=596026229536460802"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForShareTweetScheme_whenTweetIdParameterMissing_shouldThrowException() throws UrlParseException {
        Intents.intentForShareTweet(Uri.parse("mopubshare://tweet?screen_name=SpaceX&bar=596026229536460802"));
    }

    @Test(expected = UrlParseException.class)
    public void intentForShareTweetScheme_whenTweetIdParameterIsEmpty_shouldThrowException() throws UrlParseException {
        Intents.intentForShareTweet(Uri.parse("mopubshare://tweet?screen_name=SpaceX&tweet_id="));
    }

    @Test
    public void launchIntentForUserClick_withActivityContext_shouldStartActivity() throws Exception {
        Intent intent = mock(Intent.class);

        Intents.launchIntentForUserClick(activityContext, intent, null);
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNotNull();
    }

    @Test(expected = AndroidRuntimeException.class)
    public void launchIntentForUserClick_withApplicationContext_shouldThrowAndroidRuntimeException() throws Exception {
        Intent intent = mock(Intent.class);
        Intents.launchIntentForUserClick(applicationContext, intent, null);
    }

    @Test
    public void launchApplicationUrl_withResolvableUrl_shouldOpenActivity() throws Exception {
        final String url = "url_to_installed_app";
        makeUrlResolvable(url);

        Intents.launchApplicationUrl(activityContext, Uri.parse(url));
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNotNull();
    }

    @Test
    public void launchApplicationUrl_withUnresolvableMarketUrl_shouldResolveToPlayStoreURLandOpenActivity() throws Exception {
        // unresolvable "market://" intent url
        final String marketUrl = "market://details?id=com.mopub.simpleadsdemo";

        // resolvable Play Store url
        final String playStoreUrl = "https://play.google.com/store/apps/details?id=com.mopub.simpleadsdemo";
        makeUrlResolvable(playStoreUrl);

        // use unresolvable "market://" url, but should start browser activity with resolvable Play Store url
        Intents.launchApplicationUrl(activityContext, Uri.parse(marketUrl));
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNotNull();
    }

    @Test
    public void launchApplicationUrl_withResolvableMarketUrl_shouldOpenPlayStoreActivity() throws Exception {
        final String marketUrl = "market://details?id=com.mopub.simpleadsdemo";
        makeUrlResolvable(marketUrl);
        // unresolvable equivalent Play Store url ("https://play.google.com/store/apps/details?id=com.mopub.simpleadsdemo")

        // use resolvable "market://" url
        Intents.launchApplicationUrl(activityContext, Uri.parse(marketUrl));
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNotNull();
    }

    @Test
    public void launchApplicationUrl_withUnresolvableMarketUrl_shouldResolveToAmazonStoreURLandOpenActivity() throws Exception {
        // unresolvable "amzn://" intent url
        final String storeUrl = "amzn://apps/android?p=com.mopub.simpleadsdemo";

        // resolvable Amazon Store url
        final String amazonStoreUrl = "http://www.amazon.com/gp/mas/dl/android?p=com.mopub.simpleadsdemo";
        makeUrlResolvable(amazonStoreUrl);

        // use unresolvable "market://" url, but should start browser activity with resolvable Play Store url
        Intents.launchApplicationUrl(activityContext, Uri.parse(storeUrl));
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNotNull();
    }

    @Test
    public void launchApplicationUrl_withResolvableMarketUrl_shouldOpenAmazonStoreActivity() throws Exception {
        final String storeUrl = "amzn://apps/android?p=com.mopub.simpleadsdemo";
        makeUrlResolvable(storeUrl);
        // unresolvable equivalent Play Store url ("https://play.google.com/store/apps/details?id=com.mopub.simpleadsdemo")

        // use resolvable "market://" url
        Intents.launchApplicationUrl(activityContext, Uri.parse(storeUrl));
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNotNull();
    }

    @Test(expected = IntentNotResolvableException.class)
    public void launchApplicationUrl_withUnresolvableUrl_shouldThrowIntentNotResolvableException() throws Exception {
        final String url = "url_to_installed_app";

        Intents.launchApplicationUrl(activityContext, Uri.parse(url));
        final Intent startedActivity = ShadowApplication.getInstance().peekNextStartedActivity();

        assertThat(startedActivity).isNull();
    }

    @Test
    public void getPlayStoreUri_shouldBuildUriFromIntentPackage() throws Exception {
        final Intent intent = new Intent();
        final String appPackage = "com.mopub.test";
        intent.setPackage(appPackage);

        assertThat(Intents.getPlayStoreUri(intent).toString()).isEqualTo("market://details?id="
                + appPackage);
    }

    @Test
    public void getPlayStoreUri_shouldNotBlowUpWithEmptyPackage() throws Exception {
        final Intent intent = new Intent();

        assertThat(Intents.getPlayStoreUri(intent).toString())
                .isEqualTo("market://details?id=null");
    }

    private void makeUrlResolvable(String url) {
        shadowOf(activityContext.getPackageManager()).addResolveInfoForIntent(
                new Intent(Intent.ACTION_VIEW, Uri.parse(url)), new ResolveInfo());
    }
}
