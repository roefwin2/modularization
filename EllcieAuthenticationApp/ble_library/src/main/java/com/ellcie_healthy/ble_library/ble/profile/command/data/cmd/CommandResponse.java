package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.WriteRequestWithResponse;

import java.security.InvalidParameterException;
import java.util.Arrays;

import no.nordicsemi.android.ble.data.Data;

public abstract class CommandResponse extends WriteRequestWithResponse {
    private static final String TAG = "CommandResponse";

    private static final int FIRST_COMMAND_ID = 0x01;
    private static final int LAST_COMMAND_ID = 0xFF;
    private static final int MIN_DATA_SIZE = 1;
    private static final int MAX_DATA_SIZE = 19;
    private static final int FULL_DATA_SIZE = 20;

    private static final int COMMAND_RESPONSE_MIN_SIZE = 2;
    private static final int COMMAND_RESPONSE_MAX_SIZE = 20;

    private static final int COMMAND_ID_POS = 0;
    private static final int COMMAND_RESPONSE_CODE_POS = 1;
    private static final int COMMAND_RESPONSE_DATA_POS = COMMAND_RESPONSE_MIN_SIZE;
    private static final Object mLock = new Object();
    private static int mCommandId = FIRST_COMMAND_ID;
    private int mMinResponseDataSize;
    private int mMaxResponseDataSize;
    private boolean mValidity = false;

    public CommandResponse(@NonNull CommandCode cmd) throws InvalidParameterException {
        this(new byte[]{cmd.getCode()}, 0, 0);
    }

    public CommandResponse(@NonNull CommandCode cmd,
                           final byte data) throws InvalidParameterException {
        this(new byte[]{cmd.getCode(), data}, 0, 0);
    }

    public CommandResponse(@NonNull CommandCode cmd,
                           final byte[] data) throws InvalidParameterException {
        byte[] fullData = new byte[data.length + 1];
        if (data.length > 0) {
            System.arraycopy(data, 0, fullData, 1, data.length);
        }
        fullData[0] = cmd.getCode();
        fillObject(fullData, 0, 0);
    }

    public CommandResponse(@NonNull CommandCode cmd,
                           final byte data,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int responseDataSize) throws InvalidParameterException {
        this(new byte[]{cmd.getCode(), data}, responseDataSize, responseDataSize);
    }

    public CommandResponse(@NonNull CommandCode cmd,
                           final byte data,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int minResponseDataSize,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int maxResponseDataSize) throws InvalidParameterException {
        this(new byte[]{cmd.getCode(), data}, minResponseDataSize, maxResponseDataSize);
    }

    public CommandResponse(@NonNull CommandCode cmd,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int minResponseDataSize,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int maxResponseDataSize) throws InvalidParameterException {
        this(new byte[]{(byte) cmd.getCode()}, minResponseDataSize, maxResponseDataSize);
    }

    public CommandResponse(@NonNull CommandCode cmd,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int responseDataSize) throws InvalidParameterException {
        this(new byte[]{cmd.getCode()}, responseDataSize, responseDataSize);
    }

    public CommandResponse(@NonNull Data data) throws InvalidParameterException {
        this(data.getValue(), 0, 0);
    }

    public CommandResponse(@NonNull Data data,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int minResponseDataSize,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int maxResponseDataSize) throws InvalidParameterException {
        this(data.getValue(), minResponseDataSize, maxResponseDataSize);
    }

    public CommandResponse(@NonNull Data data,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int responseDataSize) throws InvalidParameterException {
        this(data.getValue(), responseDataSize, responseDataSize);
    }

    public CommandResponse(@NonNull byte[] data) throws InvalidParameterException {
        this(data, 0, 0);
    }

    public CommandResponse(@NonNull byte[] data,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int responseDataSize) throws InvalidParameterException {
        this(data, responseDataSize, responseDataSize);
    }

    public CommandResponse(@NonNull byte[] data,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int minResponseDataSize,
                           @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int maxResponseDataSize)
            throws InvalidParameterException {
        fillObject(data, minResponseDataSize, maxResponseDataSize);
    }

    protected final int getMinResponseDataSize() {
        return mMinResponseDataSize;
    }

    protected final int getMaxResponseDataSize() {
        return mMaxResponseDataSize;
    }

    private void fillObject(@NonNull byte[] data,
                            @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int minResponseDataSize,
                            @IntRange(from = 0, to = (COMMAND_RESPONSE_MAX_SIZE - COMMAND_RESPONSE_MIN_SIZE)) final int maxResponseDataSize)
            throws InvalidParameterException {

        if (data.length < MIN_DATA_SIZE) throw new InvalidParameterException();
        if (data.length > MAX_DATA_SIZE) throw new InvalidParameterException();

        mMinResponseDataSize = COMMAND_RESPONSE_MIN_SIZE + minResponseDataSize;
        mMaxResponseDataSize = COMMAND_RESPONSE_MIN_SIZE + maxResponseDataSize;

        byte[] fullData = new byte[data.length + 1];
        System.arraycopy(data, 0, fullData, 1, data.length);

        synchronized (mLock) {
            fullData[COMMAND_ID_POS] = (byte) mCommandId++;

            if (mCommandId > LAST_COMMAND_ID) {
                mCommandId = FIRST_COMMAND_ID;
            }
        }
        mRequestData = new Data(fullData);
    }

    protected void addQueryData(@NonNull byte[] data) throws InvalidParameterException {
        byte[] currData = mRequestData.getValue();
        if ((currData.length + data.length) > FULL_DATA_SIZE) throw new InvalidParameterException();

        byte[] fullData = new byte[currData.length + data.length];
        System.arraycopy(currData, 0, fullData, 0, currData.length);
        System.arraycopy(data, 0, fullData, currData.length, data.length);

        mRequestData = new Data(fullData);
    }

    protected void addQueryData(@NonNull byte data) throws InvalidParameterException {
        this.addQueryData(new byte[]{data});
    }

    protected boolean parseData(@NonNull final Data responseData) {
        // do nothing by default
        return true;
    }

    @Override
    public boolean isValid() {
        if (mValidity) {
            // no need to parse again
            return true;
        }

        Data received = this.getRawData();

        if (received == null) {
            Log.e(TAG, "no data received");
            return false;
        }
        if (received.size() < mMinResponseDataSize || received.size() > mMaxResponseDataSize) {
            Log.e(TAG, "invalid data size: " + this.getClass().toString() + " " + mMinResponseDataSize + " <= " + received.size() + " <= " + mMaxResponseDataSize);
            return false;
        }

        if (!mIsSent) {
            Log.e(TAG, "data not sent");
            return false;
        }

        final byte requestID = mRequestData.getByte(COMMAND_ID_POS);
        final byte receivedID = received.getByte(COMMAND_ID_POS);
        if (requestID != receivedID) {
            Log.e(TAG, "command id received differ from command send: " + mRequestData.getByte(COMMAND_ID_POS) + " != " + received.getByte(COMMAND_ID_POS));

            int realId = (requestID - 1);
            if (realId < FIRST_COMMAND_ID) {
                realId = LAST_COMMAND_ID - realId;
            }

            if (!mRetryOnInvalid && receivedID == realId) {
                Log.d(TAG, "receive previous id, will retry: " + receivedID + " - " + requestID + " - " + realId);
                mRetryOnInvalid = true;
            }

            return false;
        }

        CommandResponseCode crc = getResponseCode();
        if (crc != CommandResponseCode.OK && crc != CommandResponseCode.CUSTOM_RESPONSE_CODE) {
            Log.e(TAG, "invalid response code: " + crc);
            return false;
        }

        if (received.size() >= COMMAND_RESPONSE_MIN_SIZE) {
            mValidity = parseData(getResponseData(received.getValue()));
        }
        return mValidity;
    }

    public CommandResponseCode getResponseCode() {
        Data received = this.getRawData();
        if (received == null || received.size() < COMMAND_RESPONSE_MIN_SIZE) {
            return CommandResponseCode.ERROR;
        }
        return CommandResponseCode.valueOf(received.getByte(COMMAND_RESPONSE_CODE_POS));
    }

    public final int getIntResponseCode() {
        Data received = this.getRawData();
        if (received == null || received.size() < COMMAND_RESPONSE_MIN_SIZE) {
            return CommandResponseCode.ERROR.getCode();
        }
        return received.getByte(COMMAND_RESPONSE_CODE_POS);
    }

    @NonNull
    private final Data getResponseData(byte[] received) {
        if (received == null || received.length <= COMMAND_RESPONSE_MIN_SIZE) new Data();
        return new Data(Arrays.copyOfRange(received, COMMAND_RESPONSE_DATA_POS, received.length));
    }
}

