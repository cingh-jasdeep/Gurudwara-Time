package com.example.android.gurudwaratime.status;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.example.android.gurudwaratime.location_updates.LocationRequestHelper;
import com.example.android.gurudwaratime.location_updates.LocationResultHelper;

public class StatusViewModel extends AndroidViewModel {

    private static final String TAG = StatusViewModel.class.getSimpleName();

    private MutableLiveData<Boolean> mAutoSilentStatus;

    public StatusViewModel(@NonNull Application application) {
        super(application);
        mAutoSilentStatus = new MutableLiveData<>();
        mAutoSilentStatus.setValue(
                AutoSilentStatusHelper.getStatus(application));
    }

    public static void startLocationUpdates(Context context, boolean forceStart) {
        LocationRequestHelper.startLocationUpdates(context, forceStart);
    }

    public static void stopLocationUpdates(Context context) {
        LocationRequestHelper.stopLocationUpdates(context);
    }

    public static boolean getAutoSilentStatus(Context context) {
        return AutoSilentStatusHelper.getStatus(context);
    }

    public static Location getLastSyncLocation(Context context) {
        return LocationResultHelper.getLastSyncLocation(context);
    }

    void onAutoSilentStatusChanged() {
        mAutoSilentStatus.setValue(
                AutoSilentStatusHelper.getStatus(getApplication()));
    }

    MutableLiveData<Boolean> getAutoSilentStatus() {
        return mAutoSilentStatus;
    }

    void updateAutoSilentStatus(boolean requestedAutoSilentStatus) {
        AutoSilentStatusHelper.setStatus(getApplication(), requestedAutoSilentStatus);
    }
}
