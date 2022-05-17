package com.ellcie_healthy.ble_library.ble.profile.measure.data;

import androidx.annotation.NonNull;

public class RiskData {
    private RiskLevel mLevel;
    private int mTripId;

    public RiskData(@NonNull final RiskLevel level, final int tripId) {
        mLevel = level;
        mTripId = tripId;
    }

    public int getTripId() {
        return mTripId;
    }

    public RiskLevel getLevel() {
        return mLevel;
    }

    @NonNull
    @Override
    public String toString() {
        return mLevel.toString() + " (" + mTripId + ")";
    }

    public enum RiskLevel {
        RISK_LEVEL_NONE(0),
        RISK_LEVEL_1(1),
        RISK_LEVEL_2(2),
        RISK_LEVEL_3(3),
        RISK_LEVEL_4(4),
        RISK_LEVEL_5(5);

        private final int code;

        RiskLevel(int c) {
            code = c;
        }

        public static RiskLevel valueOf(int value) {
            for (RiskLevel e : values()) {
                if ((byte) e.code == (byte) value) {
                    return e;
                }
            }
            return RiskLevel.RISK_LEVEL_NONE;
        }

        public int getCode() {
            return code;
        }
    }
}
