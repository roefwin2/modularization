package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.otaUtils

/**
 * Created by Julien on 17/05/2021.
 */
class OtaUtils {
    companion object {
        //Ota file path
        const val FILE_OTA_SLOT1 = "slot_1.bin"

        const val FILE_OTA_SLOT0 = "slot_0.bin"
        const val SUCCESS = 0

        //Ota code
        val OTA_SUCCESS = byteArrayOf(0x00.toByte(), 0x00.toByte())

        val OTA_SLOT_0_ADDRESS = byteArrayOf(
            0x08.toByte(),
            0x01.toByte(), 0x80.toByte(), 0x00.toByte()
        )


    }
}