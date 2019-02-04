package com.example.android.gurudwaratime.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.gurudwaratime.background_tasks.GurudwaraTimeSyncTasks;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * receiver for geofence events at interested location
 * source:
 * https://github.com/udacity/AdvancedAndroid_Shushme
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context current context to handle broadcast
     * @param intent intent for receiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the Geofence Event from the Intent sent through
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format("Error code : %d", geofencingEvent.getErrorCode()));
            return;
        } else if ((geofencingEvent.getTriggeringGeofences() == null)
                || (geofencingEvent.getTriggeringGeofences().size() == 0)) {
            Log.e(TAG, "onReceive: no triggering geofence!");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        // Check which transition type has triggered this event
        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "onReceive: geofence enter");
                return;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: geofence dwell");
                scheduleJobToHandleGeofenceEvent(context, geofencingEvent);

                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: geofence exit");
                scheduleJobToHandleGeofenceEvent(context, geofencingEvent);
                break;
            default:
                // Log the error.
                Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));
                // No need to do anything else
                break;
        }
    }

    private void scheduleJobToHandleGeofenceEvent(Context context, GeofencingEvent geofencingEvent) {
        boolean scheduleOk =
                GurudwaraTimeSyncTasks
                        .scheduleOnDemandCurrentGeofenceHandle(context, geofencingEvent);
        Log.i(TAG, "scheduleJobToHandleGeofenceEvent: successful? " + scheduleOk);
    }
}
