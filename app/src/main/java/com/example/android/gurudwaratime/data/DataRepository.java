package com.example.android.gurudwaratime.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.example.android.gurudwaratime.background_tasks.HandleDbTasksIntentService;
import com.example.android.gurudwaratime.database.AppDatabase;
import com.example.android.gurudwaratime.database.PlaceDbEntity;
import com.example.android.gurudwaratime.database.PlacesDbDao;
import com.example.android.gurudwaratime.geofencing.GeofencingRequestHelper;
import com.example.android.gurudwaratime.location_updates.LocationResultHelper;
import com.example.android.gurudwaratime.ui.status.StatusViewModel;
import com.example.android.gurudwaratime.ui.welcome.PermissionsViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Tasks;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.android.SphericalUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.Bounds;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.RankBy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.android.gurudwaratime.data.Constants.DEFAULT_GEOFENCE_RADIUS;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_AREA_VIEWPORT_FACTOR;
import static com.example.android.gurudwaratime.data.Constants.INVALID_INDEX;
import static com.example.android.gurudwaratime.data.Constants.KEYWORD_QUERY_DEFAULT_VALUE_PLACES_API;
import static com.example.android.gurudwaratime.data.Constants.KEY_AUTO_SILENT_REQUESTED_MODE;
import static com.example.android.gurudwaratime.data.Constants.KEY_AUTO_SILENT_REQUESTED_STATUS;
import static com.example.android.gurudwaratime.data.Constants.KEY_CURRENT_GEOFENCE_PLACE_ID;
import static com.example.android.gurudwaratime.data.Constants.KEY_LAST_SYNC_LOCATION_JSON;
import static com.example.android.gurudwaratime.data.Constants.PLACES_API_NEXT_PAGE_SLEEP_INTERVAL_MILLIS;
import static com.example.android.gurudwaratime.data.Constants.PLACES_API_PAGE_SIZE;

public class DataRepository {

    private static final String TAG = DataRepository.class.getSimpleName();

    private static DataRepository sInstance;

    private final GeoApiContext mGeoApiContext;

    private final PlacesDbDao mPlacesDbDao;

    private final SharedPreferences mSharedPrefs;

    private final LiveSharedPreference<Boolean> mAutoSilentRequestedStatus;

    private final LiveSharedPreference<String> mLastSyncLocationJson;

    private final LiveSharedPreference<Integer> mAutoSilentRequestedMode;

    private final LiveSharedPreference<String> mCurrGeofencePlaceId;

//    private final LiveSharedPreference<Long> mLastSyncTimeInMillis;


    private DataRepository(Context context) {


        AppDatabase db = AppDatabase.getDatabase(context);

        mPlacesDbDao = db.placesDao();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        mAutoSilentRequestedStatus =
                new LiveSharedPreference<>(KEY_AUTO_SILENT_REQUESTED_STATUS, mSharedPrefs);

        mLastSyncLocationJson =
                new LiveSharedPreference<>(KEY_LAST_SYNC_LOCATION_JSON, mSharedPrefs);

        mAutoSilentRequestedMode =
                new LiveSharedPreference<>(KEY_AUTO_SILENT_REQUESTED_MODE, mSharedPrefs);

        mCurrGeofencePlaceId =
                new LiveSharedPreference<>(KEY_CURRENT_GEOFENCE_PLACE_ID, mSharedPrefs);

//        mLastSyncTimeInMillis =
//                new LiveSharedPreference<>(KEY_LAST_SYNC_TIME_IN_MILLIS, mSharedPrefs);

        mGeoApiContext = new GeoApiContext.Builder()
                .apiKey(Constants.KEY_QUERY_DEFAULT_VALUE_PLACES_API)
                .build();
    }

    /**
     * Singleton pattern to get instance of Repository
     *
     * @param context app context
     * @return single instance of repo
     */
    public synchronized static DataRepository getInstance(
            Context context) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(context);
                    Log.d(TAG, "Made new repository");
                }
            }
        }
        return sInstance;
    }


    /**
     * Returns all places sorted.
     *
     * @return live data of sorted places data
     */
    LiveData<List<PlaceDbEntity>> getAllPlacesSorted() {
        Log.d(TAG, "Actively retrieving all places livedata from the Database in Repository");
        return mPlacesDbDao.getAllPlacesSorted();
    }

    /**
     * Returns nearby places sorted list live data
     *
     * @return live data of sorted nearby places
     */
    public LiveData<List<PlaceDbEntity>> getNearbyPlacesSorted() {
        Log.d(TAG, "Actively retrieving nearby places list live data " +
                "from the Database in Repository");
        return mPlacesDbDao.getNearbyPlacesSorted();
    }

    /**
     * Returns all places sorted.
     *
     * @return list sorted places data
     */
    @WorkerThread
    List<PlaceDbEntity> getAllPlacesSortedSync() {
        Log.d(TAG, "Actively retrieving all places from the Database in Repository");
        return mPlacesDbDao.getAllPlacesSortedSync();
    }


    @WorkerThread
    public void insertAllSync(List<PlaceDbEntity> placeDbEntityList) {
        Log.d(TAG, "Actively inserting all places from the Database in Repository");
        mPlacesDbDao.insertAll(placeDbEntityList);
    }

    @WorkerThread
    public void deleteAllSync() {
        Log.d(TAG, "Actively deleting all places from the Database in Repository");
        mPlacesDbDao.deleteAll();
    }

    @WorkerThread
    public void replaceNearbySync(List<PlaceDbEntity> placeDbEntityList) {
        Log.d(TAG, "Actively replacing all places from the Database in Repository");
        mPlacesDbDao.replaceNearby(placeDbEntityList);
    }

    @WorkerThread
    public List<PlaceDbEntity> getNearbyPlacesExclusivelySync() {
        Log.d(TAG, "Actively retrieving included or " +
                "nearby places from the Database in Repository");
        return mPlacesDbDao.getNearbyPlacesExclusivelySync();
    }

    @WorkerThread
    private List<PlaceDbEntity> getExcludedNearbyPlacesSync() {
        Log.d(TAG, "Actively retrieving excluded nearby places " +
                "from the Database in Repository");
        return mPlacesDbDao.getExcludedNearbyPlacesSync();
    }

    private void deleteExcludedPlacesExclusive() {
        Log.i(TAG, "deleteExcludedPlacesExclusively");
        mPlacesDbDao.deleteExcludedPlacesExclusively();
    }

    private void resetExcludedFlagForAllPlaces() {
        Log.i(TAG, "resetExcludedFlagForAllPlaces");
        mPlacesDbDao.resetExcludedFlagForAllPlaces();
    }


    public PlaceDetails fetchPlaceById(String requestedPlaceId) {
        if (requestedPlaceId != null && !requestedPlaceId.equals("")) {
            PlaceDetailsRequest request =
                    PlacesApi.placeDetails(getGeoApiContext(), requestedPlaceId);
            try {
                return request.fields(PlaceDetailsRequest.FieldMask.NAME,
                        PlaceDetailsRequest.FieldMask.VICINITY).await();

            } catch (ApiException e) {
                Log.e(TAG, "fetchPlaceById: ", e);
                return null;
            } catch (InterruptedException e) {
                Log.e(TAG, "fetchPlaceById: ", e);
                return null;
            } catch (IOException e) {
                Log.e(TAG, "fetchPlaceById: ", e);
                return null;
            }
        }
        return null;
    }

    public PlaceDetailsRequest fetchPlaceByIdAsync(@NonNull String requestedPlaceId) {
        PlaceDetailsRequest request =
                PlacesApi.placeDetails(getGeoApiContext(), requestedPlaceId);
        return request.fields(PlaceDetailsRequest.FieldMask.NAME,
                PlaceDetailsRequest.FieldMask.VICINITY);
    }

    /**
     * @param location {@link android.location.Location} to perform nearby sync
     * @return string of next page token if available otherwise null
     */
    @WorkerThread
    public boolean fetchAndSaveNearbyGurudwarasSync(@NonNull Location location) {
        Log.i(TAG, "fetchAndSaveNearbyGurudwarasSync: fetching nearby Gurudwaras " +
                "using places api in the repository");

        com.google.maps.model.LatLng currLatLng =
                new com.google.maps.model.LatLng(location.getLatitude(),
                        location.getLongitude());

        NearbySearchRequest nearbySearchRequest =
                PlacesApi.nearbySearchQuery(getGeoApiContext(),
                        currLatLng);
        int pageCount = 0;
        try {
            PlacesSearchResponse searchResponse =
                    nearbySearchRequest
                            .type(PlaceType.PLACE_OF_WORSHIP)
                            .keyword(KEYWORD_QUERY_DEFAULT_VALUE_PLACES_API)
                            .rankby(RankBy.DISTANCE).await();

            processPlacesSearchResponse(searchResponse, true, pageCount);

            String nextPageToken = searchResponse.nextPageToken;
            int failedCount = 0;

            while (nextPageToken != null
                    && !nextPageToken.equals("")) {

                Thread.sleep(PLACES_API_NEXT_PAGE_SLEEP_INTERVAL_MILLIS);

                nearbySearchRequest =
                        PlacesApi.nearbySearchQuery(getGeoApiContext(),
                                currLatLng);
                try {
                    searchResponse =
                            nearbySearchRequest
                                    .pageToken(nextPageToken)
                                    .await();

                } catch (ApiException e) {
                    Log.e(TAG, "fetchAndSaveNearbyGurudwarasSync: ", e);
                    failedCount++;
                    if (failedCount > 3) {
                        return false;
                    } else {
                        continue;
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "fetchAndSaveNearbyGurudwarasSync: ", e);
                    failedCount++;
                    if (failedCount > 3) {
                        return false;
                    } else {
                        continue;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "fetchAndSaveNearbyGurudwarasSync: ", e);
                    failedCount++;
                    if (failedCount > 3) {
                        return false;
                    } else {
                        continue;
                    }
                }

                pageCount++;
                processPlacesSearchResponse(searchResponse, false, pageCount);

                nextPageToken = searchResponse.nextPageToken;
            }

            return true;

        } catch (ApiException e) {
            Log.e(TAG, "fetchAndSaveNearbyGurudwarasSync: ", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "fetchAndSaveNearbyGurudwarasSync: ", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "fetchAndSaveNearbyGurudwarasSync: ", e);
            return false;
        }

    }

    private void processPlacesSearchResponse(PlacesSearchResponse searchResponse,
                                             boolean firstResponse, int pageCount) {

        List<PlaceDbEntity> parsedPlaces = new ArrayList<>();

        parseApiPlaces(searchResponse.results, parsedPlaces, pageCount);
        adjustIncludeExcludePlaces(parsedPlaces, firstResponse);

        if (firstResponse) {
            replaceNearbySync(parsedPlaces);
        } else {
            insertAllSync(parsedPlaces);
        }

    }

    private void parseApiPlaces(PlacesSearchResult[] placeResults,
                                List<PlaceDbEntity> parsedPlaces, int pageCount) {

        if (placeResults != null && placeResults.length > 0) {

            for (int i = 0; i < placeResults.length; i++) {
                PlacesSearchResult currApiPlace = placeResults[i];

                float geofenceRadius =
                        calculateGeofenceRadius(currApiPlace);

                parsedPlaces.add(new PlaceDbEntity(
                        currApiPlace.placeId,
                        false,
                        true,
                        System.currentTimeMillis(),
                        i + pageCount * PLACES_API_PAGE_SIZE,
                        currApiPlace.geometry.location.lat,
                        currApiPlace.geometry.location.lng,
                        geofenceRadius));
            }

        }
    }

    private float calculateGeofenceRadius(PlacesSearchResult currApiPlace) {
        List<LatLng> viewPortPath = new ArrayList<>();
        Bounds currViewPort = currApiPlace.geometry.viewport;

        if (currViewPort != null) {
            com.google.maps.model.LatLng currNorthEast = currViewPort.northeast;
            com.google.maps.model.LatLng currSouthWest = currViewPort.southwest;

            if (currNorthEast != null && currSouthWest != null) {

                viewPortPath.add(
                        new LatLng(currNorthEast.lat, currNorthEast.lng));
                viewPortPath.add(
                        new LatLng(currSouthWest.lat, currNorthEast.lng));
                viewPortPath.add(
                        new LatLng(currSouthWest.lat, currSouthWest.lng));
                viewPortPath.add(
                        new LatLng(currNorthEast.lat, currSouthWest.lng));
                viewPortPath.add(
                        new LatLng(currNorthEast.lat, currNorthEast.lng));

                double viewPortArea = SphericalUtil.computeArea(viewPortPath);
                double geofenceArea = viewPortArea * GEOFENCE_AREA_VIEWPORT_FACTOR;

                return computeRadius(geofenceArea);
            }

        }

        return DEFAULT_GEOFENCE_RADIUS;
    }

    private float computeRadius(double geofenceArea) {
        return (float) Math.sqrt(geofenceArea / Math.PI);
    }


    private void adjustIncludeExcludePlaces(@NonNull List<PlaceDbEntity> parsedNearbyPlaces,
                                            boolean firstResponse) {

        if (firstResponse) {
            // next page of results, current nearby places will be deleted
            // so need to store nearby include/exclude places again
            // need to adjust current include/exclude places in the new list

            if (parsedNearbyPlaces.size() > 0) {
                //if nearby list is not empty retain all other places
                //then account for include and exclude

                List<PlaceDbEntity> allExcludedNearbyPlaces = getExcludedNearbyPlacesSync();
                if (allExcludedNearbyPlaces != null && allExcludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity excludedNearbyPlace :
                            allExcludedNearbyPlaces) {

                        int searchIndex = getCurrentIndex(excludedNearbyPlace, parsedNearbyPlaces);
                        //if db place is in new nearby places
                        if (searchIndex != INVALID_INDEX) {
                            parsedNearbyPlaces.get(searchIndex)
                                    .setExcluded(true);
                        } else {
                            excludedNearbyPlace.setNearby(false);
                            parsedNearbyPlaces.add(excludedNearbyPlace);
                        }
                    }
                }


            } else {

                //if nearby list is empty retain all other places
                List<PlaceDbEntity> allExcludedNearbyPlaces = getExcludedNearbyPlacesSync();

                if (allExcludedNearbyPlaces != null && allExcludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity excludedNearbyPlace :
                            allExcludedNearbyPlaces) {
                        excludedNearbyPlace.setNearby(false);
                    }
                    parsedNearbyPlaces.addAll(allExcludedNearbyPlaces);
                }

            }
        } else {
            // next page of results, current nearby places will not be deleted
            // so no need to store nearby include/exclude places
            // but need to adjust current include/exclude places in the new list

            if (parsedNearbyPlaces.size() > 0) {
                //if nearby list is not empty
                //then account for include and exclude

                List<PlaceDbEntity> allExcludedNearbyPlaces = getExcludedNearbyPlacesSync();
                if (allExcludedNearbyPlaces != null && allExcludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity excludedNearbyPlace :
                            allExcludedNearbyPlaces) {

                        int searchIndex = getCurrentIndex(excludedNearbyPlace, parsedNearbyPlaces);
                        //if db place is in new nearby places
                        if (searchIndex != INVALID_INDEX) {
                            parsedNearbyPlaces.get(searchIndex)
                                    .setExcluded(true);
                        }
                    }
                }


            }
        }


    }


    private int getCurrentIndex(PlaceDbEntity searchEntity, List<PlaceDbEntity> allEntities) {
        if (allEntities != null && allEntities.size() > 0) {
            for (int i = 0; i < allEntities.size(); i++) {
                PlaceDbEntity currentEntity = allEntities.get(i);
                if (currentEntity.getPlaceId().equals(searchEntity.getPlaceId())) {
                    return i;
                }
            }
        }
        return INVALID_INDEX;
    }

    public GeoApiContext getGeoApiContext() {
        return mGeoApiContext;
    }

    public void clearLastSyncLocation(Context context) {
        Log.i(TAG, "clearLastSyncLocation: clearing last sync location from shared prefs");
        LocationResultHelper.clearLastSyncLocation(context);
    }

    public LiveSharedPreference<Boolean> getAutoSilentRequestedStatusLive() {
        Log.i(TAG, "getAutoSilentRequestedStatusLive");
        return mAutoSilentRequestedStatus;
    }

    public LiveSharedPreference<String> getLastSyncLocationJsonLive() {
        Log.i(TAG, "getLastSyncLocationJsonLive");
        return mLastSyncLocationJson;
    }

    public LiveSharedPreference<Integer> getAutoSilentRequestedModeLive() {
        Log.i(TAG, "getAutoSilentRequestedModeLive");
        return mAutoSilentRequestedMode;
    }

    public LiveSharedPreference<String> getCurrGeofencePlaceIdLive() {
        Log.i(TAG, "getCurrGeofencePlaceIdLive");
        return mCurrGeofencePlaceId;
    }

    public Location getLastSyncLocation(Context context) {
        Log.i(TAG, "getLastSyncLocation: getting last sync location from shared prefs");
        return LocationResultHelper.getLastSyncLocation(context);
    }

    @WorkerThread
    public void setupGeofences(Context context) {
        //setup geofences if still permissions are still valid and
        //auto silent is on
        Log.i(TAG, "setupGeofences: setting up geofences");
        if (PermissionsViewModel.checkLocationAndDndPermissions(context)
                && StatusViewModel.getAutoSilentRequestedStatus(context)) {
            GeofencingRequestHelper geofencingRequestHelper =
                    new GeofencingRequestHelper(context);
            List<PlaceDbEntity> geofencePlacesToSetup = getNearbyPlacesExclusivelySync();
            Log.i(TAG, "setupGeofences: " + geofencePlacesToSetup.size() +
                    " geofences");
            geofencingRequestHelper.updateGeofencesList(geofencePlacesToSetup);
            try {
                Tasks.await(geofencingRequestHelper.unRegisterAllGeofences());
                Tasks.await(geofencingRequestHelper.registerAllGeofences());//NEEDS REFRESH
            } catch (ExecutionException e) {
                Log.e(TAG, "setupGeofences: ", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "setupGeofences: ", e);
            }
        }
    }

    public void removeGeofences(Context context) {
        //remove geofences
        Log.i(TAG, "removeGeofences: removing geofences");
        GeofencingRequestHelper geofencingRequestHelper =
                new GeofencingRequestHelper(context);
        if (PermissionsViewModel.checkLocationAndDndPermissions(context)) {
            geofencingRequestHelper.unRegisterAllGeofences();
        }

    }

    /**
     * must be a maximum of 20 places excluded
     *
     * @param placeId placeid to exclude
     */
    @WorkerThread
    public void markPlaceAsExcludedSync(@NonNull String placeId) {
        Log.i(TAG, "markPlaceAsExcludedSync: placeId: " + placeId);
        mPlacesDbDao.markPlaceAsExcluded(placeId);
    }

    /**
     * resets all excluded places
     */
    public void resetExcludedPlacesAsync(Context context) {
        Log.i(TAG, "resetExcludedPlacesAsync");
        HandleDbTasksIntentService.startActionResetExcludedPlaces(context);
    }

    @WorkerThread
    public void resetExcludedPlacesSync() {
        deleteExcludedPlacesExclusive();
        resetExcludedFlagForAllPlaces();
    }


//    public LiveSharedPreference<Long> getLastSyncTimeInMillisLive() {
//        return mLastSyncTimeInMillis;
//    }
}
