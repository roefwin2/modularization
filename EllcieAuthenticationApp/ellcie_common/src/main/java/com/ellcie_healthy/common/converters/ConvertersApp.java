package com.ellcie_healthy.common.converters;

import android.content.Context;

import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Yann on 03/07/2018.
 */

public class ConvertersApp {

    private static final String TAG = "ConvertersApp";


    /**
     * Return a date of the given timestamp. The date is formatted depending the phones local (example : 24/02/2018 (french) 02/24/2018 (english US))
     *
     */
    public static String convertTimestampToDate(Long timestamp, Context context) {
        if (timestamp == 0) {
            return "--";
        }
        Date date = new Date(timestamp);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        Logger.d(TAG, "convertTimestampToDate : " + dateFormat.format(date));
        return dateFormat.format(date);
    }


}
