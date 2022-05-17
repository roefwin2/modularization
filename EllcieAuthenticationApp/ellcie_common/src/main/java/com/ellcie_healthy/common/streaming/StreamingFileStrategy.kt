package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.streaming

interface StreamingFileStrategy {
    fun isStreamingOnGoing(): Boolean

    fun initStreamingFile()

    fun writeStreamingData(data: ByteArray?)

    fun onStreamingStopped()
}