package com.example.android.gurudwaratime.ui.status.utils;

import android.content.Context;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import static com.example.android.gurudwaratime.data.Constants.KEY_AUTO_SILENT_REQUESTED_MODE;
import static com.example.android.gurudwaratime.data.Constants.KEY_AUTO_SILENT_REQUESTED_STATUS;

/**
 * Helper class
 * set or check current auto silent requested status and mode
 * source: https://github.com/googlecodelabs/background-location-updates-android-o
 */
public class AutoSilentRequestedStatusHelper {

    public static void setStatus(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_AUTO_SILENT_REQUESTED_STATUS, value)
                .apply();
    }

    public static boolean getStatus(Context context) {
        //by default we start by enabling auto silent on first app launch
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_AUTO_SILENT_REQUESTED_STATUS, true);
    }

    /**
     * helper function to set ringer mode for auto silent
     *
     * @param context context to store shared pref
     * @param mode    Ringer mode for auto silent
     *                can be
     *                AudioManager.RINGER_MODE_SILENT or
     *                AudioManager.RINGER_MODE_VIBRATE
     */
    public static void setMode(Context context, int mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(KEY_AUTO_SILENT_REQUESTED_MODE, mode)
                .apply();
    }

    /**
     * helper function to get ringer mode for auto silent
     *
     * @param context context to store shared pref
     * @return Ringer mode for auto silent
     * can be
     * AudioManager.RINGER_MODE_SILENT or
     * AudioManager.RINGER_MODE_VIBRATE
     */
    public static int getMode(Context context) {
        //by ringer mode silent is default
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_AUTO_SILENT_REQUESTED_MODE,
                        AudioManager.RINGER_MODE_SILENT);
    }
}
