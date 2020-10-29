// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.tests.base;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.web.sugar.Web;
import androidx.test.espresso.web.webdriver.Locator;

import com.mopub.framework.pages.AdDetailPage;
import com.mopub.simpleadsdemo.MoPubSampleActivity;
import com.mopub.simpleadsdemo.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

public class MoPubBaseTestCase {

    protected static final String WEB_PAGE_LINK = "https://www.mopub.com/en/click-test";
    private static final String RELEASE_WEB_PAGE_LINK = "https://www.mopub.com/en";
    private static final String MRAID_CLICKTHROUGH_BUTTON = "//div[@id=\"expanded\"]/img[2]";
    protected final int PORTRAIT_ORIENTATION = SCREEN_ORIENTATION_PORTRAIT;
    protected final int LANDSCAPE_ORIENTATION = SCREEN_ORIENTATION_LANDSCAPE;
    @Rule
    public IntentsTestRule<MoPubSampleActivity> mActivityRule =
            new IntentsTestRule<MoPubSampleActivity>(MoPubSampleActivity.class);
    protected AdDetailPage adDetailPage;

    @Before
    public void setUp() {
        adDetailPage = new AdDetailPage();
    }

    @After
    public void tearDown() {
    }

    /**
     * Validates if it is horizontally centered on screen.
     */
    protected void isAlignedInLine() {
        final ViewInteraction bannerElement = onView(allOf(withId(R.id.banner_mopubview),
                hasChildCount(1)));
        bannerElement.check(matches(adDetailPage.isCenteredInWindow()));
    }

    /**
     * Verify the navigation and loading of the given adUnit.
     */
    protected void isShownInFullscreen() {
        final ViewInteraction element = onView(allOf(withId(android.R.id.content)));
        element.check(matches(adDetailPage.isInFullscreen()));
    }

    protected void inLineAdDidLoad() {
        final ViewInteraction bannerElement = onView(allOf(withId(R.id.banner_mopubview),
                hasChildCount(1)));
        assertTrue("Ad banner failed to load", adDetailPage.waitForElement(bannerElement));
    }

    /**
     * Asserts the webView turns to the orientation position using the MoPubSampleActivity rule
     *
     * @param orientation ActivityInfo orientation to be set
     */
    protected void checkChangeOnRotation(final int orientation) {
        final ViewInteraction mraidElement =
                onView(allOf(withId(android.R.id.content), withParentIndex(1)));
        adDetailPage.changeOrientationTo(orientation);
        mraidElement.check(matches((adDetailPage.didRotate(orientation))));
    }

    /**
     * Asserts the webView orientation matches expected when expanded
     *
     * @param orientation ActivityInfo orientation to be asserted
     */
    protected void checkMraidElementChangeOrientationOnExpand(int orientation) {
        final ViewInteraction mraidElement =
                onView(allOf(withId(android.R.id.content), withParentIndex(1)));
        mraidElement.check(matches((adDetailPage.didRotate(orientation))));
    }

    /**
     * Verify clicking on the Ad shows the clickthrough url in the MoPubBrowser.
     *
     * @param id element to validate clickthrough
     */
    protected void hasClickthrough(final int id) {
        final ViewInteraction bannerElement = onView(allOf(withId(id),
                hasChildCount(1)));
        // Check for web page loads correctly
        adDetailPage.clickElement(bannerElement);
        final ViewInteraction browserLinkElement = onView(withText(RELEASE_WEB_PAGE_LINK));

        assertTrue("Browser Link not found", adDetailPage.waitForElement(browserLinkElement));
    }

    /**
     * Asserts the Mraid clickthrough works and has the MoPub browser redirect
     */
    protected void hasMraidClickthrough() throws InterruptedException {
        final ViewInteraction bannerElement = onView(allOf(withId(R.id.banner_mopubview),
                hasChildCount(1)));

        adDetailPage.clickElement(bannerElement);

        Web.WebInteraction elem = onWebView()
                .withElement(findElement(Locator.XPATH, MRAID_CLICKTHROUGH_BUTTON));
        adDetailPage.clickWebElement(elem, "Could not click web element");
        elem.reset();

        final ViewInteraction browserLinkElement = onView(withText(RELEASE_WEB_PAGE_LINK));
        assertTrue("Browser Link not found", adDetailPage.waitForElement(browserLinkElement));
    }
}
