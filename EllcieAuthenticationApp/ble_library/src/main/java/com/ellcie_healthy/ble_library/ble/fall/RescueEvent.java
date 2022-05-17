package com.ellcie_healthy.ble_library.ble.fall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RescueEvent {
    public enum State {
        NO_EVENT, // when no helper is contacted
        HELPER_ACK, // when an helper has acquitted
        LAST_CONTACTED_HELPER, // the last helper contacted
        VITARIS_ACK, // when the assister return a solution (user_ok, false_alarm, etc...)
        ASSISTER_CONTACTED, // when the assister is contacted by EH
        EMERGENCY_CONTACTED // when the emergency are coming. DEPRECATED
    }

    private String mHelperDisplayName;
    private State mRescueState;
    private String mVitarisEvent;
    private String mHelperDisplayNameAckVitaris = "";


    public RescueEvent(@NonNull State rescueState, @NonNull String helperDisplayName) {
        mRescueState = rescueState;
        mHelperDisplayName = helperDisplayName;
    }


    public RescueEvent(@NonNull State rescueState, @NonNull String vitarisReason, @Nullable String helperDisplayNameAckVitaris) {
        mRescueState = rescueState;
        mVitarisEvent = vitarisReason;
        mHelperDisplayNameAckVitaris = helperDisplayNameAckVitaris;
    }


    public RescueEvent(State rescueState) {
        mRescueState = rescueState;
        mHelperDisplayName = "";
    }

    public String getHelperDisplayName() {
        return mHelperDisplayName;
    }

    public void setHelperDisplayName(String helperDisplayName) {
        mHelperDisplayName = helperDisplayName;
    }

    public State getRescueState() {
        return mRescueState;
    }

    public void setRescueState(State rescueState) {
        mRescueState = rescueState;
    }

    public String getVitarisAckReason() {
        return mVitarisEvent;
    }

    /**
     * Get the display name of the helper contacted by Vitaris
     * This helper take in charge the assisted.
     */
    public String getVitarisAckHelperDisplayName() {
        return mHelperDisplayNameAckVitaris;
    }
}
