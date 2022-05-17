package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class LogV2 {
    public static final int LOG_V2_DATA_SIZE = 8;

    private final long timestamp;
    private final byte code;
    private final byte reason;
    private final int data;
    private final byte[] dataByte;
    private byte[] dataArray = null;


    public LogV2(long timestamp, byte code, byte reason, @IntRange(from = 0, to = 0xFFFF) int data) {
        this.timestamp = timestamp;
        this.code = code;
        this.reason = reason;
        this.data = data;
        this.dataByte = new byte[1];
    }

    public LogV2(@NonNull byte[] data) {
        if (data.length != LOG_V2_DATA_SIZE) throw new InvalidParameterException();


        this.timestamp = ((long) ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt()) * 1000;
        this.code = data[4];
        this.reason = data[5];
        this.data = ByteBuffer.wrap(Arrays.copyOfRange(data, 6, 8)).order(ByteOrder.LITTLE_ENDIAN).getShort();
        this.dataByte = data;
        this.dataArray = ByteBuffer.wrap(Arrays.copyOfRange(data, 6, 8)).order(ByteOrder.LITTLE_ENDIAN).array();
        ArrayUtils.reverse(dataArray);

    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte getCode() {
        return code;
    }

    public byte getReason() {
        return reason;
    }

    public int getData() {
        return data;
    }

    public byte[] getDataArray() {
        return dataArray;
    }

    public byte[] getDataByte() {
        return dataByte;
    }

    @Override
    public String toString() {
        return "LogV2{" +
                "timestamp=" + timestamp +
                ", code=" + String.format("0x%02X", (int) code & 0xFF) +
                ", reason=" + String.format("0x%02X", (int) reason & 0xFF) +
                ", data=" + String.format("0x%04X", data & 0xFFFF) +
                '}';
    }
}
