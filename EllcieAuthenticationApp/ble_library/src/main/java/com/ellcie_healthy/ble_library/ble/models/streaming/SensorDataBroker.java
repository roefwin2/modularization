package com.ellcie_healthy.ble_library.ble.models.streaming;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData;

import java.util.ArrayList;
import java.util.List;

public class SensorDataBroker {
    private static final String TAG = SensorDataBroker.class.getSimpleName();

    private final ArrayList<SensorDataConsumer> mConsumers = new ArrayList<>();

    public SensorDataBroker() {
    }

    public synchronized boolean addConsumer(@NonNull final SensorDataConsumer consumer) {
        if (mConsumers.contains(consumer)) {
            Log.d(TAG, "consumer for " + consumer.getConsumerName() + " already added");
            return true;
        }

        return mConsumers.add(consumer);
    }

    public synchronized void delConsumer(@NonNull final SensorDataConsumer consumer) {
        consumer.terminateAndWait();
        mConsumers.remove(consumer);
    }

    public synchronized void startListeners(@NonNull SensorDataConsumer.SensorDataConsumerType type) {
        for (SensorDataConsumer sdc : mConsumers) {
            if (type == SensorDataConsumer.SensorDataConsumerType.ANY || sdc.getConsumerType() == type) {
                sdc.start();
            }
        }
    }

    public synchronized void stopListeners(@NonNull SensorDataConsumer.SensorDataConsumerType type) {
        for (SensorDataConsumer sdc : mConsumers) {
            if (type == SensorDataConsumer.SensorDataConsumerType.ANY || sdc.getConsumerType() == type) {
                sdc.terminate();
            }
        }

        for (SensorDataConsumer sdc : mConsumers) {
            if (type == SensorDataConsumer.SensorDataConsumerType.ANY || sdc.getConsumerType() == type) {
                sdc.waitRunningThread();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void dispatch(@NonNull final SensorData data) {
        for (final SensorDataConsumer c : mConsumers) {
            if (c.getSensorType() == data.getSensorType() || c.getSensorType() == SensorType.ANY) {
                c.enqueueData(data);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void dispatch(@NonNull final List<SensorData> datas) {
        for (final SensorDataConsumer c : mConsumers) {
            for (int i = 0; i < datas.size(); i++) {
                final SensorData data = datas.get(i);
                if (c.getSensorType() == data.getSensorType() || c.getSensorType() == SensorType.ANY) {
                    c.enqueueData(data);
                }
            }
        }
    }
}
