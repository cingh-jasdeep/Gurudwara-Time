package com.example.android.gurudwaratime.background_tasks;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.android.gurudwaratime.data.Constants.ACTION_NEVER_SILENT_AT_LOCATION;
import static com.example.android.gurudwaratime.data.Constants.ACTION_UNDO_SILENT_AT_LOCATION;

public class HandleAtLocationTasksIntentService extends IntentService {

    private static final String TAG = HandleAtLocationTasksIntentService.class.getSimpleName();

    public HandleAtLocationTasksIntentService() {
        super(TAG);
    }

    public static void startActionUndoSilentAtLocation(Context context) {
        Intent intent = new Intent(context, HandleAtLocationTasksIntentService.class);
        intent.setAction(ACTION_UNDO_SILENT_AT_LOCATION);
        context.startService(intent);
    }

    public static void startActionNeverSilentAtLocation(Context context) {
        Intent intent = new Intent(context, HandleAtLocationTasksIntentService.class);
        intent.setAction(ACTION_NEVER_SILENT_AT_LOCATION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && !action.equals("")) {
                Log.i(TAG, "onHandleIntent: action: " + action);
                switch (action) {
                    case ACTION_UNDO_SILENT_AT_LOCATION:
                        GurudwaraTimeSyncTasks.handleUndoSilentCurrentGeofence
                                (getApplicationContext());
                        break;
                    case ACTION_NEVER_SILENT_AT_LOCATION:
                        GurudwaraTimeSyncTasks.handleNeverSilentCurrentGeofence
                                (getApplicationContext());
                        break;
                    default:
                        Log.e(TAG, "onHandleIntent: invalid action: " + action);
                }
            }

        }
    }
}
