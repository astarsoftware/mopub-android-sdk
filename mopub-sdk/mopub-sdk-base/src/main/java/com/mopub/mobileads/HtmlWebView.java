// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.Context;

public class HtmlWebView extends BaseHtmlWebView {

    public HtmlWebView(Context context) {
        super(context);
    }

    public void init(BaseWebViewListener baseWebViewListener, String dspCreativeId) {
        super.init();
        final HtmlWebViewClient mHtmlWebViewClient =
                new HtmlWebViewClient(this, baseWebViewListener, dspCreativeId);
        setWebViewClient(mHtmlWebViewClient);
    }
}
