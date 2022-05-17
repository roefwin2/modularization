package com.ellcie_healthy.ble_library.ble.profile;

import no.nordicsemi.android.ble.data.Data;

public abstract class WriteRequestWithResponse extends WriteResponseCallback {
    protected Data mRequestData = null;
    protected boolean mRetryOnInvalid = false;

    public final Data getRequestData() {
        return mRequestData;
    }

    public final boolean shouldRetry() {
        return mRetryOnInvalid;
    }
}

