package com.example.android.gurudwaratime.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlacesDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlacesDbEntity> placesDbEntityList);

    @Query("DELETE FROM " + DataNames.TABLE_NAME_PLACES)
    void deleteAll();

    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " ORDER BY " + DataNames.COLUMN_NEARBY_INDEX)
    LiveData<List<PlacesDbEntity>> getAllPlacesSorted();

    @Query("SELECT * FROM " + DataNames.TABLE_NAME_PLACES +
            " WHERE " + DataNames.COLUMN_IS_NEARBY + " = " + DataNames.TRUE +
            " ORDER BY " + DataNames.COLUMN_NEARBY_INDEX)
    LiveData<List<PlacesDbEntity>> getNearbyPlacesSorted();
}
