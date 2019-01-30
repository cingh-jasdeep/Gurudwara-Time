package com.example.android.gurudwaratime.data;

import com.example.android.gurudwaratime.BuildConfig;

import java.util.concurrent.TimeUnit;

public class Constants {

    /**
     * Base URL for Places Api
     */
    public static final String NEARBY_SEARCH_BASE_URL =
            "https://maps.googleapis.com/maps/api/place/";

    /**
     * Path URL for Nearby Search
     */
    public static final String NEARBY_SEARCH_URL_PATH = "nearbysearch/json";

    // Sample api call
    // (base url)
    // https://maps.googleapis.com/maps/api/place/
    // nearbysearch/json
    // ?location=28.5306261,77.4727113
    // &type=place_of_worship
    // &keyword=gurudwara
    // &key=API_KEY
    // &rankby=distance

    //PLACES API QUERY KEYS
    public static final String LOCATION_QUERY_KEY_PLACES_API = "location";
    public static final String TYPE_QUERY_KEY_PLACES_API = "type";
    public static final String KEYWORD_QUERY_KEY_PLACES_API = "keyword";
    public static final String KEY_QUERY_KEY_PLACES_API = "key";
    public static final String RANKBY_QUERY_KEY_PLACES_API = "rankby";

    //PLACES API QUERY DEFAULT VALUES
    public static final String TYPE_QUERY_DEFAULT_VALUE_PLACES_API = "place_of_worship";
    public static final String KEYWORD_QUERY_DEFAULT_VALUE_PLACES_API = "Gurudwara";
    public static final String KEY_QUERY_DEFAULT_VALUE_PLACES_API = BuildConfig.GOOGLE_PLACES_API_KEY;
    public static final String RANKBY_QUERY_DEFAULT_VALUE_PLACES_API = "distance";


    //PLACES API
    public static final int PLACES_API_PAGE_SIZE = 20;
    public static final int PLACES_API_NEXT_PAGE_SLEEP_INTERVAL_MILLIS = 2000;

    //NEARBY MAP VIEW VALUES
    public static final String KEYWORD_QUERY_NEARBY_MAP = "Gurudwaras";
    public static final String KEYWORD_QUERY_NEARBY_URL = "Nearby Gurudwaras";


    /**
     * shared pref keys for location updates request, last sync location
     */
    public final static String KEY_LOCATION_UPDATES_REQUEST_STATUS =
            "location-updates-request-status";
    public final static String KEY_LAST_SYNC_LOCATION_JSON =
            "location-updates-last-sync-location-json";
    public final static String KEY_LAST_SYNC_TIME_IN_MILLIS =
            "location-updates-last-sync-time-in-millis";

    /**
     * shared pref key for auto silent status
     */
    public final static String KEY_AUTO_SILENT_STATUS = "auto-silent-status";


    // sync constants

    public static final String GURUDWARA_TIME_SYNC_TAG = "gurudwara-time-sync-tag";
    public static final String EXTRA_SYNC_NEW_LOCATION_JSON = "extra-new-location-json";
    public static final String EXTRA_FORCE_SYNC_NEW_LOCATION = "extra-force-sync-new-location";

    public final static long SYNC_EXPIRY_TIME_LENGTH_IN_HOURS = 24;
    public final static long SYNC_EXPIRY_TIME_LENGTH_IN_MILLIS =
            TimeUnit.HOURS.toMillis(SYNC_EXPIRY_TIME_LENGTH_IN_HOURS);


    //json parsing constants
    public final static String LOCATION_JSON_LAT = "latitude";
    public final static String LOCATION_JSON_LONG = "longitude";
    public final static String LOCATION_JSON_PROVIDER = "provider";


    public static final double INVALID_LAT = 91;
    public static final double INVALID_LONG = 181;

    //Geofencing constants
    public static final float DEFAULT_GEOFENCE_RADIUS = 50; // 50 meters
    public static final double GEOFENCE_AREA_VIEWPORT_FACTOR = 0.6; // 60% of viewport area

    public static final int INVALID_INDEX = -1;


    //paging constants
    public static final int DEFAULT_PAGING_SIZE = 20;

}
