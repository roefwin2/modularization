package com.ellcie_healthy.ble_library.ble.profile.measure;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataBroker;
import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataConsumer;
import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.DataGatheringDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.DebugDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.HumidityDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.PedometerDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.PressureDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.RiskDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.StreamingDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.TemperatureDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.WornTimeDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.BlinkDrowsinessInfoData;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.HeadRotationData;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.LookDirectionData;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.ThreeAxisData;
import com.ellcie_healthy.ble_library.ble.utils.Utils;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;

public class MeasureService extends BleService<MeasureCallbacks> {
    private static final String TAG = "MeasureService";

    private static final int MAX_TIMESTAMP_DIFF_BETWEEN_FRAMES_MS = 3600000; // max 1h without data in milliseconds

    private static final float MAX_THREE_AXIS_VALUE = 50;
    private static final float MIN_THREE_AXIS_VALUE = -50;
    private static final int MAX_EYE_SENSOR_VALUE = 0xFFFF;
    private static final int MIN_EYE_SENSOR_VALUE = 0;
    private static final float MAX_PRESSURE_VALUE = 1150;
    private static final float MIN_PRESSURE_VALUE = 800;
    private static final float MAX_TEMPERATURE_VALUE = 50;
    private static final float MIN_TEMPERATURE_VALUE = -20;
    private static final float MAX_BLINK_DURATION_VALUE = 5000;
    private static final float MAX_BLINK_INDEX_VALUE = 5;
    private static final float MAX_BLINK_INDEX_REALTIME_VALUE = 20;

    private static final int STREAMING_DATA_START_OFFSET = 5;

    private static final int MAX_FLOAT_NUMBER = 2;

    private static final float MIN_DIFF_TIMESTAMP = 1;
    private static final float MAX_DIFF_TIMESTAMP_EYE_SENSOR = 100;
    private static final float MAX_DIFF_TIMESTAMP_PRESSURE = 300;
    private static final float MAX_DIFF_TIMESTAMP_TEMPERATURE = 300;
    private static final float MAX_DIFF_TIMESTAMP_ACC_GYRO = 50;

    /**
     * Measure Service UUID.
     */
    private final static UUID MEASURE_SERVICE_UUID = UUID.fromString("838f7fdd-4c42-405f-b8d4-83a698cce2e0");
    private final static UUID PEDOMETER_CHARACTERISTIC_UUID = UUID.fromString("f189c777-f887-463e-87dd-c425645a36d4");
    private final static UUID TEMPERATURE_CHARACTERISTIC_UUID = UUID.fromString("04208641-bbc4-48da-a513-3f81465d1dea");
    private final static UUID HUMIDITY_CHARACTERISTIC_UUID = UUID.fromString("cc8f9df0-1cc7-4b7a-93d6-618eb30d264a");
    private final static UUID PRESSURE_CHARACTERISTIC_UUID = UUID.fromString("349861c9-b9bb-4d48-8e3f-0734ce400e93");
    private final static UUID WORN_CHARACTERISTIC_UUID = UUID.fromString("d31a32cc-af12-4c09-85fa-37d7477fa40d");
    private final static UUID STREAMING_CHARACTERISTIC_UUID = UUID.fromString("a864bb58-1b21-4b89-8f5a-6947341abbf0");
    private final static UUID RISK_CHARACTERISTIC_UUID = UUID.fromString("f4ef55c5-e17b-4853-a6e3-77b99cd2b134");
    private final static UUID DATA_GATHERING_CHARACTERISTIC_UUID = UUID.fromString("ffc2f57c-978c-44bb-a9cd-660e0b842f90");
    private final static UUID DEBUG_1_CHARACTERISTIC_UUID = UUID.fromString("1a499edc-caac-42f2-acc4-866da4a13f18");
//    private final static UUID DEBUG_2_CHARACTERISTIC_UUID = UUID.fromString("cb38fa82-555b-45ee-9973-0bce6728511e");

    private final HashMap<SensorType, Long> mStreamingTimestamps = new HashMap<>();

    public MeasureService(final EHBleManager manager, @NonNull final SensorDataBroker sensorDataBroker) {
        super(manager, MEASURE_SERVICE_UUID, "Measure");

        final BleCharacteristic<PedometerDataCallback> pedometer = new BleCharacteristic<>(PEDOMETER_CHARACTERISTIC_UUID,
                "Pedometer",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        PedometerDataCallback mPedometerDataCallback = new PedometerDataCallback() {
            @Override
            public void onStepValue(@NonNull BluetoothDevice device, int steps) {
                Log.d(TAG, "onStepValue: " + steps);
                if (mCallbacks != null)
                    mCallbacks.onStepValue(device, steps);
            }
        };
        pedometer.addCharacteristicCallback(mPedometerDataCallback);
        this.addCharacteristic(pedometer);

        final BleCharacteristic<TemperatureDataCallback> temperature = new BleCharacteristic<>(TEMPERATURE_CHARACTERISTIC_UUID,
                "Temperature",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        TemperatureDataCallback mTemperatureCallback = new TemperatureDataCallback() {
            @Override
            public void onTemperatureValue(@NonNull BluetoothDevice device, int temperature) {
                Log.d(TAG, "onTemperatureValue: " + temperature + "°C");

                if (mCallbacks != null)
                    mCallbacks.onTemperatureValue(device, temperature);
            }
        };
        temperature.addCharacteristicCallback(mTemperatureCallback);
        this.addCharacteristic(temperature);

        final BleCharacteristic<HumidityDataCallback> humidity = new BleCharacteristic<>(HUMIDITY_CHARACTERISTIC_UUID,
                "Humidity",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        HumidityDataCallback mHumidityCallback = new HumidityDataCallback() {
            @Override
            public void onHumidityValue(@NonNull BluetoothDevice device, int humidity) {
                Log.d(TAG, "onHumidityValue: " + humidity + "%");

                if (mCallbacks != null)
                    mCallbacks.onHumidityValue(device, humidity);
            }
        };
        humidity.addCharacteristicCallback(mHumidityCallback);
        this.addCharacteristic(humidity);

        final BleCharacteristic<PressureDataCallback> pressure = new BleCharacteristic<>(PRESSURE_CHARACTERISTIC_UUID,
                "Pressure",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        PressureDataCallback mPressureCallback = new PressureDataCallback() {
            @Override
            public void onPressureValue(@NonNull BluetoothDevice device, int pressure) {
                Log.d(TAG, "onPressureValue: " + pressure + "hPa");

                if (mCallbacks != null)
                    mCallbacks.onPressureValue(device, pressure);
            }
        };
        pressure.addCharacteristicCallback(mPressureCallback);
        this.addCharacteristic(pressure);

        final BleCharacteristic<WornTimeDataCallback> worn = new BleCharacteristic<>(WORN_CHARACTERISTIC_UUID,
                "Worn",
                false,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        WornTimeDataCallback mWornCallback = new WornTimeDataCallback() {
            @Override
            public void onWornValue(@NonNull BluetoothDevice device, int duration) {
                Log.d(TAG, "onWornValue: " + duration);

                if (mCallbacks != null)
                    mCallbacks.onWornValue(device, duration);
            }
        };
        worn.addCharacteristicCallback(mWornCallback);
        this.addCharacteristic(worn);

        final BleCharacteristic<StreamingDataCallback> streaming = new BleCharacteristic<>(STREAMING_CHARACTERISTIC_UUID,
                "Streaming",
                false,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);

        // Log.d(TAG, "receive: " + sensor);
        StreamingDataCallback mStreamingCallback = new StreamingDataCallback() {
            private boolean parseEyeSensor(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();
                int offset = STREAMING_DATA_START_OFFSET;
                long timestamp = ts;

                if (bData == null) {
                    return false;
                }

                //noinspection rawtypes
                final ArrayList<SensorData> datas = new ArrayList<>();
                // Val0
                int val = Utils.bytesToUnsignedShort(bData, offset);
                if (val >= MAX_EYE_SENSOR_VALUE || val <= MIN_EYE_SENSOR_VALUE) {
                    Log.e(TAG, "invalid data value!!!! " + data + " - " + val);
                    return false;
                }
                datas.add(new SensorData<>(sensor, timestamp, val));

                for (int i = 0; i < 3; i++) {
                    // TSX
                    offset += 2;
                    long diff = Utils.bytesToUnsignedShort(bData, offset);
                    if (diff < MIN_DIFF_TIMESTAMP || diff > MAX_DIFF_TIMESTAMP_EYE_SENSOR) {
                        Log.e(TAG, "invalid eye sensor data diff!!!! " + data + " - " + diff);
                        return false;
                    }
                    timestamp += diff;
                    // ValX
                    offset += 2;
                    val = Utils.bytesToUnsignedShort(bData, offset);
                    if (val >= MAX_EYE_SENSOR_VALUE || val <= MIN_EYE_SENSOR_VALUE) {
                        Log.e(TAG, "invalid eye sensor data value!!!! " + data + " - " + val);
                        return false;
                    }
                    datas.add(new SensorData<>(sensor, timestamp, val));
                }

                sensorDataBroker.dispatch(datas);
                return true;
            }

            private boolean parsePressure(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();
                byte[] bfData = new byte[4];
                int offset = STREAMING_DATA_START_OFFSET;
                long timestamp = ts;

                if (bData == null) {
                    return false;
                }

                bfData[0] = 0;
                System.arraycopy(bData, offset, bfData, 1, 3);
                //noinspection rawtypes
                final ArrayList<SensorData> datas = new ArrayList<>();
                float val = Utils.bytesToInt(bfData, 0) / 1000f;
                if (val < MIN_PRESSURE_VALUE || val > MAX_PRESSURE_VALUE) {
                    Log.e(TAG, "invalid pressure data value!!!! " + data + " - " + val);
                    return false;
                }

                datas.add(new SensorData<>(sensor, timestamp, val));

                for (int i = 0; i < 2; i++) {
                    // TSX
                    offset += 3;
                    long diff = Utils.bytesToUnsignedShort(bData, offset);
                    if (diff < MIN_DIFF_TIMESTAMP || diff > MAX_DIFF_TIMESTAMP_PRESSURE) {
                        Log.e(TAG, "invalid pressure data diff!!!! " + data + " - " + diff);
                        return false;
                    }
                    timestamp += diff;
                    // ValX
                    offset += 2;
                    bfData[0] = 0;
                    System.arraycopy(bData, offset, bfData, 1, 3);

                    val = Utils.bytesToInt(bfData, 0) / 1000f;
                    if (val < MIN_PRESSURE_VALUE || val > MAX_PRESSURE_VALUE) {
                        Log.e(TAG, "invalid pressure data value!!!! " + data + " - " + val);
                        return false;
                    }
                    datas.add(new SensorData<>(sensor, timestamp, val));
                }

                sensorDataBroker.dispatch(datas);
                return true;
            }

            private boolean parseTemperature(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();
                int offset = STREAMING_DATA_START_OFFSET;
                long timestamp = ts;

                if (bData == null) {
                    return false;
                }

                //noinspection rawtypes
                final ArrayList<SensorData> datas = new ArrayList<>();
                // Val0
                float val = Utils.bytesToShort(bData, offset) / 100f;
                if (val < MIN_TEMPERATURE_VALUE || val > MAX_TEMPERATURE_VALUE) {
                    Log.e(TAG, "invalid temperature data value!!!! " + data + " - " + val);
                    return false;
                }
                datas.add(new SensorData<>(sensor, timestamp, val));

                for (int i = 0; i < 3; i++) {
                    // TSX
                    offset += 2;
                    long diff = Utils.bytesToUnsignedShort(bData, offset);
                    if (diff < MIN_DIFF_TIMESTAMP || diff > MAX_DIFF_TIMESTAMP_TEMPERATURE) {
                        Log.e(TAG, "invalid temperature data diff!!!! " + data + " - " + diff);
                        return false;
                    }
                    timestamp += diff;
                    // ValX
                    offset += 2;
                    val = Utils.bytesToShort(bData, offset) / 100f;
                    if (val < MIN_TEMPERATURE_VALUE || val > MAX_TEMPERATURE_VALUE) {
                        Log.e(TAG, "invalid pressure data value!!!! " + data + " - " + val);
                        return false;
                    }
                    datas.add(new SensorData<>(sensor, timestamp, val));
                }

                sensorDataBroker.dispatch(datas);
                return true;
            }

            private int getDecimalPlaces(float f) {
                final String txt = Float.toString(f);
                int integerPlaces = txt.indexOf('.');
                if (integerPlaces < 1) {
                    return Integer.MAX_VALUE;
                }

                return txt.length() - integerPlaces - 1;
            }

            private boolean isInvalid3AxisData(@NonNull ThreeAxisData tad) {
                if (tad.getX() > MAX_THREE_AXIS_VALUE || tad.getX() < MIN_THREE_AXIS_VALUE ||
                        tad.getY() > MAX_THREE_AXIS_VALUE || tad.getY() < MIN_THREE_AXIS_VALUE ||
                        tad.getZ() > MAX_THREE_AXIS_VALUE || tad.getZ() < MIN_THREE_AXIS_VALUE) {
                    return true;
                }

                if (getDecimalPlaces(tad.getX()) > MAX_FLOAT_NUMBER ||
                        getDecimalPlaces(tad.getY()) > MAX_FLOAT_NUMBER ||
                        getDecimalPlaces(tad.getZ()) > MAX_FLOAT_NUMBER) {
                    Log.e(TAG, "inconsistant 3 axis data value!!!! " + tad);
                    return true;
                }

                return false;
            }

            private boolean parse3Axis(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();
                int offset = STREAMING_DATA_START_OFFSET;
                long timestamp = ts;

                if (bData == null) {
                    return false;
                }

                //noinspection rawtypes
                final ArrayList<SensorData> datas = new ArrayList<>();
                ThreeAxisData tad = new ThreeAxisData(Utils.bytesToShort(bData, offset) / 100f,
                        Utils.bytesToShort(bData, offset + 2) / 100f,
                        Utils.bytesToShort(bData, offset + 4) / 100f);

//            Log.d(TAG, "receive: " + sensor);
                if (isInvalid3AxisData(tad)) {
                    Log.e(TAG, "invalid accelerometer data value!!!! " + data + " - " + tad);
                    return false;
                }

                datas.add(new SensorData<>(sensor == SensorType.ACC_GYRO ? SensorType.ACCELEROMETER : sensor, timestamp, tad));
                offset += 6;
                long diff = Utils.bytesToUnsignedShort(bData, offset);
                if ((sensor != SensorType.ACC_GYRO && diff < MIN_DIFF_TIMESTAMP && diff > MAX_DIFF_TIMESTAMP_ACC_GYRO) || (sensor == SensorType.ACC_GYRO && diff != 0)) {
                    Log.e(TAG, "invalid acc/gyro data diff!!!! " + data + " - " + diff);
                    return false;
                }
                timestamp += diff;
                offset += 2;
                tad = new ThreeAxisData(Utils.bytesToShort(bData, offset) / 100f,
                        Utils.bytesToShort(bData, offset + 2) / 100f,
                        Utils.bytesToShort(bData, offset + 4) / 100f);

                if (isInvalid3AxisData(tad)) {
                    Log.e(TAG, "invalid gyre/acc data value!!!! " + data + " - " + tad);
                    return false;
                }
                datas.add(new SensorData<>(sensor == SensorType.ACC_GYRO ? SensorType.GYROSCOPE : sensor, timestamp, tad));

                sensorDataBroker.dispatch(datas);
                return true;
            }

            private boolean parseLookDirectionAngles(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();

                if (bData == null) {
                    return false;
                }

                final LookDirectionData ldd = new LookDirectionData(Utils.bytesToShort(bData, STREAMING_DATA_START_OFFSET),
                        Utils.bytesToShort(bData, STREAMING_DATA_START_OFFSET + 2));

                sensorDataBroker.dispatch(new SensorData<>(sensor, ts, ldd));
                return true;
            }

            private boolean parseHeadRotationAngles(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();

                if (bData == null) {
                    return false;
                }

                final HeadRotationData hrd = new HeadRotationData(Utils.bytesToShort(bData, STREAMING_DATA_START_OFFSET) / 100f,
                        Utils.bytesToShort(bData, STREAMING_DATA_START_OFFSET + 2) / 100f,
                        Utils.bytesToShort(bData, STREAMING_DATA_START_OFFSET + 4) / 100f);

                sensorDataBroker.dispatch(new SensorData<>(sensor, ts, hrd));
                return true;
            }

            private boolean parseBlinkDrowsinessInfo(@NonNull SensorType sensor, final long ts, @NonNull final Data data) {
                byte[] bData = data.getValue();

                if (bData == null) {
                    return false;
                }

                final BlinkDrowsinessInfoData bi = new BlinkDrowsinessInfoData(Utils.byteToUnsigned(bData[STREAMING_DATA_START_OFFSET]),
                        Utils.byteToUnsigned(bData[STREAMING_DATA_START_OFFSET + 1]),
                        Utils.bytesToUnsignedShort(bData, STREAMING_DATA_START_OFFSET + 2),
                        Utils.byteToUnsigned(bData[STREAMING_DATA_START_OFFSET + 4]),
                        Utils.byteToUnsigned(bData[STREAMING_DATA_START_OFFSET + 5]) / 10f);

                if (bi.getLastDuration() > MAX_BLINK_DURATION_VALUE ||
                        bi.getCurrMean() > MAX_BLINK_DURATION_VALUE ||
                        bi.getBestMean() > MAX_BLINK_DURATION_VALUE ||
                        bi.getRealTimeIdx() > MAX_BLINK_INDEX_REALTIME_VALUE ||
                        bi.getGaugeIdx() > MAX_BLINK_INDEX_VALUE) {
                    Log.e(TAG, "invalid blink data value!!!! " + data + " - " + bi);
                    return false;
                }

                sensorDataBroker.dispatch(new SensorData<>(sensor, ts, bi));
                return true;
            }

            @Nullable
            private Long getDataTimestamp(@NonNull SensorType sensor, @NonNull final Data data) {
                byte[] bData = data.getValue();
                if (bData == null) {
                    return null;
                }

                final long timestamp = Utils.bytesToInt(bData, 1);
                Long prevTs = mStreamingTimestamps.get(sensor);
                if (prevTs != null && (timestamp <= prevTs || (timestamp - prevTs) > MAX_TIMESTAMP_DIFF_BETWEEN_FRAMES_MS)) {
                    Log.e(TAG, "Invalid timestamp: " + sensor + " - previous: " + prevTs + " <> new: " + timestamp);
                    Log.e(TAG, "data: " + data);
                    return null;
                }
//                mStreamingTimestamps.put(sensor, timestamp);

                return timestamp;
            }

            private void storeDataTimestamp(@NonNull SensorType sensor, long timestamp) {
                mStreamingTimestamps.put(sensor, timestamp);
            }

            @Override
            public void onStreamingData(@NonNull BluetoothDevice device, @NonNull SensorType sensor, @NonNull final Data data) {
                /*final EHLicense license = EHLicense.getLicense(manager.getManagerContext());
                if (license == null || !license.isSensorAllow(sensor)) {
                    return;
                }*/

                final Long timestamp = getDataTimestamp(sensor, data);
                if (timestamp == null) {
                    return;
                }
                boolean valid = false;

                switch (sensor) {
                    case EYE_SENSOR_LEFT_RIGHT:
                    case EYE_SENSOR_LEFT_DOWN:
                    case EYE_SENSOR_LEFT_UP:
                    case EYE_SENSOR_RIGHT_UP:
                    case EYE_SENSOR_RIGHT_DOWN:
                        valid = parseEyeSensor(sensor, timestamp, data);
                        break;
                    case ATMO_PRESSURE:
                        valid = parsePressure(sensor, timestamp, data);
                        break;
                    case TEMPERATURE:
                        valid = parseTemperature(sensor, timestamp, data);
                        break;
                    case ACC_GYRO:
                    case GYROSCOPE:
                    case ACCELEROMETER:
                        valid = parse3Axis(sensor, timestamp, data);
                        break;
                    case LOOK_DIRECTION:
                        valid = parseLookDirectionAngles(sensor, timestamp, data);
                        break;
                    case HEAD_ROTATION:
                        valid = parseHeadRotationAngles(sensor, timestamp, data);
                        break;
                    case BLINK_DROWSINESS_INFO:
                        valid = parseBlinkDrowsinessInfo(sensor, timestamp, data);
                        break;
                    case AMB_LIGHT:
                    case BATT_TEMP:
                    case HUMIDITY:
                    case OPENLAB_COMPUTED:
                    case OPENLAB_RAW:
                    case PEDOMETER:
                        Log.d(TAG, "unsupported streaming: " + sensor);
                        return;
                }

                if (valid) {
                    storeDataTimestamp(sensor, timestamp);
                }
            }
        };
        streaming.addCharacteristicCallback(mStreamingCallback);
        this.addCharacteristic(streaming);

        final BleCharacteristic<RiskDataCallback> risk = new BleCharacteristic<>(RISK_CHARACTERISTIC_UUID,
                "Risk",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        RiskDataCallback mRiskCallback = new RiskDataCallback() {
            @Override
            public void onRiskValue(@NonNull BluetoothDevice device, @NonNull RiskData risk) {
                Log.d(TAG, "onRiskValue: " + risk);

                if (mCallbacks != null)
                    mCallbacks.onRiskValue(device, risk);
            }
        };
        risk.addCharacteristicCallback(mRiskCallback);
        this.addCharacteristic(risk);

        final BleCharacteristic<DataGatheringDataCallback> dataGathering = new BleCharacteristic<>(DATA_GATHERING_CHARACTERISTIC_UUID,
                "Data gathering",
                false,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        DataGatheringDataCallback mDataGatheringCallback = new DataGatheringDataCallback() {
            @Override
            public void onDataGatheringData(@NonNull BluetoothDevice device, @NonNull Data data) {
                Log.d(TAG, "onDataGatheringData: " + data);
                if (mCallbacks != null)
                    mCallbacks.onDataGatheringData(device, data);
            }
        };
        dataGathering.addCharacteristicCallback(mDataGatheringCallback);
        this.addCharacteristic(dataGathering);

        final BleCharacteristic<DebugDataCallback> debug1 = new BleCharacteristic<>(DEBUG_1_CHARACTERISTIC_UUID,
                "Debug 1",
                false);
        DebugDataCallback mDebug1Callback = new DebugDataCallback() {
            @Override
            public void onDebugData(@NonNull BluetoothDevice device, @NonNull Data data) {
                Log.d(TAG, "onDebugData: 1 - " + data);
            }
        };
        debug1.addCharacteristicCallback(mDebug1Callback);
        this.addCharacteristic(debug1);
    }

    public void clearStreamingTimestamps() {
        Log.d(TAG, "clear listeners: " + mStreamingTimestamps.size());
        mStreamingTimestamps.clear();
        Log.d(TAG, "listeners cleared: " + mStreamingTimestamps.size());
    }
}
