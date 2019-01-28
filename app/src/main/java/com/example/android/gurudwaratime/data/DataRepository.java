package com.example.android.gurudwaratime.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.example.android.gurudwaratime.database.AppDatabase;
import com.example.android.gurudwaratime.database.PlaceDbEntity;
import com.example.android.gurudwaratime.database.PlacesDbDao;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.android.SphericalUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.Bounds;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.RankBy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.gurudwaratime.data.Constants.DEFAULT_GEOFENCE_RADIUS;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_AREA_VIEWPORT_FACTOR;
import static com.example.android.gurudwaratime.data.Constants.INVALID_INDEX;
import static com.example.android.gurudwaratime.data.Constants.KEYWORD_QUERY_DEFAULT_VALUE_PLACES_API;
import static com.example.android.gurudwaratime.data.Constants.PLACES_API_NEXT_PAGE_SLEEP_INTERVAL_MILLIS;
import static com.example.android.gurudwaratime.data.Constants.PLACES_API_PAGE_SIZE;

public class DataRepository {

    private static final String TAG = DataRepository.class.getSimpleName();

    private static DataRepository sInstance;

    private final GeoApiContext mGeoApiContext;

    private final PlacesDbDao mPlacesDbDao;


    private DataRepository(AppDatabase db) {
        mPlacesDbDao = db.placesDao();

        mGeoApiContext = new GeoApiContext.Builder()
                .apiKey(Constants.KEY_QUERY_DEFAULT_VALUE_PLACES_API)
                .build();
    }

    /**
     * Singleton pattern to get instance of Repository
     *
     * @param db app database instance
     * @return single instance of repo
     */
    public synchronized static DataRepository getInstance(
            AppDatabase db) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(db);
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
        Log.d(TAG, "Actively retrieving all places livedata from the DataBase in Repository");
        return mPlacesDbDao.getAllPlacesSorted();
    }

    /**
     * Returns all places sorted.
     *
     * @return list sorted places data
     */
    @WorkerThread
    List<PlaceDbEntity> getAllPlacesSortedSync() {
        Log.d(TAG, "Actively retrieving all places from the DataBase in Repository");
        return mPlacesDbDao.getAllPlacesSortedSync();
    }


    @WorkerThread
    public void insertAllSync(List<PlaceDbEntity> placeDbEntityList) {
        Log.d(TAG, "Actively inserting all places from the DataBase in Repository");
        mPlacesDbDao.insertAll(placeDbEntityList);
    }

    @WorkerThread
    public void deleteAllSync() {
        Log.d(TAG, "Actively deleting all places from the DataBase in Repository");
        mPlacesDbDao.deleteAll();
    }

    @WorkerThread
    public void replaceNearbySync(List<PlaceDbEntity> placeDbEntityList) {
        Log.d(TAG, "Actively replacing all places from the DataBase in Repository");
        mPlacesDbDao.replaceNearby(placeDbEntityList);
    }

    private List<PlaceDbEntity> getIncludedNearbyPlacesSync() {
        Log.d(TAG, "Actively retrieving included nearby places from the DataBase in Repository");
        return mPlacesDbDao.getIncludedNearbyPlacesSync();
    }

    private List<PlaceDbEntity> getExcludedNearbyPlacesSync() {
        Log.d(TAG, "Actively retrieving excluded nearby places from the DataBase in Repository");
        return mPlacesDbDao.getExcludedNearbyPlacesSync();
    }

    /**
     * @param location {@link android.location.Location} to perform nearby sync
     * @param context  {@link android.content.Context} to perform sync on
     * @return string of next page token if available otherwise null
     */
    @WorkerThread
    public boolean fetchAndSaveNearbyGurudwarasSync(@NonNull Location location, Context context) {
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

        Log.i(TAG, "fetchAndSaveNearbyGurudwarasSync: Save response \n"
                + parsedPlaces);

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
                        false,
                        true,
                        System.currentTimeMillis(),
                        i + pageCount * PLACES_API_PAGE_SIZE,
                        currApiPlace.name,
                        currApiPlace.vicinity,
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

                List<PlaceDbEntity> allIncludedNearbyPlaces = getIncludedNearbyPlacesSync();
                if (allIncludedNearbyPlaces != null && allIncludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity includedNearbyPlace :
                            allIncludedNearbyPlaces) {

                        int searchIndex = getCurrentIndex(includedNearbyPlace, parsedNearbyPlaces);
                        //if db place is in new nearby places
                        if (searchIndex != INVALID_INDEX) {
                            parsedNearbyPlaces.get(searchIndex)
                                    .setIncluded(true);
                        } else {
                            includedNearbyPlace.setNearby(false);
                            parsedNearbyPlaces.add(includedNearbyPlace);
                        }
                    }
                }


            } else {

                //if nearby list is empty retain all other places
                List<PlaceDbEntity> allExcludedNearbyPlaces = getExcludedNearbyPlacesSync();
                List<PlaceDbEntity> allIncludedNearbyPlaces = getIncludedNearbyPlacesSync();

                if (allExcludedNearbyPlaces != null && allExcludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity excludedNearbyPlace :
                            allExcludedNearbyPlaces) {
                        excludedNearbyPlace.setNearby(false);
                    }
                    parsedNearbyPlaces.addAll(allExcludedNearbyPlaces);
                }

                if (allIncludedNearbyPlaces != null && allIncludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity includedNearbyPlace :
                            allIncludedNearbyPlaces) {
                        includedNearbyPlace.setNearby(false);
                    }
                    parsedNearbyPlaces.addAll(allIncludedNearbyPlaces);
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

                List<PlaceDbEntity> allIncludedNearbyPlaces = getIncludedNearbyPlacesSync();
                if (allIncludedNearbyPlaces != null && allIncludedNearbyPlaces.size() > 0) {
                    for (PlaceDbEntity includedNearbyPlace :
                            allIncludedNearbyPlaces) {

                        int searchIndex = getCurrentIndex(includedNearbyPlace, parsedNearbyPlaces);
                        //if db place is in new nearby places
                        if (searchIndex != INVALID_INDEX) {
                            parsedNearbyPlaces.get(searchIndex)
                                    .setIncluded(true);
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
}
