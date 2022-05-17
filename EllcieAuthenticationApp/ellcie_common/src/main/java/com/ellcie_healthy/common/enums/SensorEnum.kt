package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.enums


/**
 * Created by Yann on 12/06/2018.
 */
enum class SensorEnum(private val mText: String) {
    EYE_SENSOR("eye_sensor"),
    @Suppress("unused")
    IMU_GYRO_NORM("imu_gyro"),
    @Suppress("unused")
    IMU_ACCELERO_NORM("imu_accelero"),
    PRESSURE("pressure_sensor");

    override fun toString(): String {
        return mText
    }

    companion object {
        val default: SensorEnum
            get() = EYE_SENSOR

        @Suppress("unused")
        fun getSensorEnumByValue(graph: String): SensorEnum {
            val sensors: Array<SensorEnum> = values()
            for (sensor in sensors) {
                if (graph == sensor.toString()) {
                    return sensor
                }
            }
            return default
        }
    }
}
