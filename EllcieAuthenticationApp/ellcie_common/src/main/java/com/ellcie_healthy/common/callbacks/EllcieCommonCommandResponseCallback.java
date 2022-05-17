package com.ellcie_healthy.common.callbacks;

/**
 * Created by Yann on 30/08/2018.
 */

public interface EllcieCommonCommandResponseCallback {
    /**
     * to use to retrieve a value from an asynchronous call
     *
     */
    void onResponseReceived(byte[] response);
}
