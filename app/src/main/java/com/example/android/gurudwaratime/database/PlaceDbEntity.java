package com.example.android.gurudwaratime.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = DataNames.TABLE_NAME_PLACES)
public class PlaceDbEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = DataNames.COLUMN_PLACE_ID)
    private String placeId;

    @ColumnInfo(name = DataNames.COLUMN_IS_EXCLUDED)
    private boolean isExcluded;

    @ColumnInfo(name = DataNames.COLUMN_IS_NEARBY)
    private boolean isNearby;

    @ColumnInfo(name = DataNames.COLUMN_UPDATED_AT)
    private long updatedAt;

    @ColumnInfo(name = DataNames.COLUMN_NEARBY_INDEX)
    private int nearbyIndex;

    @ColumnInfo(name = DataNames.COLUMN_CACHED_PLACE_LAT)
    private double cachedPlaceLat;

    @ColumnInfo(name = DataNames.COLUMN_CACHED_PLACE_LONG)
    private double cachedPlaceLong;

    @ColumnInfo(name = DataNames.COLUMN_CACHED_PLACE_GEOFENCE_RADIUS)
    private float cachedPlaceGeofenceRadius;

    public PlaceDbEntity(@NonNull String placeId, boolean isExcluded,
                         boolean isNearby, long updatedAt, int nearbyIndex, double cachedPlaceLat,
                         double cachedPlaceLong, float cachedPlaceGeofenceRadius) {
        this.placeId = placeId;
        this.isExcluded = isExcluded;
        this.isNearby = isNearby;
        this.updatedAt = updatedAt;
        this.nearbyIndex = nearbyIndex;
        this.cachedPlaceLat = cachedPlaceLat;
        this.cachedPlaceLong = cachedPlaceLong;
        this.cachedPlaceGeofenceRadius = cachedPlaceGeofenceRadius;
    }

    @NonNull
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(@NonNull String placeId) {
        this.placeId = placeId;
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(boolean excluded) {
        isExcluded = excluded;
    }

    public boolean isNearby() {
        return isNearby;
    }

    public void setNearby(boolean nearby) {
        isNearby = nearby;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getNearbyIndex() {
        return nearbyIndex;
    }

    public void setNearbyIndex(int nearbyIndex) {
        this.nearbyIndex = nearbyIndex;
    }

    public double getCachedPlaceLat() {
        return cachedPlaceLat;
    }

    public void setCachedPlaceLat(double cachedPlaceLat) {
        this.cachedPlaceLat = cachedPlaceLat;
    }

    public double getCachedPlaceLong() {
        return cachedPlaceLong;
    }

    public void setCachedPlaceLong(double cachedPlaceLong) {
        this.cachedPlaceLong = cachedPlaceLong;
    }

    public float getCachedPlaceGeofenceRadius() {
        return cachedPlaceGeofenceRadius;
    }

    public void setCachedPlaceGeofenceRadius(float cachedPlaceGeofenceRadius) {
        this.cachedPlaceGeofenceRadius = cachedPlaceGeofenceRadius;
    }
}
