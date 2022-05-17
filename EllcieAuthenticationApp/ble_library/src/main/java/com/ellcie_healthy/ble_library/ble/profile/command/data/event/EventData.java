package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import java.security.InvalidParameterException;

import no.nordicsemi.android.ble.data.Data;

public class EventData {
    public final static int EVENT_DATA_MIN_SIZE = 1;
    protected final static int EVENT_DATA_POS = 1;
    protected Data mData;
    protected int mSize;

    public EventData() {
        mData = null;
        mSize = 0;
    }

    public EventData(Data data) {
        mData = data;
        mSize = data.size() - EVENT_DATA_POS;
    }

    public EventData(@NonNull Data data, int size) {
        if ((data.size() - EVENT_DATA_POS) != size)
            throw new InvalidParameterException("invalid data size: " + (data.size() - EVENT_DATA_POS) + " - expected: " + size);

        mData = data;
        mSize = size;
    }

    public Data getData() {
        return mData;
    }

    public int getSize() {
        return mSize;
    }

    @NonNull
    @Override
    public String toString() {
        return mData == null ? "" : mData.toString();
    }
}
