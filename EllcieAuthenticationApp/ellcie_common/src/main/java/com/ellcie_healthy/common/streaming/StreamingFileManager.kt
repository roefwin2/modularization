package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.streaming


@Suppress("unused")
class StreamingFileManager {
    var streamingState = StreamingState.NO_STREAMING

    enum class StreamingState {
        NO_STREAMING, STREAMING_TRIP, STREAMING_OPTICIAN, STREAMING_FALL
    }

    companion object {
        private const val TAG = "StreamingFileManager"
    }
}
