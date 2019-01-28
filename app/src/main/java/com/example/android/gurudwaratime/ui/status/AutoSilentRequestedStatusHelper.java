package com.example.android.gurudwaratime.ui.status;

import android.content.Context;
import android.preference.PreferenceManager;

import static com.example.android.gurudwaratime.data.Constants.KEY_AUTO_SILENT_STATUS;

/**
 * Helper class
 * set or check current auto silent status
 * source: https://github.com/googlecodelabs/background-location-updates-android-o
 */
public class AutoSilentRequestedStatusHelper {

    public static void setStatus(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_AUTO_SILENT_STATUS, value)
                .apply();
    }

    public static boolean getStatus(Context context) {
        //by default we start by enabling auto silent on first app launch
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_AUTO_SILENT_STATUS, true);
    }
}
