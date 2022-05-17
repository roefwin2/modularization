package com.ellcie.nordicblelibrary.models

import com.ellcie.nordicblelibrary.repository.DeviceInitializedRepository

sealed class DeviceConnection

object Connecting : DeviceConnection()
data class Initializing(val initializedRepository : DeviceInitializedRepository) : DeviceConnection()
object Connected: DeviceConnection()
object Disconnecting : DeviceConnection()
data class Disconnected(val reason : String) : DeviceConnection()
