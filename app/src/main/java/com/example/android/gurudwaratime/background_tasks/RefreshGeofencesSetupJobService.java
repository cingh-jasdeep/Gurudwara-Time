package com.example.android.gurudwaratime.background_tasks;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.SimpleJobService;

public class RefreshGeofencesSetupJobService extends SimpleJobService {
    @Override
    public int onRunJob(JobParameters job) {
        GurudwaraTimeSyncTasks.refreshGeofences(getApplicationContext());
        return RESULT_SUCCESS;
    }
}
