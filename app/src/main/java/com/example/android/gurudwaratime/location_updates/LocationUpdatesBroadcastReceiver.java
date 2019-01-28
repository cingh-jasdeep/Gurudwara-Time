package com.example.android.gurudwaratime.location_updates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.example.android.gurudwaratime.BuildConfig;
import com.example.android.gurudwaratime.ui.status.StatusViewModel;
import com.example.android.gurudwaratime.sync.GurudwaraTimeSyncTasks;
import com.example.android.gurudwaratime.ui.welcome.PermissionsViewModel;
import com.google.android.gms.location.LocationResult;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATES = BuildConfig.APPLICATION_ID +
            ".ACTION_PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Log.i(TAG, "Received action: " + intent.getAction());
            switch (intent.getAction()) {
                case ACTION_PROCESS_UPDATES:
                    LocationResult result = LocationResult.extractResult(intent);
                    if (result != null) {
                        Location newLocation = result.getLastLocation();
                        if (newLocation != null) {
                            if (GurudwaraTimeSyncTasks.scheduleNearbySyncImmediately(
                                    context,
                                    newLocation)) {
                                Log.i(TAG, "onReceive: scheduled nearby sync successfully \n" +
                                        newLocation);
                            } else {
                                Log.i(TAG, "onReceive: could not schedule nearby sync!");
                            }
                        }
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
                && StatusViewModel.getAutoSilentRequestedStatus(context)) {
            Log.i(TAG, "onReceive: Starting location updates on reboot");
            StatusViewModel.startLocationUpdates(context, true);
        }
    }
}
