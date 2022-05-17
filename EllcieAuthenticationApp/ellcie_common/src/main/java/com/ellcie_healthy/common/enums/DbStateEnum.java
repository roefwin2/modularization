package com.ellcie_healthy.common.enums;

import androidx.annotation.NonNull;

/**
 * Created by Yann on 27/11/2018.
 */

public enum DbStateEnum {
    AVAILABLE("AVAILABLE"),
    SOON_UNAVAILABLE("SOON_UNAVAILABLE"),
    UNAVAILABLE("UNAVAILABLE"),
    UNDEFINED("");

    DbStateEnum(@SuppressWarnings("unused") String text) {
    }


    public static DbStateEnum getDbStateEnum(@NonNull String type) {
        DbStateEnum[] allDbState = DbStateEnum.values();
        for (DbStateEnum dbState : allDbState)  {
            if (type.equals(dbState.toString())) {
                return dbState;
            }
        }
        return UNDEFINED; // by default
    }
}
