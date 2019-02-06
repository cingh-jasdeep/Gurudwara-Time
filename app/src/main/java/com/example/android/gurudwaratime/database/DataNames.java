package com.example.android.gurudwaratime.database;

public class DataNames {
    public static final String DATABASE_NAME = "gurudwara_time_db";

    public static final String TABLE_NAME_PLACES = "places";

    public static final String COLUMN_PLACE_ID = "place_id";

    public static final String COLUMN_IS_EXCLUDED = "is_excluded";

    public static final String COLUMN_IS_NEARBY = "is_nearby";

    /**
     * index from the sorted nearby search result list
     * not applicable user included and excluded places (-1)
     */
    public static final String COLUMN_NEARBY_INDEX = "nearby_index";

    /**
     * to be used only when the app is loading results or offline
     */
    public static final String COLUMN_CACHED_PLACE_LONG = "cached_place_long";
    public static final String COLUMN_CACHED_PLACE_LAT = "cached_place_lat";
    public static final String COLUMN_CACHED_PLACE_GEOFENCE_RADIUS
            = "cached_place_geofence_radius"; //radius is in meters


    public static final String COLUMN_UPDATED_AT = "updated_at";

    public static final int TRUE = 1;

    public static final int FALSE = 0;
}
