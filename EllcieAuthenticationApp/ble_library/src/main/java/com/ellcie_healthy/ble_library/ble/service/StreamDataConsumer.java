package com.ellcie_healthy.ble_library.ble.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;


import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataConsumer;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureService;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData;
import com.ellcie_healthy.ble_library.ble.utils.FileStream;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class StreamDataConsumer {
    private static final String TAG = StreamDataConsumer.class.getSimpleName();
    private static final int CAPACITY = 10000;

    private static final HashMap<SensorType, String> STREAM_HEADERS = new HashMap<SensorType, String>() {{
        put(SensorType.ACCELEROMETER, "t" + SensorData.DATA_SEPARATOR + "ax" + SensorData.DATA_SEPARATOR + "ay" + SensorData.DATA_SEPARATOR + "az");
        put(SensorType.GYROSCOPE, "t" + SensorData.DATA_SEPARATOR + "gx" + SensorData.DATA_SEPARATOR + "gy" + SensorData.DATA_SEPARATOR + "gz");
        put(SensorType.HEAD_ROTATION, "t" + SensorData.DATA_SEPARATOR + "pitch" + SensorData.DATA_SEPARATOR + "roll" + SensorData.DATA_SEPARATOR + "yaw");
        put(SensorType.EYE_SENSOR_LEFT_DOWN, "t" + SensorData.DATA_SEPARATOR + "x");
        put(SensorType.EYE_SENSOR_LEFT_UP, "t" + SensorData.DATA_SEPARATOR + "x");
        put(SensorType.EYE_SENSOR_RIGHT_DOWN, "t" + SensorData.DATA_SEPARATOR + "x");
        put(SensorType.EYE_SENSOR_RIGHT_UP, "t" + SensorData.DATA_SEPARATOR + "x");
        put(SensorType.EYE_SENSOR_LEFT_RIGHT, "t" + SensorData.DATA_SEPARATOR + "x");
        put(SensorType.LOOK_DIRECTION, "t" + SensorData.DATA_SEPARATOR + "alpha" + SensorData.DATA_SEPARATOR + "beta");
        put(SensorType.ATMO_PRESSURE, "t" + SensorData.DATA_SEPARATOR + "p");
    }};
    private final HashMap<SensorType, FileStream> mFileStreams = new HashMap<>();
    private final Context mContext;
    private final MeasureService mService;
    private String mSerial = null;
    private int mTripId = -1;
    private Uri mRoot = null;

    @SuppressWarnings("unchecked")
    private final SensorDataConsumer mDataConsumer = new SensorDataConsumer(SensorDataConsumer.SensorDataConsumerType.BG, TAG, SensorType.ANY, CAPACITY, data -> handleData(data));

    public StreamDataConsumer(@NonNull final Context context, @NonNull final MeasureService service) {
        mContext = context;
        mService = service;
        //mService.addSensorDataConsumer(mDataConsumer);
        mDataConsumer.start();
    }

    public synchronized void setRoot(Uri uri) {
        if (uri == null || !uri.equals(mRoot)) {
            closeAll();
        }
        mRoot = uri;
    }

    public void setSerial(String serial) {
        setSerialAndTripId(serial, mTripId);
    }

    public void setTripId(int tripId) {
        setSerialAndTripId(mSerial, tripId);
    }

    public synchronized void setSerialAndTripId(String serial, int tripId) {
        Log.d(TAG, "setSerialAndTripId: " + serial + " - " + tripId);
        if (serial == null || !serial.equals(mSerial) || tripId <= 0 || tripId != mTripId) {
            closeAll();
        }

        mSerial = serial;
        mTripId = tripId;
    }

    public synchronized void closeAll() {
//        Log.d(TAG, "close all streams");
        for (Map.Entry<SensorType, FileStream> s : mFileStreams.entrySet()) {
            s.getValue().close();
        }

        mFileStreams.clear();
    }

    public synchronized void flushAll() {
//        Log.d(TAG, "flush all streams");
        for (Map.Entry<SensorType, FileStream> s : mFileStreams.entrySet()) {
            s.getValue().flush();
        }
    }

    private synchronized void handleData(SensorData data) {
        if (mRoot == null || mSerial == null || mTripId <= 0) {
//            Log.d(TAG, "Handle data but do nothing: " + mRoot + " - " + mSerial + " - " + mTripId);
            return;
        }

        FileStream fs = mFileStreams.get(data.getSensorType());
        if (fs == null) {
            try {
                Log.d(TAG, "create file: " + data.getSensorType().name());
                String header = STREAM_HEADERS.get(data.getSensorType());
                fs = new FileStream(mContext, mRoot, mSerial, mTripId, data.getSensorType().name(), (header != null) ? header : "");
                if (fs == null) return;
                fs.open();
                mFileStreams.put(data.getSensorType(), fs);
            } catch (FileNotFoundException e) {
                return;
            }
        }

        fs.write(data);
    }
}
