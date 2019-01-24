package com.example.android.gurudwaratime.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class PlacesDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<PlaceDbEntity> placeDbEntityList);

    @Query("DELETE FROM " + DataNames.TABLE_NAME_PLACES)
    public abstract void deleteAll();

    @Query("DELETE FROM " + DataNames.TABLE_NAME_PLACES +
            " WHERE " + DataNames.COLUMN_IS_NEARBY + " = " + DataNames.TRUE)
    abstract void deleteNearby();

    @Transaction
    public void replaceNearby(List<PlaceDbEntity> placeDbEntityList) {
        deleteNearby();
        insertAll(placeDbEntityList);
    }

    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " ORDER BY " + DataNames.COLUMN_NEARBY_INDEX)
    public abstract LiveData<List<PlaceDbEntity>> getAllPlacesSorted();


    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " ORDER BY " + DataNames.COLUMN_NEARBY_INDEX)
    public abstract List<PlaceDbEntity> getAllPlacesSortedSync();

    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " WHERE " + DataNames.COLUMN_IS_NEARBY + " = " + DataNames.TRUE +
            " ORDER BY " + DataNames.COLUMN_NEARBY_INDEX)
    abstract LiveData<List<PlaceDbEntity>> getNearbyPlacesSorted();

    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " WHERE " + DataNames.COLUMN_IS_NEARBY + " = " + DataNames.TRUE +
            " AND " + DataNames.COLUMN_IS_INCLUDED + " = " + DataNames.TRUE)
    public abstract List<PlaceDbEntity> getIncludedNearbyPlacesSync();

    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " WHERE " + DataNames.COLUMN_IS_NEARBY + " = " + DataNames.TRUE +
            " AND " + DataNames.COLUMN_IS_EXCLUDED + " = " + DataNames.TRUE)
    public abstract List<PlaceDbEntity> getExcludedNearbyPlacesSync();
}
