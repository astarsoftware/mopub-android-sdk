// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads

import android.os.Handler
import com.mopub.common.Preconditions

/**
 * A runnable that is used to update a [VastVideoViewController]'s countdown display according
 * to rules contained in the [VastVideoViewController]
 */
class VastVideoViewCountdownRunnableTwo(
    private val videoViewController: VastVideoViewControllerTwo,
    handler: Handler
) : RepeatingHandlerRunnable(handler) {
    override fun doWork() {
        videoViewController.updateCountdown()
    }
}
