package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.networks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.ellcie_healthy.common.networks.INetworkSubscriber
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger
import java.util.*


/**
 * Created by Remy on 09/01/2018.
 */
object NetworkManager {
    private val sSubscribers: ArrayList<INetworkSubscriber> = ArrayList<INetworkSubscriber>()
    private var state = false
    private val sReceiver: BroadcastReceiver = Receiver()
    private const val TAG = "NetworkManager"

    /**
     *
     * @param connected
     */
    private fun notifySubsribers(connected: Boolean) {
        Logger.d(TAG, "notifySubsribers")
        for (subscriber in sSubscribers) {
            subscriber.onNetworkStateChanged(connected)
        }
    }

    /**
     *
     */
    @JvmStatic
    fun removeAllSubscribers() {
        sSubscribers.clear()
    }

    /**
     * when a subscriber is added, his callback is immediately called with the current network state
     * @param context
     * @param subscriber
     */
    @JvmStatic
    fun addSubscriber(context: Context, subscriber: INetworkSubscriber) {
        Logger.d(TAG, "addSubscriber()")
        if (!sSubscribers.contains(subscriber)) {
            sSubscribers.add(subscriber)
        }
        subscriber.onNetworkStateChanged(networkIsAvailable(context))
    }

    /**
     * Remove a subscriber
     * @param subscriber
     */
    @JvmStatic
    fun removeSubscriber(subscriber: INetworkSubscriber) {
        Logger.d(TAG, "removeSubscriber()")
        sSubscribers.remove(subscriber)
    }

    /**
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun networkIsAvailable(context: Context): Boolean {
        Logger.d(TAG, "networkIsAvailable()")
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // This condition is for supporting os version lower than Android M. It is deprecated but for compatibility reason, this code is kept for the moment.
        val nwInfo = connectivityManager.activeNetworkInfo
        return nwInfo != null && nwInfo.isConnected
    }

    /**
     *
     * @param context
     */
    @JvmStatic
    fun register(context: Context) {
        Logger.d(TAG, "register()")
        context.registerReceiver(sReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    /**
     *
     * @param context
     */
    @JvmStatic
    fun unregister(context: Context) {
        Logger.d(TAG, "unregister()")
        context.unregisterReceiver(sReceiver)
    }

    class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Logger.d(TAG, "onReceived()")
            state = networkIsAvailable(context)
            Logger.d(TAG, "onReceived: state: $state")
            notifySubsribers(state)
        }
    }
}