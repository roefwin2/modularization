package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.otaUtils

import kotlin.experimental.xor

object ChecksumUtils {
    /**
     * Compute checksum by doing a xor
     *
     * @param data
     * @return
     */
    @JvmStatic
    fun computeChecksum(data: ByteArray): Byte {
        //Logger.d(TAG, "computeChecksum()");
        var checksum: Byte = 0
        for (i in data.indices) {
            checksum = checksum xor data[i]
        }
        return checksum
    }
}
