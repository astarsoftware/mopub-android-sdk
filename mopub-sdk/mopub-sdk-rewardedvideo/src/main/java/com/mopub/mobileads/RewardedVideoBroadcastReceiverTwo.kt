// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.mopub.common.IntentActions.ACTION_REWARDED_VIDEO_COMPLETE
import com.mopub.mobileads.RewardedVastVideoInterstitialTwo.RewardedVideoInterstitialListenerTwo

@Mockable
class RewardedVideoBroadcastReceiverTwo(
    private val rewardedVideoListener: RewardedVideoInterstitialListenerTwo?,
    broadcastIdentifier: Long
) : BaseBroadcastReceiver(broadcastIdentifier) {

    override fun getIntentFilter(): IntentFilter { return sIntentFilter }

    override fun onReceive(context: Context, intent: Intent) {
        when {
            rewardedVideoListener == null || !shouldConsumeBroadcast(intent) -> return
            ACTION_REWARDED_VIDEO_COMPLETE == intent.action -> {
                rewardedVideoListener.onVideoComplete()
                unregister(this)
            }
        }
    }

    companion object {
        private val sIntentFilter = IntentFilter(ACTION_REWARDED_VIDEO_COMPLETE)
    }
}
