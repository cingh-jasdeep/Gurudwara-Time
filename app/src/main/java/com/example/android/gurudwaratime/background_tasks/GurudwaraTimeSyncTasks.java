package com.example.android.gurudwaratime.background_tasks;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.gurudwaratime.data.DataRepository;
import com.example.android.gurudwaratime.geofencing.GeofencingResultHelper;
import com.example.android.gurudwaratime.location_updates.LocationResultHelper;
import com.example.android.gurudwaratime.ui.status.StatusViewModel;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.maps.model.PlaceDetails;

import static com.example.android.gurudwaratime.data.Constants.EXTRA_FORCE_SYNC_NEW_LOCATION;
import static com.example.android.gurudwaratime.data.Constants.EXTRA_HANDLE_GEOFENCE_EVENT_REQUEST_ID;
import static com.example.android.gurudwaratime.data.Constants.EXTRA_HANDLE_GEOFENCE_EVENT_TRANSITION;
import static com.example.android.gurudwaratime.data.Constants.EXTRA_SYNC_NEW_LOCATION_JSON;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_SYNC_FLEX_TIME_LENGTH_IN_SECONDS;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_SYNC_START_TIME_LENGTH_IN_SECONDS;
import static com.example.android.gurudwaratime.data.Constants.INVALID_RINGER_MODE;
import static com.example.android.gurudwaratime.data.Constants.NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_SECONDS;
import static com.example.android.gurudwaratime.data.Constants.NEARBY_SYNC_FLEX_TIME_LENGTH_IN_SECONDS;
import static com.example.android.gurudwaratime.data.Constants.NEARBY_SYNC_REFRESH_TAG;
import static com.example.android.gurudwaratime.data.Constants.ON_DEMAND_CURRENT_GEOFENCE_HANDLE_TAG;
import static com.example.android.gurudwaratime.data.Constants.ON_DEMAND_NEARBY_SYNC_TAG;
import static com.example.android.gurudwaratime.data.Constants.REFRESH_GEOFENCE_SETUP_TAG;

public class GurudwaraTimeSyncTasks {

    private static final String TAG = GurudwaraTimeSyncTasks.class.getSimpleName();


    public static boolean scheduleOnDemandNearbySync(Context context,
                                                     @NonNull Location newLocation) {

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        Bundle jobExtrasBundle = new Bundle();
        jobExtrasBundle.putString(EXTRA_SYNC_NEW_LOCATION_JSON,
                LocationResultHelper.toJsonString(newLocation));

        Job syncJob = jobDispatcher.newJobBuilder()
                .setService(UpdateNearbyGurudwarasJobService.class)
                .setTag(ON_DEMAND_NEARBY_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setTrigger(Trigger.NOW)
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setExtras(jobExtrasBundle)
                .build();

        return (jobDispatcher.schedule(syncJob) == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS);
    }

    public static boolean scheduleNearbySyncRefresh(Context context,
                                                    @NonNull Location refreshLocation) {

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        Bundle jobExtrasBundle = new Bundle();
        jobExtrasBundle.putString(EXTRA_SYNC_NEW_LOCATION_JSON,
                LocationResultHelper.toJsonString(refreshLocation));

        //force refresh in recurring job to refresh places data
        jobExtrasBundle.putBoolean(EXTRA_FORCE_SYNC_NEW_LOCATION, true);

        Job syncJob = jobDispatcher.newJobBuilder()
                .setService(UpdateNearbyGurudwarasJobService.class)
                .setTag(NEARBY_SYNC_REFRESH_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_SECONDS,
                        NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_SECONDS +
                                NEARBY_SYNC_FLEX_TIME_LENGTH_IN_SECONDS))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setExtras(jobExtrasBundle)
                .build();

        return (jobDispatcher.schedule(syncJob) == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS);
    }

    public static boolean cancelAnyNearbySyncJobs(Context context) {

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        return (jobDispatcher.cancel(ON_DEMAND_NEARBY_SYNC_TAG)
                == FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS);
    }

    @WorkerThread
    static boolean performNearbySync(Context context,
                                     @NonNull String newLocationJson,
                                     boolean forceSync) {

        LocationResultHelper locationResultHelper = new LocationResultHelper(
                context, newLocationJson);

        if (locationResultHelper.isFreshLocation()
                || forceSync) {

            //fetch nearby Gurudwaras and sync with db
            DataRepository repo = DataRepository.getInstance(context.getApplicationContext());

            if (repo.fetchAndSaveNearbyGurudwarasSync(locationResultHelper.getLocation())) {

                //setup geofences
                repo.setupGeofences(context);//NEEDS REFRESH EVERY 24 HOURS
                scheduleGeofencesSetupRefresh(context);

                // Save the location results.
                locationResultHelper.saveSyncLocation();

                // Show notification results
//                locationResultHelper.showNotification();

                Location syncedLocation = repo.getLastSyncLocation(context);

                Log.i(TAG, "performNearbySync: successfully synced\n" + syncedLocation);

                Log.i(TAG, "performNearbySync: scheduling refresh places job");
                scheduleNearbySyncRefresh(context, syncedLocation);
            } else {
                return false;
            }
        }


        return true;
    }

    public static boolean scheduleOnDemandCurrentGeofenceHandle(Context context,
                                                                GeofencingEvent geofencingEvent) {

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        String geofenceRequestId =
                geofencingEvent.getTriggeringGeofences().get(0).getRequestId();

        int geofenceTransition =
                geofencingEvent.getGeofenceTransition();

        Bundle jobExtrasBundle = new Bundle();
        jobExtrasBundle.putString(EXTRA_HANDLE_GEOFENCE_EVENT_REQUEST_ID,
                geofenceRequestId);
        jobExtrasBundle.putInt(EXTRA_HANDLE_GEOFENCE_EVENT_TRANSITION,
                geofenceTransition);

        Job syncJob = jobDispatcher.newJobBuilder()
                .setService(HandleGeofenceEventJobService.class)
                .setTag(ON_DEMAND_CURRENT_GEOFENCE_HANDLE_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.NOW)
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setExtras(jobExtrasBundle)
                .build();

        return (jobDispatcher.schedule(syncJob) == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS);
    }

    public static boolean cancelAnyGeofenceJobs(Context context) {

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        return ((jobDispatcher.cancel(ON_DEMAND_CURRENT_GEOFENCE_HANDLE_TAG)
                == FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS) &&
                (jobDispatcher.cancel(REFRESH_GEOFENCE_SETUP_TAG)
                        == FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS));
    }

    @WorkerThread
    public static void handleGeofenceEvent(Context context,
                                           @NonNull String requestId,
                                           int geofenceTransition) {

        GeofencingResultHelper resultHelper = new GeofencingResultHelper(context);

        int requestedSilentMode =
                StatusViewModel.getAutoSilentRequestedMode(context);

        int currentRingerMode =
                resultHelper.getCurrentRingerMode();


        // Check which transition type has triggered this event
        switch (geofenceTransition) {

            case Geofence.GEOFENCE_TRANSITION_DWELL:

                Log.i(TAG, "handleGeofenceEvent: dwell");

                if (currentRingerMode == requestedSilentMode) {
                    //no need to do anything,
                    // may be going from one geofence to another or
                    // user is already in the requested silent mode, so no change needed
                    Log.i(TAG, "handleGeofenceEvent: requested == current ringer mode");
                    return;
                }

                //fetch current place name  and vicinity if available
                DataRepository repo = DataRepository.getInstance(context);
                PlaceDetails currPlaceDetails = repo.fetchPlaceById(requestId);
                //executes a network call, may or may not execute based on network connectivity

                String currPlaceName = null;
                String currPlaceVicinity;

                if (currPlaceDetails != null) {
                    currPlaceName = currPlaceDetails.name;
                    resultHelper.saveCurrentGeofencePlaceName(currPlaceName);
                    currPlaceVicinity = currPlaceDetails.vicinity;
                    resultHelper.saveCurrentGeofencePlaceVicinity(currPlaceVicinity);
                }

                //save current geofence place id and ringer
                resultHelper.saveCurrentGeofencePlaceId(requestId);
                resultHelper.saveCurrentGeofenceRestoreRingerMode(currentRingerMode);


                switch (requestedSilentMode) {
                    case AudioManager.RINGER_MODE_SILENT:
                        resultHelper.setCurrentRingerMode(requestedSilentMode);
                        resultHelper.sendNotification(requestedSilentMode, currPlaceName, true);
                        break;

                    case AudioManager.RINGER_MODE_VIBRATE:
                        resultHelper.setCurrentRingerMode(requestedSilentMode);
                        resultHelper.sendNotification(requestedSilentMode, currPlaceName, true);
                        break;

                    default:
                        Log.e(TAG, "handleGeofenceEvent: invalid requestedSilentMode: "
                                + requestedSilentMode);
                        return;

                }

                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "handleGeofenceEvent: exit");

                //check if there is saved current id only then display notification
                //this makes sure that if user exits earlier than dwell time
                // no ringer mode adjustments are made

                int currGeofenceRestoreRingerMode =
                        resultHelper.getCurrentGeofenceRestoreRingerMode();

                if (currGeofenceRestoreRingerMode == INVALID_RINGER_MODE) {
                    Log.i(TAG, "handleGeofenceEvent: no saved restore ringer mode");
                    return;
                }
                // if user made manual change then do nothing
                if (currentRingerMode != requestedSilentMode) {
                    Log.i(TAG, "handleGeofenceEvent: user made manual change");
                    return;
                }

                resultHelper.setCurrentRingerMode(currGeofenceRestoreRingerMode);
                resultHelper.sendNotification(currGeofenceRestoreRingerMode, null,
                        false);
                resultHelper.clearCurrentGeofenceData();
                break;
            default:
                // Log the error.
                Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));
                // No need to do anything else
                break;
        }

    }

    public static boolean scheduleGeofencesSetupRefresh(Context context) {

        Log.i(TAG, "scheduleGeofencesSetupRefresh: scheduling " +
                "geofence setup every 23 hours for expiry check");

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        Job syncJob = jobDispatcher.newJobBuilder()
                .setService(RefreshGeofencesSetupJobService.class)
                .setTag(REFRESH_GEOFENCE_SETUP_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(
                        Trigger.executionWindow(
                                GEOFENCE_SYNC_START_TIME_LENGTH_IN_SECONDS,
                                GEOFENCE_SYNC_START_TIME_LENGTH_IN_SECONDS +
                                        GEOFENCE_SYNC_FLEX_TIME_LENGTH_IN_SECONDS))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        return (jobDispatcher.schedule(syncJob) == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS);
    }


    public static void refreshGeofences(Context context) {
        //fetch nearby Gurudwaras and sync with db
        Log.i(TAG, "refreshGeofences: refreshing geofences for expiry check");
        DataRepository repo = DataRepository.getInstance(context.getApplicationContext());
        repo.setupGeofences(context);

    }

    public static void handleUndoSilentCurrentGeofence(Context context) {
        GeofencingResultHelper resultHelper = new GeofencingResultHelper(context);
        String currGeofencePlaceId = resultHelper.getCurrentGeofencePlaceId();

        if (TextUtils.isEmpty(currGeofencePlaceId)) {
            Log.i(TAG, "undoSilentAndClearCurrentGeofenceData: no current geofence " +
                    "place id found");
            return;
        }
        Log.i(TAG, "handleUndoSilentCurrentGeofence: placeId: " + currGeofencePlaceId);
        undoSilentAndClearCurrentGeofenceData(context, resultHelper);

    }

    public static void handleNeverSilentCurrentGeofence(Context context) {
        GeofencingResultHelper resultHelper = new GeofencingResultHelper(context);
        String currGeofencePlaceId = resultHelper.getCurrentGeofencePlaceId();

        if (TextUtils.isEmpty(currGeofencePlaceId)) {
            Log.i(TAG, "undoSilentAndClearCurrentGeofenceData: no current geofence " +
                    "place id found");
            return;
        }
        Log.i(TAG, "handleNeverSilentCurrentGeofence: placeId:" + currGeofencePlaceId);
        //add place id to exclude list
        DataRepository repo = DataRepository.getInstance(context);
        repo.markPlaceAsExcludedSync(currGeofencePlaceId);

        //refresh geofences
        repo.setupGeofences(context);

        //undo silent and clear data
        undoSilentAndClearCurrentGeofenceData(context, resultHelper);

    }

    private static void undoSilentAndClearCurrentGeofenceData(Context context,
                                                              GeofencingResultHelper resultHelper) {

        int requestedSilentMode =
                StatusViewModel.getAutoSilentRequestedMode(context);

        int currentRingerMode =
                resultHelper.getCurrentRingerMode();

        // if user made manual change then do nothing
        if (currentRingerMode != requestedSilentMode) {
            Log.i(TAG, "undoSilentAndClearCurrentGeofenceData: user made manual change");
            return;
        }

        int restoreRingerMode =
                resultHelper.getCurrentGeofenceRestoreRingerMode();

        resultHelper.setCurrentRingerMode(restoreRingerMode);
        resultHelper.clearNotification();
        resultHelper.clearCurrentGeofenceData();
    }

}
