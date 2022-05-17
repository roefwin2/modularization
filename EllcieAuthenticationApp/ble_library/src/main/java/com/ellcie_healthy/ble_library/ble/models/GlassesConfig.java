package com.ellcie_healthy.ble_library.ble.models;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GlassesConfig {
    private int alarmVolume;
    private int notifVolume;
    private int alarmLuminosity;
    private int notifLuminosity;
    private boolean silentMode;
    private boolean enableSos;
    private boolean enableFaiting;
    private int sensitivityLevel;

    public GlassesConfig(@IntRange(from = 0, to = 10) final int alarmVolume,
                         @IntRange(from = 0, to = 10) final int notifVolume,
                         @IntRange(from = 0, to = 100) final int alarmLuminosity,
                         @IntRange(from = 0, to = 100) final int notifLuminosity,
                         final boolean silentMode,
                         final boolean enableSos,
                         final boolean enableFaiting,
                         final int sensitivityLevel
    ) {
        this.alarmVolume = alarmVolume;
        this.notifVolume = notifVolume;
        this.alarmLuminosity = alarmLuminosity;
        this.notifLuminosity = notifLuminosity;
        this.silentMode = silentMode;
        this.enableSos = enableSos;
        this.enableFaiting = enableFaiting;
        this.sensitivityLevel = sensitivityLevel;
    }

    public int getAlarmVolume() {
        return alarmVolume;
    }

    public void setAlarmVolume(@IntRange(from = 0, to = 10) final int alarmVolume) {
        this.alarmVolume = alarmVolume;
    }

    public int getNotifVolume() {
        return notifVolume;
    }

    public void setNotifVolume(@IntRange(from = 0, to = 10) final int notifVolume) {
        this.notifVolume = notifVolume;
    }

    public int getAlarmLuminosity() {
        return alarmLuminosity;
    }

    public void setAlarmLuminosity(@IntRange(from = 0, to = 100) final int alarmLuminosity) {
        this.alarmLuminosity = alarmLuminosity;
    }

    public int getNotifLuminosity() {
        return notifLuminosity;
    }

    public void setNotifLuminosity(@IntRange(from = 0, to = 100) final int notifLuminosity) {
        this.notifLuminosity = notifLuminosity;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public boolean isEnableSos() {
        return enableSos;
    }

    public void setEnableSos(boolean enableSos) {
        this.enableSos = enableSos;
    }

    public boolean isEnableFaiting() {
        return enableFaiting;
    }

    public void setEnableFaiting(boolean enableFaiting) {
        this.enableFaiting = enableFaiting;
    }

    public int getSensitivityLevel() {
        return sensitivityLevel;
    }

    public void setSensitivityLevel(@IntRange(from = 1, to = 5) final int sensitivityLevel) {
        this.sensitivityLevel = sensitivityLevel;
    }

    public boolean isValidSensitivity() {
        int sensitivity = this.sensitivityLevel;
        if ((1 <= sensitivity && sensitivity <= 5)) {
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "GlassesConfig{" +
                "alarmVolume=" + alarmVolume +
                ", notifVolume=" + notifVolume +
                ", alarmLuminosity=" + alarmLuminosity +
                ", notifLuminosity=" + notifLuminosity +
                ", silentMode=" + silentMode +
                ", enableSos=" + enableSos +
                ", enableFaiting=" + enableFaiting +
                ", sensitivityLevel=" + sensitivityLevel +
                '}';
    }
}
