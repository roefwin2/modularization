package com.ellcie_healthy.common.networks

interface INetworkSubscriber {
    fun onNetworkStateChanged(state: Boolean)
}