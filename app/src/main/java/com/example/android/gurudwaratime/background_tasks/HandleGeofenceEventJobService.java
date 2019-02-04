package com.example.android.gurudwaratime.background_tasks;

import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.SimpleJobService;

import static com.example.android.gurudwaratime.data.Constants.EXTRA_HANDLE_GEOFENCE_EVENT_REQUEST_ID;
import static com.example.android.gurudwaratime.data.Constants.EXTRA_HANDLE_GEOFENCE_EVENT_TRANSITION;
import static com.example.android.gurudwaratime.data.Constants.INVALID_GEOFENCE_TRANSITION;

public class HandleGeofenceEventJobService extends SimpleJobService {
    private static final String TAG = HandleGeofenceEventJobService.class.getSimpleName();

    @Override
    public int onRunJob(JobParameters job) {

        Bundle jobExtras = job.getExtras();

        if (jobExtras == null) {
            Log.e(TAG, "onRunJob: no job extras");
            return RESULT_FAIL_NORETRY;
        }

        String requestId = jobExtras.getString(EXTRA_HANDLE_GEOFENCE_EVENT_REQUEST_ID);
        if (requestId == null || requestId.equals("")) {
            Log.e(TAG, "onRunJob: invalid geofence request id");
            return RESULT_FAIL_NORETRY;
        }

        int geofenceTransition = jobExtras.getInt(
                EXTRA_HANDLE_GEOFENCE_EVENT_TRANSITION,
                INVALID_GEOFENCE_TRANSITION);

        if (geofenceTransition == INVALID_GEOFENCE_TRANSITION) {
            Log.e(TAG, "onRunJob: invalid geofence transition");
            return RESULT_FAIL_NORETRY;
        }

        //synchronous task to handle current geofence event
        GurudwaraTimeSyncTasks.handleGeofenceEvent(getApplicationContext(),
                requestId, geofenceTransition);

        Log.i(TAG, "onRunJob: handling geofence event complete");

        return RESULT_SUCCESS;

    }

}
