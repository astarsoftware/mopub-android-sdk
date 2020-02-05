// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads

import android.app.Activity
import com.mopub.common.MoPubReward
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.AdLogEvent

/**
 * A custom event for showing MoPub rewarded videos.
 */
class MoPubRewardedVideoTwo : MoPubRewardedAd() {
    var rewardedVastVideoInterstitial: RewardedVastVideoInterstitialTwo?

    override fun getAdNetworkId(): String {
        return mAdUnitId ?: MOPUB_REWARDED_VIDEO_TWO_ID
    }

    override fun onInvalidate() {
        rewardedVastVideoInterstitial?.onInvalidate()
        rewardedVastVideoInterstitial = null
        super.onInvalidate()
    }

    @Throws(Exception::class)
    override fun loadWithSdkInitialized(
        activity: Activity,
        localExtras: Map<String, Any>,
        serverExtras: Map<String, String>
    ) {
        super.loadWithSdkInitialized(activity, localExtras, serverExtras)
        rewardedVastVideoInterstitial?.loadInterstitial(
            activity,
            MoPubRewardedVideoTwoListener(),
            localExtras, serverExtras
        ) ?: MoPubLog.log(
            AdLogEvent.CUSTOM,
            "rewardedVastVideoInterstitialTwo is null. Has this class been invalidated?"
        )
    }

    override fun show() {
        if (isReady && rewardedVastVideoInterstitial != null) {
            MoPubLog.log(AdLogEvent.CUSTOM, "Showing MoPub rewarded video.")
            rewardedVastVideoInterstitial?.showInterstitial()
        } else {
            MoPubLog.log(AdLogEvent.CUSTOM, "Unable to show MoPub rewarded video")
        }
    }

    private inner class MoPubRewardedVideoTwoListener : MoPubRewardedAdListener(
        MoPubRewardedVideoTwo::class.java
    ), RewardedVastVideoInterstitialTwo.RewardedVideoInterstitialListenerTwo {
        override fun onVideoComplete() {
            rewardedAdCurrencyName?.let {
                MoPubRewardedVideoManager.onRewardedVideoCompleted(
                    mCustomEventClass,
                    adNetworkId,
                    MoPubReward.success(it, rewardedAdCurrencyAmount)
                )
            } ?:
            MoPubLog.log(
                AdLogEvent.CUSTOM,
                "No rewarded video was loaded, so no reward is possible"
            )
        }
    }

    companion object {
        const val MOPUB_REWARDED_VIDEO_TWO_ID = "mopub_rewarded_video_two_id"
    }

    init {
        rewardedVastVideoInterstitial = RewardedVastVideoInterstitialTwo()
    }
}
