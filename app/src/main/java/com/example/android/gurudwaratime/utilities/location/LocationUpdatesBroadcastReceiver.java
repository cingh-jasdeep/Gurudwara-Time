package com.example.android.gurudwaratime.utilities.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.example.android.gurudwaratime.BuildConfig;
import com.example.android.gurudwaratime.status.StatusViewModel;
import com.example.android.gurudwaratime.welcome.PermissionsViewModel;
import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();

    public static final String ACTION_PROCESS_UPDATES = BuildConfig.APPLICATION_ID +
            ".ACTION_PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Log.i(TAG, "Received action: " + intent.getAction());
            switch (intent.getAction()) {
                case ACTION_PROCESS_UPDATES:
                    LocationResult result = LocationResult.extractResult(intent);
                    if (result != null) {
                        List<Location> locations = result.getLocations();
                        LocationResultHelper locationResultHelper = new LocationResultHelper(
                                context, locations);
                        // Save the location data to SharedPreferences.
                        locationResultHelper.saveResults();
                        // Show notification with the location data.
                        locationResultHelper.showNotification();
                        Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));
                    }
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    startLocationUpdatesOnBoot(context);
            }
        }
    }

    private void startLocationUpdatesOnBoot(Context context) {
        //if permissions are available and auto silent is enabled
        // then restart location updates on reboot
        if (PermissionsViewModel.checkLocationAndDndPermissions(context)
                && StatusViewModel.getAutoSilentStatusStatic(context)) {
            Log.i(TAG, "onReceive: Starting location updates on reboot");
            StatusViewModel.startLocationUpdates(context);
        }
    }
}
