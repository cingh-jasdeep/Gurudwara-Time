package com.example.android.gurudwaratime.ui.status;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.gurudwaratime.data.LiveSharedPreference;
import com.example.android.gurudwaratime.location_updates.LocationRequestHelper;
import com.example.android.gurudwaratime.location_updates.LocationResultHelper;

import static com.example.android.gurudwaratime.data.Constants.KEY_AUTO_SILENT_STATUS;
import static com.example.android.gurudwaratime.data.Constants.KEY_LAST_SYNC_LOCATION_JSON;

public class StatusViewModel extends AndroidViewModel {

    private static final String TAG = StatusViewModel.class.getSimpleName();


    private final LiveSharedPreference<Boolean> mAutoSilentRequestedStatus;
    private final LiveSharedPreference<String> mLastSyncLocationJson;
    private final MediatorLiveData<AutoSilentStatusStates> mAutoSilentStatus;


    public StatusViewModel(@NonNull Application application) {
        super(application);
        SharedPreferences sp =
                PreferenceManager.getDefaultSharedPreferences(application);

        mAutoSilentRequestedStatus =
                new LiveSharedPreference<>(KEY_AUTO_SILENT_STATUS, sp);

        mLastSyncLocationJson =
                new LiveSharedPreference<>(KEY_LAST_SYNC_LOCATION_JSON, sp);

        mAutoSilentStatus = new MediatorLiveData<>();

        mAutoSilentStatus.addSource(mAutoSilentRequestedStatus, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean sharedPrefValue) {
                onAutoSilentRequestStatusChange(sharedPrefValue);
            }
        });

    }

    private void onAutoSilentRequestStatusChange(@Nullable Boolean sharedPrefValue) {
        if (sharedPrefValue == null) {
            // not initialized yet, should turn on by default
            updateAutoSilentRequestedStatus(true);
        } else if (sharedPrefValue) {
            // auto silent on setup
            //start location updates
            StatusViewModel.startLocationUpdates(getApplication(), false);
            mAutoSilentStatus.setValue(AutoSilentStatusStates.INIT);
            //start observing last sync location
            mAutoSilentStatus.addSource(mLastSyncLocationJson, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String lastSyncLocationJson) {
                    onLastSyncLocationJsonChange(lastSyncLocationJson);
                }
            });
        } else {
            // auto silent off setup
            //stop location updates
            StatusViewModel.stopLocationUpdates(getApplication());
            //stop observing last sync location
            mAutoSilentStatus.removeSource(mLastSyncLocationJson);
            //update status to turned off
            mAutoSilentStatus.setValue(AutoSilentStatusStates.TURNED_OFF);

        }
    }

    private void onLastSyncLocationJsonChange(@Nullable String lastSyncLocationJson) {
        if (lastSyncLocationJson != null &&
                !lastSyncLocationJson.equals("")) {
            //update state
            if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.INIT) {
                mAutoSilentStatus.setValue(AutoSilentStatusStates.NO_LOCATION);
            }
        }
    }

    public static void startLocationUpdates(Context context, boolean forceStart) {
        LocationRequestHelper.startLocationUpdates(context, forceStart);
    }

    public static void stopLocationUpdates(Context context) {
        LocationRequestHelper.stopLocationUpdates(context);
    }

    public static boolean getAutoSilentRequestedStatus(Context context) {
        return AutoSilentRequestedStatusHelper.getStatus(context);
    }

    public static Location getLastSyncLocation(Context context) {
        return LocationResultHelper.getLastSyncLocation(context);
    }

    LiveData<AutoSilentStatusStates> getAutoSilentRequestedStatus() {
        return mAutoSilentStatus;
    }

    void updateAutoSilentRequestedStatus(boolean requestedAutoSilentStatus) {
        AutoSilentRequestedStatusHelper.setStatus(getApplication(), requestedAutoSilentStatus);
    }
}
