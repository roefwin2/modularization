package com.ellcie.nordicblelibrary.models

sealed class ScanState<T>(open val value : T){

    data class Started<T>(override val value: T) : ScanState<T>(value)
    data class Stopped<T>(override val value: T) : ScanState<T>(value)

}
