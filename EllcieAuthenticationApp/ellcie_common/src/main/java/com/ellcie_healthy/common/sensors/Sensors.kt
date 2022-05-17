package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.sensors

@Suppress("unused")
class Sensors {

    companion object Sensors {
        const val EYE_SENSOR_LEFT = 0x0C.toByte()
        const val EYE_SENSOR_RIGHT = 0x0B.toByte()
        const val EYE_SENSOR_SUM = 0x0D.toByte()
        const val ACCELEROMETER = 0x30.toByte()
        const val GYROSCOPE = 0x31.toByte()
        const val ACCELEROMETER_GYROSCOPE = 0x32.toByte()
        const val BAROMETER = 0x1C.toByte()
        const val UNKNOWN_SENSOR = 0x00.toByte()
        const val MIN_DELTA_EYE_SENSOR = 10.0
        const val MIN_DELTA_IMU_ACCELERO_SENSOR = 5.0
        const val MIN_DELTA_IMU_AG_GYRO_SENSOR = 5.0
        const val MIN_DELTA_PRESSURE_SENSOR = 5.0
        const val MIN_DELTA_DEFAULT = 10.0
        const val NB_POINTS_EYE_SENSOR = 500
        const val NB_POINTS_IMU_AG = 75
        const val NB_POINTS_PRESSURE = 75
        const val NB_POINTS_DEFAULT = 50
        const val IMU_STREAMING_MIN_VERSION = "1.0.18"
        const val PRESSURE_STREAMING_MIN_VERSION = "1.0.21"
    }
}