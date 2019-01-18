package com.example.android.gurudwaratime.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = DataNames.TABLE_NAME_PLACES)
public class PlacesDbEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = DataNames.COLUMN_PLACE_ID)
    private String placeId;

    @ColumnInfo(name = DataNames.COLUMN_IS_INCLUDED)
    private boolean isIncluded;

    @ColumnInfo(name = DataNames.COLUMN_IS_EXCLUDED)
    private boolean isExcluded;

    @ColumnInfo(name = DataNames.COLUMN_IS_NEARBY)
    private boolean isNearby;

    @ColumnInfo(name = DataNames.COLUMN_UPDATED_AT)
    private long updatedAt;

    @ColumnInfo(name = DataNames.COLUMN_NEARBY_INDEX)
    private int nearbyIndex;

    @ColumnInfo(name = DataNames.COLUMN_CACHED_PLACE_NAME)
    private String cachedPlaceName;

    @ColumnInfo(name = DataNames.COLUMN_CACHED_PLACE_LAT)
    private double cachedPlaceLat;

    @ColumnInfo(name = DataNames.COLUMN_CACHED_PLACE_LONG)
    private double cachedPlaceLong;

    public PlacesDbEntity(@NonNull String placeId, boolean isIncluded, boolean isExcluded,
                          boolean isNearby, long updatedAt, int nearbyIndex, String cachedPlaceName, double cachedPlaceLat, double cachedPlaceLong) {
        this.placeId = placeId;
        this.isIncluded = isIncluded;
        this.isExcluded = isExcluded;
        this.isNearby = isNearby;
        this.updatedAt = updatedAt;
        this.nearbyIndex = nearbyIndex;
        this.cachedPlaceName = cachedPlaceName;
        this.cachedPlaceLat = cachedPlaceLat;
        this.cachedPlaceLong = cachedPlaceLong;
    }

    @NonNull
    public String getPlaceId() {
        return placeId;
    }

    public boolean isIncluded() {
        return isIncluded;
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public boolean isNearby() {
        return isNearby;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public int getNearbyIndex() {
        return nearbyIndex;
    }

    public String getCachedPlaceName() {
        return cachedPlaceName;
    }

    public double getCachedPlaceLat() {
        return cachedPlaceLat;
    }

    public double getCachedPlaceLong() {
        return cachedPlaceLong;
    }
}
