package com.ellcie_healthy.ble_library.ble.models.streaming;



import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData;

import java.util.concurrent.LinkedBlockingQueue;

public class SensorDataConsumer<T> {
    public enum SensorDataConsumerType {
        FG,
        BG,
        ANY
    }

    private static final String TAG = SensorDataConsumer.class.getSimpleName();

    private final String mName;
    private final SensorDataConsumerType mConsumerType;
    private final SensorType mSensorType;
    private final LinkedBlockingQueue<SensorData<T>> mQueue;
    private final Object mLock = new Object();
    private volatile boolean mRunning = true;
    private Thread mThread = null;
    private SensorDataCallback<T> mCallback;
    private final Runnable mConsumerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Sensor consumer started: " + mSensorType + " - " + mName);

            while (mRunning) {
                // data can be null when force unlock polling
                SensorData<T> data = null;
                try {
                    data = mQueue.take();

                    synchronized (mLock) {
                        if (mRunning && (data != null) && (mCallback != null)) {
                            mCallback.onSensorData(data);
                        }
                    }
                } catch (InterruptedException e) {
                    Log.w(TAG, "mQueue interrupted: " + mSensorType + " - " + mName);
                }
            }
            Log.d(TAG, "Sensor consumer stopped: " + mSensorType + " - " + mName);
        }
    };

    public SensorDataConsumer(@NonNull SensorDataConsumerType consumerType, @NonNull String name, @NonNull SensorType sensor, @IntRange(from = 1, to = Integer.MAX_VALUE) int capacity) {
        this.mConsumerType = consumerType;
        this.mSensorType = sensor;
        this.mQueue = new LinkedBlockingQueue<>(capacity);
        this.mCallback = null;
        this.mName = name;
    }

    public SensorDataConsumer(@NonNull SensorDataConsumerType consumerType, @NonNull String name, @NonNull SensorType sensor, @IntRange(from = 1, to = Integer.MAX_VALUE) int capacity, @NonNull final SensorDataCallback<T> callback) {
        this(consumerType, name, sensor, capacity);
        this.mCallback = callback;
    }

    public void setCallback(final SensorDataCallback<T> callback) {
        synchronized (mLock) {
            this.mCallback = callback;
        }
    }

    public boolean hasCallback() {
        synchronized (mLock) {
            return mCallback != null;
        }
    }

    public synchronized void terminate() {
        mRunning = false;
        if (mThread != null) {
            mThread.interrupt();
        }
        mQueue.clear();
    }

    public SensorType getSensorType() {
        return mSensorType;
    }

    public final String getConsumerName() {
        return mName;
    }

    public SensorDataConsumerType getConsumerType() {
        return mConsumerType;
    }

    public boolean enqueueData(@NonNull final SensorData<T> data) {
        if (!mRunning) {
            return false;
        }

        return mQueue.offer(data);
    }

    public synchronized boolean start() {
        if (isRunning()) {
            Log.d(TAG, "Sensor consumer already running: " + mSensorType + " - " + mName);
            return true;
        }

        mThread = new Thread(mConsumerRunnable);
        mRunning = true;
        mQueue.clear();
        mThread.start();

        return true;
    }

    public synchronized void waitRunningThread() {
        if ((mThread == null) || !mThread.isAlive()) {
            mThread = null;
            return;
        }

        Log.d(TAG, "wait running thread: " + mSensorType + " - " + mName);
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "join interrupted: " + mSensorType + " - " + mName);
        }

        Log.d(TAG, "Running thread terminated: " + mSensorType + " - " + mName);
        mThread = null;
    }

    public synchronized void terminateAndWait() {
        if (!isRunning()) {
            return;
        }

        Log.d(TAG, "Terminated and wait running thread: " + mSensorType + " - " + mName);

        terminate();
        waitRunningThread();
    }

    public synchronized boolean isRunning() {
        return mRunning && (mThread != null) && mThread.isAlive();
    }
}