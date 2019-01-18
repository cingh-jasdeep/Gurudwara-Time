package com.example.android.gurudwaratime.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.support.annotation.WorkerThread;

import java.util.List;

public class DataRepository {
    private PlacesDbDao mPlacesDbDao;
    private LiveData<List<PlacesDbEntity>> mNearbyPlacesSorted;

    DataRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mPlacesDbDao = db.placesDao();
        mNearbyPlacesSorted = mPlacesDbDao.getAllPlacesSorted();
    }

    LiveData<List<PlacesDbEntity>> getAllPlacesSorted() {
        return mNearbyPlacesSorted;
    }

    @WorkerThread
    public void insertAllSync(List<PlacesDbEntity> placesDbEntityList) {
        mPlacesDbDao.insertAll(placesDbEntityList);
    }

    @WorkerThread
    public void deleteAllSync() {
        mPlacesDbDao.deleteAll();
    }
}
