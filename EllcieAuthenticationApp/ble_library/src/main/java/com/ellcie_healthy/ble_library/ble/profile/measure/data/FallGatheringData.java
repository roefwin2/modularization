package com.ellcie_healthy.ble_library.ble.profile.measure.data;

public class FallGatheringData {
    private int seq;
    private byte crc;
    private final byte[] data = new byte[17];

    public FallGatheringData(final int seq, final byte[] data, byte crc) {
        this.crc = crc;
        this.seq = seq;
        System.arraycopy(data, 0, this.data, 0, this.data.length);
    }

    public static byte calculateCrc(final byte[] data) {
        byte crc = 0;
        if (data.length != 20) return crc;

        for (int i = 1; i < data.length; i++) {
            crc ^= (data[i] & 0xFF);
        }

        return crc;
    }

    public byte getCrc() {
        return crc;
    }

    public int getSeq() {
        return seq;
    }

    public byte[] getData() {
        return data;
    }
}