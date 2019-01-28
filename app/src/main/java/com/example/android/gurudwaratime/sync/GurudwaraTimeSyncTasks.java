package com.example.android.gurudwaratime.sync;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.example.android.gurudwaratime.data.DataRepository;
import com.example.android.gurudwaratime.database.AppDatabase;
import com.example.android.gurudwaratime.location_updates.LocationResultHelper;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import static com.example.android.gurudwaratime.data.Constants.EXTRA_SYNC_NEW_LOCATION_JSON;
import static com.example.android.gurudwaratime.data.Constants.GURUDWARA_TIME_SYNC_TAG;

public class GurudwaraTimeSyncTasks {

    private static final String TAG = GurudwaraTimeSyncTasks.class.getSimpleName();


    public static boolean scheduleNearbySyncImmediately(Context context,
                                                        @NonNull Location newLocation) {

        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        Bundle jobExtrasBundle = new Bundle();
        jobExtrasBundle.putString(EXTRA_SYNC_NEW_LOCATION_JSON,
                LocationResultHelper.toJsonString(newLocation));

        Job syncJob = jobDispatcher.newJobBuilder()
                .setService(UpdateNearbyGurudwarasJobService.class)
                .setTag(GURUDWARA_TIME_SYNC_TAG)
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

    @WorkerThread
    static boolean performNearbySync(Context context,
                                     @NonNull String newLocationJson,
                                     boolean forceSync) {

        LocationResultHelper locationResultHelper = new LocationResultHelper(
                context, newLocationJson);

        if (locationResultHelper.isFreshLocation()
                || forceSync) {

            //fetch nearby Gurudwaras and sync with db
            DataRepository repo = DataRepository.getInstance(AppDatabase.getDatabase(context));

            if (repo.fetchAndSaveNearbyGurudwarasSync(locationResultHelper.getLocation(),
                    context)) {

                //todo setup geofences if still valid

                // Save the location results.
                locationResultHelper.saveSyncLocation();

                // Show notification results
                locationResultHelper.showNotification();

                Log.i(TAG, "performNearbySync: successfully synced\n" +
                        LocationResultHelper.getLastSyncLocation(context));
            } else {
                return false;
            }
        }


        return true;
    }


}
