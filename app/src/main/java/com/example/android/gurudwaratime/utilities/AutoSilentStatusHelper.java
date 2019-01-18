package com.example.android.gurudwaratime.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Helper class
 * set or check current auto silent status
 * source: https://github.com/googlecodelabs/background-location-updates-android-o
 */
public class AutoSilentStatusHelper {

    public final static String KEY_AUTO_SILENT_STATUS = "auto-silent-status";

    public static void setStatus(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_AUTO_SILENT_STATUS, value)
                .apply();
    }

    public static boolean getStatus(Context context) {
        //by default we start by requesting location updates on first app launch
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_AUTO_SILENT_STATUS, true);
    }
}
