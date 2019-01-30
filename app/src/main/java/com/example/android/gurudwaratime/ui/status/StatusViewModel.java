package com.example.android.gurudwaratime.ui.status;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.gurudwaratime.data.DataRepository;
import com.example.android.gurudwaratime.data.LiveSharedPreference;
import com.example.android.gurudwaratime.location_updates.LocationRequestHelper;

import static com.example.android.gurudwaratime.data.Constants.SYNC_EXPIRY_TIME_LENGTH_IN_MILLIS;

public class StatusViewModel extends AndroidViewModel {

    private static final String TAG = StatusViewModel.class.getSimpleName();

    private final DataRepository mRepo;
    private final LiveSharedPreference<Boolean> mAutoSilentRequestedStatus;
    private final LiveSharedPreference<Long> mLastSyncTimeInMillis;
    private final MediatorLiveData<AutoSilentStatusStates> mAutoSilentStatus;


    public StatusViewModel(@NonNull Application application) {
        super(application);
        mRepo = DataRepository.getInstance(getApplication());

        mAutoSilentRequestedStatus =
                mRepo.getAutoSilentRequestedStatusLive();

        mLastSyncTimeInMillis =
                mRepo.getLastSyncTimeInMillisLive();

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
            //remove source if present
            mAutoSilentStatus.removeSource(mLastSyncTimeInMillis);
            //start observing last sync location
            mAutoSilentStatus.addSource(mLastSyncTimeInMillis, new Observer<Long>() {
                @Override
                public void onChanged(@Nullable Long aLong) {
                    onLastSyncTimeChange(aLong);
                }
            });
        } else {
            // auto silent off setup
            //stop location updates
            StatusViewModel.stopLocationUpdates(getApplication());
            //stop observing last sync location
            mAutoSilentStatus.removeSource(mLastSyncTimeInMillis);
            //update status to turned off
            mAutoSilentStatus.setValue(AutoSilentStatusStates.TURNED_OFF);

        }
    }

    private void onLastSyncTimeChange(@Nullable Long lastSyncTimeInMillis) {
        if (lastSyncTimeInMillis != null) {
            boolean isFreshSync = (System.currentTimeMillis() - lastSyncTimeInMillis)
                    < SYNC_EXPIRY_TIME_LENGTH_IN_MILLIS;
            if (isFreshSync) {
                //update state
                if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.INIT) {
                    mAutoSilentStatus.setValue(AutoSilentStatusStates.NO_LOCATION);
                }
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

    LiveData<AutoSilentStatusStates> getAutoSilentStatus() {
        return mAutoSilentStatus;
    }

    void updateAutoSilentRequestedStatus(boolean requestedAutoSilentStatus) {
        AutoSilentRequestedStatusHelper.setStatus(getApplication(), requestedAutoSilentStatus);
    }
}
