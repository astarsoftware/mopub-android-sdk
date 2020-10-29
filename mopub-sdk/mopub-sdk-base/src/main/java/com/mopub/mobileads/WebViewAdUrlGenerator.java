// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.Context;

import com.astarsoftware.android.AndroidUtils;
import com.mopub.common.AdUrlGenerator;
import com.mopub.common.ClientMetadata;
import com.mopub.common.Constants;

public class WebViewAdUrlGenerator extends AdUrlGenerator {

    public WebViewAdUrlGenerator(Context context) {
        super(context);
    }

    @Override
    public String generateUrlString(String serverHostname) {
        initUrlString(serverHostname, Constants.AD_HANDLER);

        setApiVersion("6");

        final ClientMetadata clientMetadata = ClientMetadata.getInstance(mContext);
        addBaseParams(clientMetadata);

        setMraidFlag(true);

		if (AndroidUtils.isDebugBuild()) {
			if (mAdUnitId.startsWith("ASTAR")) {
				if (mAdUnitId.startsWith("ASTAR-MRAIDTEST")) {
					return String.format(
						"https://%s%s?v=%s&udid=%s&id=%s&nv=%s",
						"mraid-testing.astar.mobi",
						"/mraid.php",
						"6",
						"astar_test_udid",
						mAdUnitId,
						clientMetadata.getSdkVersion());
				}
			}
		}

        return getFinalUrlString();
    }
}
