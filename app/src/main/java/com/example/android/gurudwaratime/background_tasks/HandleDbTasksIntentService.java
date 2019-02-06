package com.example.android.gurudwaratime.background_tasks;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.android.gurudwaratime.data.Constants.ACTION_RESET_EXCLUDED_PLACES;

public class HandleDbTasksIntentService extends IntentService {

    private static final String TAG = HandleDbTasksIntentService.class.getSimpleName();

    public static void startActionResetExcludedPlaces(Context context) {
        Intent intent = new Intent(context, HandleDbTasksIntentService.class);
        intent.setAction(ACTION_RESET_EXCLUDED_PLACES);
        context.startService(intent);
    }

    public HandleDbTasksIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && !action.equals("")) {
                Log.i(TAG, "onHandleIntent: action: " + action);
                switch (action) {
                    case ACTION_RESET_EXCLUDED_PLACES:
                        GurudwaraTimeSyncTasks.handleResetExcludedPlaces
                                (getApplicationContext());
                        break;
                    default:
                        Log.e(TAG, "onHandleIntent: invalid action: " + action);
                }
            }

        }
    }
}
