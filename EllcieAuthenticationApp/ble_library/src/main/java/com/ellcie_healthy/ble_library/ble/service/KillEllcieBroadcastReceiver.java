package com.ellcie_healthy.ble_library.ble.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ellcie_healthy.common.ServiceConstant;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.LogEnum;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger;

public class KillEllcieBroadcastReceiver extends BroadcastReceiver {
    public static final String BROADCAST_KILL_ACTION = "BROADCAST_KILL_ACTION";

    public static final String INTENT_FILTER_VALUE = "EH_KILL_ALL";

    public static final String CLASS = "BR_CLASS";



    private final static String TAG = "KillEllcieBroadcastReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.e(LogEnum.SIA005, "APP KILLED BY USER", TAG);
        final Intent broadcast = new Intent(BROADCAST_KILL_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);

        context.stopService(new Intent(context, EHBleForegroundService.class));


        if(intent.getExtras() != null &&
                intent.getExtras().get(CLASS) != null &&
                !intent.getBooleanExtra(ServiceConstant.KEEP_APP, false)){ // this condition is true if user wants to kill the service and finish the application from killing the service
            Class clazz = (Class) intent.getExtras().get(CLASS);

            Intent intentToKillApp = new Intent(context, clazz);


            intentToKillApp.addCategory(Intent.CATEGORY_HOME);
            intentToKillApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intentToKillApp.putExtra(ServiceConstant.EXIT_APP, true);
            context.startActivity(intentToKillApp);

        }

    }
}
