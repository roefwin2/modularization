package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase


enum class VitarisEvent(  // bad situation (no solution found by Vitaris)
        val value: String) {
    USER_OK("VITARIS_USER_OK"),  // when the user is ok
    FALSE_ALARM("VITARIS_FALSE_ALARM"),  // false alarm
    VITARIS_HELPER_ACK("VITARIS_ACK_HELPER"),  // when a helper take in charge
    EMERGENCY("VITARIS_EMERGENCY"),  // when the emergency take in charge
    NO_SOLUTION("VITARIS_NO_SOLUTION");

}
