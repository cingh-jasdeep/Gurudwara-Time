package com.example.android.gurudwaratime.ui.status;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.gurudwaratime.R;
import com.example.android.gurudwaratime.background_tasks.GurudwaraTimeSyncTasks;
import com.example.android.gurudwaratime.background_tasks.HandleAtLocationTasksIntentService;
import com.example.android.gurudwaratime.data.DataRepository;
import com.example.android.gurudwaratime.data.LiveSharedPreference;
import com.example.android.gurudwaratime.geofencing.GeofencingResultHelper;
import com.example.android.gurudwaratime.location_updates.LocationRequestHelper;
import com.example.android.gurudwaratime.ui.status.utils.AutoSilentRequestedStatusHelper;
import com.google.maps.PendingResult;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.model.PlaceDetails;

public class StatusViewModel extends AndroidViewModel {

    private static final String TAG = StatusViewModel.class.getSimpleName();

    private final DataRepository mRepo;
    private final LiveSharedPreference<Boolean> mAutoSilentRequestedStatus;
    private final LiveSharedPreference<Integer> mAutoSilentRequestedMode;
    private final LiveSharedPreference<String> mLastSyncLocationJson;
    private final LiveSharedPreference<String> mCurrGeofencePlaceId;


    private final MediatorLiveData<AutoSilentStatusStates> mAutoSilentStatus;
    private final MediatorLiveData<AutoSilentModeStates> mAutoSilentMode;

    private String mAtLocationName;
    private String mAtLocationVicinity;


    public StatusViewModel(@NonNull Application application) {
        super(application);
        mRepo = DataRepository.getInstance(getApplication());

        mAutoSilentRequestedStatus =
                mRepo.getAutoSilentRequestedStatusLive();

        mAutoSilentRequestedMode =
                mRepo.getAutoSilentRequestedModeLive();

        mLastSyncLocationJson =
                mRepo.getLastSyncLocationJsonLive();

        mCurrGeofencePlaceId =
                mRepo.getCurrGeofencePlaceIdLive();

        mAutoSilentStatus = new MediatorLiveData<>();
        mAutoSilentMode = new MediatorLiveData<>();

        mAutoSilentStatus.addSource(mAutoSilentRequestedStatus, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean sharedPrefValue) {
                onAutoSilentRequestedStatusChange(sharedPrefValue);
            }
        });

    }

    private void onAutoSilentRequestedStatusChange(@Nullable Boolean sharedPrefValue) {
        if (sharedPrefValue == null) {
            // not initialized yet, should turn on by default
            updateAutoSilentRequestedStatus(true);
        } else if (sharedPrefValue) {
            // auto silent on setup
            //start location updates
            StatusViewModel.startLocationUpdates(getApplication(), false);
            mAutoSilentStatus.setValue(AutoSilentStatusStates.INIT);

            //remove source if present
            mAutoSilentStatus.removeSource(mLastSyncLocationJson);
            //start observing last sync location
            mAutoSilentStatus.addSource(mLastSyncLocationJson, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String locationJson) {
                    onLastSyncLocationChange(locationJson);
                }
            });

            //start observing auto silent mode also
            mAutoSilentMode.removeSource(mAutoSilentRequestedMode);
            mAutoSilentMode.addSource(mAutoSilentRequestedMode, new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer sharedPrefValue) {
                    onAutoSilentRequestedModeChange(sharedPrefValue);
                }
            });

            //update status widget init
            GurudwaraTimeSyncTasks.scheduleOnDemandStatusWidgetUpdate(
                    getApplication().getApplicationContext());
        } else {
            // auto silent off setup
            //stop location updates
            StatusViewModel.stopLocationUpdates(getApplication());
            //stop observing current geofence place id
            mAutoSilentStatus.removeSource(mCurrGeofencePlaceId);
            //stop observing last sync location
            mAutoSilentStatus.removeSource(mLastSyncLocationJson);
            //stop observing auto silent mode also and reset value to disabled
            mAutoSilentMode.removeSource(mAutoSilentRequestedMode);
            mAutoSilentMode.setValue(AutoSilentModeStates.DISABLED);

            //cancel all background jobs
            StatusViewModel.cancelAllBackgroundJobs(getApplication());
            //clear last sync location because geofencing is not needed anymore
            //and creation of geofences is tied to background places update
            mRepo.clearLastSyncLocation(getApplication());
            //remove any geofences data and undo any silent modes
            mRepo.removeGeofences(getApplication().getApplicationContext());
            onUndoSilentRequest();

            //update status to turned off
            mAutoSilentStatus.setValue(AutoSilentStatusStates.TURNED_OFF);
            //update status widget turned _ off
            GurudwaraTimeSyncTasks.scheduleOnDemandStatusWidgetUpdate(
                    getApplication().getApplicationContext());

        }
    }

    private void onCurrentGeofencePlaceIdChange(String placeId) {
        if (!TextUtils.isEmpty(placeId)) {
            //we are in a geofence
            //get name and vicinity
            //change auto silent status to AT_LOCATION
            fetchPlaceDetailsAndUpdateStatus(placeId);
        } else {
            // we are not in a geofence
            // change status if we were AT_LOCATION
            if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.AT_LOCATION) {
                mAutoSilentStatus.setValue(AutoSilentStatusStates.NO_LOCATION);
            }
        }
    }

    private void fetchPlaceDetailsAndUpdateStatus(@NonNull String placeId) {
        GeofencingResultHelper resultHelper =
                new GeofencingResultHelper(getApplication().getApplicationContext());

        mAtLocationName = resultHelper.getCurrentGeofencePlaceName();

        if (TextUtils.isEmpty(mAtLocationName)) {
            //need to fetch details again
            //check for internet connection
            //src: https://github.com/google-developer-training/android-fundamentals-apps-v2/tree/master/WhoWroteIt
            ConnectivityManager connMgr = (ConnectivityManager)
                    getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }

            if (networkInfo != null && networkInfo.isConnected()) {
                //get place details async
                PlaceDetailsRequest request = mRepo.fetchPlaceByIdAsync(placeId);
                request.setCallback(new PendingResult.Callback<PlaceDetails>() {
                    @Override
                    public void onResult(PlaceDetails result) {
                        mAtLocationName = result.name;
                        mAtLocationVicinity = result.vicinity;
                        setAutoSilentStatusAtLocation(true);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "onFailure: cannot fetch place details at location", e);
                    }
                });


            } else { // no internet..show default values
                mAtLocationName = getApplication()
                        .getString(R.string.msg_place_name_default_at_location);
                mAtLocationVicinity = getApplication()
                        .getString(R.string.msg_place_vicinity_default_at_location);

                setAutoSilentStatusAtLocation(false);
            }

        } else {
            //details are available offline
            mAtLocationVicinity = resultHelper.getCurrentGeofencePlaceVicinity();
            setAutoSilentStatusAtLocation(false);
        }
    }

    private void setAutoSilentStatusAtLocation(boolean isAsync) {
        if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.NO_LOCATION) {
            if (isAsync) {
                mAutoSilentStatus.postValue(AutoSilentStatusStates.AT_LOCATION);
            } else {
                mAutoSilentStatus.setValue(AutoSilentStatusStates.AT_LOCATION);
            }
        }
    }

    private void onLastSyncLocationChange(@Nullable String locationJson) {
        if (locationJson != null) {
            //update state
            if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.INIT) {
                mAutoSilentStatus.setValue(AutoSilentStatusStates.NO_LOCATION);
                mAutoSilentStatus.removeSource(mCurrGeofencePlaceId);
                mAutoSilentStatus.addSource(mCurrGeofencePlaceId, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String sharedPrefValue) {
                        onCurrentGeofencePlaceIdChange(sharedPrefValue);
                    }
                });
            }
        }
    }

    private void onAutoSilentRequestedModeChange(@Nullable Integer sharedPrefValue) {
        if (sharedPrefValue == null) {
            // not initialized yet, should set to silent by default
            updateAutoSilentRequestedMode(AudioManager.RINGER_MODE_SILENT);

        } else if (sharedPrefValue == AudioManager.RINGER_MODE_SILENT) {
            mAutoSilentMode.setValue(AutoSilentModeStates.SILENT);

        } else if (sharedPrefValue == AudioManager.RINGER_MODE_VIBRATE) {
            mAutoSilentMode.setValue(AutoSilentModeStates.VIBRATE);
        }
    }

    public static void startLocationUpdates(Context context, boolean forceStart) {
        LocationRequestHelper.startLocationUpdates(context, forceStart);
    }

    public static void stopLocationUpdates(Context context) {
        LocationRequestHelper.stopLocationUpdates(context);
    }

    public static boolean getAutoSilentRequestedStatus(Context context) {
        Log.i(TAG, "getAutoSilentRequestedStatus from sharedpref");
        return AutoSilentRequestedStatusHelper.getStatus(context);
    }

    public static int getAutoSilentRequestedMode(Context context) {
        Log.i(TAG, "getAutoSilentRequestedMode from sharedpref");
        return AutoSilentRequestedStatusHelper.getMode(context);
    }

    LiveData<AutoSilentStatusStates> getAutoSilentStatus() {
        return mAutoSilentStatus;
    }

    LiveData<AutoSilentModeStates> getAutoSilentMode() {
        return mAutoSilentMode;
    }

    void updateAutoSilentRequestedStatus(boolean requestedAutoSilentStatus) {
        AutoSilentRequestedStatusHelper.setStatus(getApplication(), requestedAutoSilentStatus);
    }

    void updateAutoSilentRequestedMode(int ringerMode) {
        AutoSilentRequestedStatusHelper.setMode(getApplication(), ringerMode);
    }

    private static boolean cancelAllBackgroundJobs(Context context) {
        return GurudwaraTimeSyncTasks.cancelAnyNearbySyncJobs(context)
                && GurudwaraTimeSyncTasks.cancelAnyGeofenceJobs(context);
    }

    public void onUndoSilentRequest() {
        if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.AT_LOCATION) {
            HandleAtLocationTasksIntentService.startActionUndoSilentAtLocation(getApplication()
                    .getApplicationContext());
        }
    }

    public void onNeverSilentHereRequest() {
        if (mAutoSilentStatus.getValue() == AutoSilentStatusStates.AT_LOCATION) {
            HandleAtLocationTasksIntentService.startActionNeverSilentAtLocation(getApplication()
                    .getApplicationContext());
        }
    }

    public void resetExcludedPlaces() {
        mRepo.resetExcludedPlacesAsync(getApplication().getApplicationContext());
    }

    public enum AutoSilentStatusStates {
        INIT, NO_LOCATION, AT_LOCATION, TURNED_OFF
    }

    public enum AutoSilentModeStates {
        DISABLED, SILENT, VIBRATE
    }

    public String getAtLocationPlaceName() {
        return mAtLocationName;
    }

    public String getAtLocationPlaceVicinity() {
        return mAtLocationVicinity;
    }
}
