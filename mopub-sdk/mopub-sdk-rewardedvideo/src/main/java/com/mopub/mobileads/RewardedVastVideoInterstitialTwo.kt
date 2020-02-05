// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads

import android.content.Context
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED
import com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_SUCCESS

@Mockable
class RewardedVastVideoInterstitialTwo : VastVideoInterstitialTwo() {
    interface RewardedVideoInterstitialListenerTwo :
        CustomEventInterstitialListener {
        fun onVideoComplete()
    }

    var rewardedVideoBroadcastReceiver: RewardedVideoBroadcastReceiverTwo? = null

    override fun loadInterstitial(
        context: Context,
        customEventInterstitialListener: CustomEventInterstitialListener,
        localExtras: Map<String, Any>,
        serverExtras: Map<String, String>
    ) {
        MoPubLog.log(LOAD_ATTEMPTED, ADAPTER_NAME)
        super.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras)
        if (customEventInterstitialListener is RewardedVideoInterstitialListenerTwo) {
            rewardedVideoBroadcastReceiver = RewardedVideoBroadcastReceiverTwo(
                customEventInterstitialListener,
                mBroadcastIdentifier
            ).also {
                it.register(it, context)
            }
        }
    }

    override fun onVastVideoConfigurationPrepared(vastVideoConfig: VastVideoConfig?) {
        MoPubLog.log(LOAD_SUCCESS, ADAPTER_NAME)
        vastVideoConfig?.setIsRewardedVideo(true)
        super.onVastVideoConfigurationPrepared(vastVideoConfig)
    }

    override fun onInvalidate() {
        super.onInvalidate()
        rewardedVideoBroadcastReceiver?.unregister(rewardedVideoBroadcastReceiver)
    }

    companion object {
        val ADAPTER_NAME =
            RewardedVastVideoInterstitialTwo::class.java.simpleName
    }
}
