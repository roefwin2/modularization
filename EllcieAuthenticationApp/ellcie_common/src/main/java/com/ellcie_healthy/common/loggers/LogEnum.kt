package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers

/**
 * Created by Julien on 06/05/2021.
 */
@Suppress("unused")
enum class LogEnum
/**
 * @param text
 */(val text: String) {
    // --- ERRORS ---
    // GLASSES
    SE2002("Bad serial number"), SE2003("Mac address of the glasses is not available, unable to startService"), SE2004("Impossible to read firmware version (timeout) from glasses"), SE2006("Impossible to read serial number version (timeout) from glasses"),  // BLE
    SBLEE1001("ble state BONDING FAIL"),  // OTA
    SEO014("Ota failed, max errors is reached"),
    SEO015("Ota failed, timeout (old ota)"),
    SEO016("Ota error"), SEO017("Ota init timeout reached"),  // - MOBILE
    SE3002("Failed to load offline logs"),  // - TEXT TO SPEECH
    SEA0A1("TextToSpeech: initialization failed"), SEA0A2("TextToSpeech: not initialized"),  // Service
    SES002("Service: user not authenticated"), SES003("Service: user not correctly pushed on the glasses"),  // - BLE
    SEB001("Service measure not discovered"), SEB002("Service battery not discovered"), SEB003("Service ota not discovered"), SEB005("Service control not discovered"), SEB006("Service device info not discovered"),  // - Other
    SEZ001("Could not write file (sensor/algo event)"), SEZ002("Could not upload(sensor/algo event) file, file does not exist"), SEZ003("Could not upload optician file, file does not exist"), SEZ004("Could not upload fall streaming file, file does not exist"),  // ****** WARNING ******

    // - OTA
    SWO001("Ota timeout reached"),  // - Command sent to Glasses
    SWC001("Lock Taps"), SWC002("Unlock Taps"), SW2001("Warning, service already started"), SW2002("Warning, service already binded"),  // ****** INFO ******

    // - Glasses status
    SI2001("Glasses connected"), SI2003("Glasses ready"), SI2006("Reconnection to the glasses"), SI2008("Bonded to the glasses"), SI2014("Not bonded to the glasses"), SI2015("Glasses disconnected"), SI2209("Glasses temperature received"), SBLEI1002("ble state CONNECTING"), SBLEI1003("ble state CONNECTED"), SBLEI1004("ble state DISCONNECTING"), SBLEI1005("ble state DISCONNECTED"), SBLEI1006("ble state BONDED"), SBLEI1007("ble state NOT BONDED"), SBLEI1008("App subscribed to all characteristics required"), SBLEI1009("Retry to subscribes to all characteristics"),  // - User related
    SIU008("User changes the silent mode"),  // - App services
    SIS006("Offline logs load ongoing"), SIS007("Offline logs loaded successfully"), SIS009("Offline logs, get log called"), SIS010("user id correctly pushed on the glasses"),  // - Appairage/Scan
    SIA005("'CLOSE' button pressed, app ellcie killed.");

    companion object {
        /**
         * Check if the given string is a valid code
         * @param code
         * @return
         */
        fun exist(code: String?): Boolean {
            for (aEnum in values()) {
                if (aEnum.name.equals(code, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}