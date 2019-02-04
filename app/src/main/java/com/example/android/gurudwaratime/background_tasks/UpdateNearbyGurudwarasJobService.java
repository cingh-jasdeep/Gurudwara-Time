package com.example.android.gurudwaratime.background_tasks;

import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.SimpleJobService;

import static com.example.android.gurudwaratime.data.Constants.EXTRA_FORCE_SYNC_NEW_LOCATION;
import static com.example.android.gurudwaratime.data.Constants.EXTRA_SYNC_NEW_LOCATION_JSON;

public class UpdateNearbyGurudwarasJobService extends SimpleJobService {
    private static final String TAG = UpdateNearbyGurudwarasJobService.class.getSimpleName();

    @Override
    public int onRunJob(JobParameters job) {

        Bundle jobExtras = job.getExtras();

        if (jobExtras != null) {
            String newLocationJson = jobExtras.getString(EXTRA_SYNC_NEW_LOCATION_JSON);

            if (newLocationJson != null && !newLocationJson.equals("")) {

                boolean forceSync = jobExtras.getBoolean(
                        EXTRA_FORCE_SYNC_NEW_LOCATION,
                        false);
                Log.i(TAG, "onRunJob: starting nearby sync");
                //synchronous task to sync nearby places with db
                boolean syncResult =
                        GurudwaraTimeSyncTasks.performNearbySync(getApplicationContext(),
                                newLocationJson, forceSync);

                if (syncResult) {
                    Log.i(TAG, "onRunJob: nearby sync complete");
                    return JobService.RESULT_SUCCESS;
                } else {
                    Log.i(TAG, "onRunJob: nearby sync failed");
                    return JobService.RESULT_FAIL_RETRY;
                }
            } else {
                Log.i(TAG, "onRunJob: invalid sync location");
            }
        } else {
            Log.i(TAG, "onRunJob: no job extras");
        }

        return JobService.RESULT_FAIL_NORETRY;
    }
}
