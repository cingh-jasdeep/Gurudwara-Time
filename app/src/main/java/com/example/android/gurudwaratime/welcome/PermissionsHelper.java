package com.example.android.gurudwaratime.welcome;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PermissionsHelper {

    public static final int REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 106;
    private static final String TAG = PermissionsHelper.class.getSimpleName();

    /**
     * Return the current state of the location permission.
     * source: https://github.com/googlecodelabs/background-location-updates-android-o
     */
    public static boolean checkLocationPermission(Context context) {
        int permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Return the current state of the dnd permission. (for api level >23)
     * source: https://github.com/udacity/AdvancedAndroid_Shushme
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkDNDPermission(Context context) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        return nm.isNotificationPolicyAccessGranted();
    }

    /**
     * Return the current state of the location and dnd permission.
     * source: https://github.com/googlecodelabs/background-location-updates-android-o
     */
    public static boolean checkLocationAndDndPermissions(Context context) {
        boolean locationPermissionState = checkLocationPermission(context);
        boolean dndPermissionState = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dndPermissionState = checkDNDPermission(context);
        }

        return locationPermissionState && dndPermissionState;
    }

    /**
     * Request location permissions
     * source: https://github.com/googlecodelabs/background-location-updates-android-o
     */
    public static void requestLocationPermission(Activity activity) {
        Log.i(TAG, "Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * Request dnd access permissions (for api level >23)
     * source: https://github.com/udacity/AdvancedAndroid_Shushme
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestDNDPermission(Context context) {
        Log.i(TAG, "opening dnd access");
        Intent intent = new Intent(
                android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
