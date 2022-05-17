package com.ellcie_healthy.ble_library.ble.profile.measure.data;

import androidx.annotation.NonNull;

public class BlinkDrowsinessInfoData {
    private final int currMean;
    private final int bestMean;
    private final int lastDuration;
    private final float realTimeIdx;
    private final int gaugeIdx;

    public BlinkDrowsinessInfoData(int currMean, int bestMean, int lastDuration, int gaugeIdx, float realTimeIdx) {
        this.currMean = currMean;
        this.bestMean = bestMean;
        this.lastDuration = lastDuration;
        this.gaugeIdx = gaugeIdx;
        this.realTimeIdx = realTimeIdx;
    }

    public int getCurrMean() {
        return currMean;
    }

    public int getBestMean() {
        return bestMean;
    }

    public int getLastDuration() {
        return lastDuration;
    }

    public float getRealTimeIdx() {
        return realTimeIdx;
    }

    public int getGaugeIdx() {
        return gaugeIdx;
    }

    @NonNull
    @Override
    public String toString() {
        return currMean + SensorData.DATA_SEPARATOR +
                bestMean + SensorData.DATA_SEPARATOR +
                lastDuration + SensorData.DATA_SEPARATOR +
                gaugeIdx + SensorData.DATA_SEPARATOR +
                realTimeIdx;
    }
}
