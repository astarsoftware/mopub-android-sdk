// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.volley.NetworkResponse;
import com.mopub.volley.VolleyError;

public class MoPubNetworkError extends VolleyError {
    public enum Reason {
        WARMING_UP,
        NO_FILL,
        BAD_HEADER_DATA,
        BAD_BODY,
        TRACKING_FAILURE,
        TOO_MANY_REQUESTS,
        UNSPECIFIED
    }

    @NonNull private final Reason mReason;
    @Nullable private final Integer mRefreshTimeMillis;

    public MoPubNetworkError(@NonNull Reason reason) {
        super();
        mReason = reason;
        mRefreshTimeMillis = null;
    }

    public MoPubNetworkError(@NonNull NetworkResponse networkResponse, @NonNull Reason reason) {
        super(networkResponse);
        mReason = reason;
        mRefreshTimeMillis = null;
    }

    public MoPubNetworkError(@NonNull Throwable cause, @NonNull Reason reason) {
        super(cause);
        mReason = reason;
        mRefreshTimeMillis = null;
    }

    public MoPubNetworkError(@NonNull String message, @NonNull Reason reason) {
        this(message, reason, null);
    }

    public MoPubNetworkError(@NonNull String message, @NonNull Throwable cause, @NonNull Reason reason) {
        super(message, cause);
        mReason = reason;
        mRefreshTimeMillis = null;
    }

    public MoPubNetworkError(@NonNull String message, @NonNull Reason reason,
            @Nullable Integer refreshTimeMillis) {
        super(message);
        mReason = reason;
        mRefreshTimeMillis = refreshTimeMillis;
    }

    @NonNull
    public Reason getReason() {
        return mReason;
    }

    @Nullable
    public Integer getRefreshTimeMillis() {
        return mRefreshTimeMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MoPubNetworkError that = (MoPubNetworkError) o;

        if (mReason != that.mReason) {
            return false;
        }
        return mRefreshTimeMillis != null ?
                mRefreshTimeMillis.equals(that.mRefreshTimeMillis) : that.mRefreshTimeMillis == null;
    }

    @Override
    public int hashCode() {
        int result = mReason.hashCode();
        result = 31 * result + (mRefreshTimeMillis != null ? mRefreshTimeMillis.hashCode() : 0);
        return result;
    }
}
