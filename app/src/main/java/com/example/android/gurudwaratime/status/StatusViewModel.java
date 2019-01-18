package com.example.android.gurudwaratime.status;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.gurudwaratime.utilities.AutoSilentStatusHelper;
import com.example.android.gurudwaratime.utilities.location.LocationRequestHelper;

public class StatusViewModel extends AndroidViewModel {

    private static final String TAG = StatusViewModel.class.getSimpleName();
    static final String KEY_AUTO_SILENT_STATUS = AutoSilentStatusHelper.KEY_AUTO_SILENT_STATUS;

    private MutableLiveData<Boolean> mAutoSilentStatus;

    public StatusViewModel(@NonNull Application application) {
        super(application);
        mAutoSilentStatus = new MutableLiveData<>();
        mAutoSilentStatus.setValue(
                AutoSilentStatusHelper.getStatus(application));
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

    public static void startLocationUpdates(Context context) {
        Log.i(TAG, "startLocationUpdates: starting location updates");
        LocationRequestHelper.startLocationUpdates(context);
    }

    public static void stopLocationUpdates(Context context) {
        Log.i(TAG, "stopLocationUpdates: stoping location updates");
        LocationRequestHelper.stopLocationUpdates(context);
    }

    public static boolean getAutoSilentStatusStatic(Context context) {
        return AutoSilentStatusHelper.getStatus(context);
    }
}
