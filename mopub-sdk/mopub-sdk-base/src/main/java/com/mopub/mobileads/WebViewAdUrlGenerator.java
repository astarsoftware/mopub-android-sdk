// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.Context;

import com.astarsoftware.android.AndroidUtils;
import com.mopub.common.AdUrlGenerator;
import com.mopub.common.ClientMetadata;
import com.mopub.common.Constants;

import static com.mopub.common.ExternalViewabilitySessionManager.ViewabilityVendor;

public class WebViewAdUrlGenerator extends AdUrlGenerator {
    private final boolean mIsStorePictureSupported;

    public WebViewAdUrlGenerator(Context context, boolean isStorePictureSupported) {
        super(context);
        mIsStorePictureSupported = isStorePictureSupported;
    }

    @Override
    public String generateUrlString(String serverHostname) {
        initUrlString(serverHostname, Constants.AD_HANDLER);

        setApiVersion("6");

        final ClientMetadata clientMetadata = ClientMetadata.getInstance(mContext);
        addBaseParams(clientMetadata);

        setMraidFlag(true);

        setExternalStoragePermission(mIsStorePictureSupported);

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
        enableViewability(ViewabilityVendor.getEnabledVendorKey());

        return getFinalUrlString();
    }
}
