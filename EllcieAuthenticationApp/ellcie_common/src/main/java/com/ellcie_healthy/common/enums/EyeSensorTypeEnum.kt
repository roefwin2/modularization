package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.enums

import java.util.*


/**
 * Created by Yann on 24/07/2018.
 */
enum class EyeSensorTypeEnum(private val mText: String) {
    //  IR_RIGHT("IR droite"),
    //  IR_LEFT("IR gauche"),
    IR_SUM("Sum IR left and right"), IR_LEFT_RIGHT("IR left and IR right");

    override fun toString(): String {
        return mText
    }

    companion object {

        /**
         * Return the list of sensors accessible by the role default (other than ALPHA)
         *
         * @return
         */
        @Suppress("unused")
        val eyeSensorListDefault: Array<EyeSensorTypeEnum>
            get() {
                val sensorsList: MutableList<EyeSensorTypeEnum> = ArrayList()
                sensorsList.add(IR_SUM)
                sensorsList.add(IR_LEFT_RIGHT)
                return sensorsList.toTypedArray()
            }

        /**
         * Return the list of sensors accessible by the role ALPHA
         *
         * @return
         */
        @Suppress("unused")
        val eyeSensorListAlpha: Array<EyeSensorTypeEnum>
            get() = values()
        val default: EyeSensorTypeEnum
            get() = IR_LEFT_RIGHT
    }
}
