package com.example.android.gurudwaratime.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.gurudwaratime.database.PlaceDbEntity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_DEFAULT_EXPIRY_TIME_LENGTH_IN_MILLIS;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_DEFAULT_LOITERING_TIME_LENGTH_IN_MILLIS;

/**
 * helper class to register and unregister geofences
 * source:
 * https://github.com/udacity/AdvancedAndroid_Shushme
 */

public class GeofencingRequestHelper {

    // Constants
    public static final String TAG = GeofencingRequestHelper.class.getSimpleName();

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;

    public GeofencingRequestHelper(Context context) {
        mContext = context;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    /***
     * Registers the list of Geofences specified in mGeofenceList with Google Place Services
     * Uses {@link #getGeofencingRequest} to get the list of Geofences to be registered
     * Uses {@link #getGeofencePendingIntent} to get the pending intent to launch the IntentService
     * when the Geofence is triggered
     * @return Task used to create geofences
     */
    public Task<Void> registerAllGeofences() {
        // Check that the list has Geofences in it
        if (mGeofenceList == null || mGeofenceList.size() == 0) {
            return null;
        }
        try {
            return LocationServices.getGeofencingClient(mContext).addGeofences(
                    getGeofencingRequest(),
                    getGeofencePendingIntent());
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
        return null;
    }

    /***
     * Unregisters all the Geofences created by this app from Google Place Services
     * Uses {@code #mGoogleApiClient} to connect to Google Place Services
     * Uses {@link #getGeofencePendingIntent} to get the pending intent passed when
     * registering the Geofences in the first place
     */
    public Task<Void> unRegisterAllGeofences() {
        try {
            return LocationServices.getGeofencingClient(mContext).removeGeofences(
                    // This is the same pending intent that was used in registerGeofences
                    getGeofencePendingIntent()
            );
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
        return null;
    }


    /***
     * Updates the local ArrayList of Geofences using data from the passed in list
     * Uses the Place ID defined by the API as the Geofence object Id
     *
     * @param places the PlaceBuffer result of the getPlaceById call
     */
    public void updateGeofencesList(@NonNull List<PlaceDbEntity> places) {
        mGeofenceList = new ArrayList<>();
        if (places.size() == 0) return;
        for (PlaceDbEntity place : places) {
            // Read the place information from the DB cursor
            String placeUID = place.getPlaceId();
            double placeLat = place.getCachedPlaceLat();
            double placeLng = place.getCachedPlaceLong();
            // Build a Geofence object
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCE_DEFAULT_EXPIRY_TIME_LENGTH_IN_MILLIS)
                    .setCircularRegion(placeLat, placeLng, place.getCachedPlaceGeofenceRadius())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_DWELL |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(GEOFENCE_DEFAULT_LOITERING_TIME_LENGTH_IN_MILLIS)
                    .build();
            // Add it to the list
            mGeofenceList.add(geofence);
        }
    }

    /***
     * Creates a GeofencingRequest object using the mGeofenceList ArrayList of Geofences
     * Used by {@code #registerGeofences}
     *
     * @return the GeofencingRequest object
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /***
     * Creates a PendingIntent object using the GeofenceTransitionsIntentService class
     * Used by {@code #registerGeofences}
     *
     * @return the PendingIntent object
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, PendingIntent.
                        FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

}
