package com.ellcie_healthy.ble_library.ble.profile.measure.data;

public class LookDirectionData {
    private int alpha, beta;

    public LookDirectionData(int alpha, int beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    @Override
    public String toString() {
        return alpha + SensorData.DATA_SEPARATOR + beta;
    }
}