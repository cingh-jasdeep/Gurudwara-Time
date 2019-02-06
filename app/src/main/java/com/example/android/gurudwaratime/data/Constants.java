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
    public final static String KEY_AUTO_SILENT_REQUESTED_STATUS = "auto-silent-requested-status";

    /**
     * shared pref key for auto silent mode
     */
    public final static String KEY_AUTO_SILENT_REQUESTED_MODE = "auto-silent-requested-mode";

    /**
     * shared pref keys for current geofence
     */
    public final static String KEY_CURRENT_GEOFENCE_PLACE_ID = "current-geofence-place-id";
    public final static String KEY_CURRENT_GEOFENCE_RESTORE_RINGER_MODE =
            "current-geofence-restore-ringer-mode";
    public final static String KEY_CURRENT_GEOFENCE_PLACE_NAME = "current-geofence-place-name";
    public final static String KEY_CURRENT_GEOFENCE_PLACE_VICINITY =
            "current-geofence-place-vicinity";

    public final static int INVALID_RINGER_MODE = -1;

    // sync constants

    public static final String ON_DEMAND_NEARBY_SYNC_TAG = "on-demand-nearby-sync-tag";
    public static final String NEARBY_SYNC_REFRESH_TAG = "nearby-sync-refresh-tag";

    public static final String EXTRA_SYNC_NEW_LOCATION_JSON = "extra-new-location-json";
    public static final String EXTRA_FORCE_SYNC_NEW_LOCATION = "extra-force-sync-new-location";

    public final static int NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_HOURS = 24;

    public final static long NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_MILLIS =
            TimeUnit.HOURS.toMillis(NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_HOURS);

    public final static int NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_SECONDS =
            (int) TimeUnit.HOURS.toSeconds(NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_HOURS);

    public final static int NEARBY_SYNC_FLEX_TIME_LENGTH_IN_SECONDS =
            NEARBY_SYNC_EXPIRY_TIME_LENGTH_IN_SECONDS / 8;

    public static final int NEARBY_SYNC_NOTIFICATION_ID = 463;


    //json parsing constants
    public final static String LOCATION_JSON_LAT = "latitude";
    public final static String LOCATION_JSON_LONG = "longitude";
    public final static String LOCATION_JSON_PROVIDER = "provider";


    public static final double INVALID_LAT = 91;
    public static final double INVALID_LONG = 181;

    //Geofencing constants
    public static final float DEFAULT_GEOFENCE_RADIUS = 50; // 50 meters
    public static final double GEOFENCE_AREA_VIEWPORT_FACTOR = 0.6; // 60% of viewport area

    public final static long GEOFENCE_DEFAULT_EXPIRY_TIME_LENGTH_IN_HOURS = 24;
    public final static long GEOFENCE_DEFAULT_EXPIRY_TIME_LENGTH_IN_MILLIS =
            TimeUnit.HOURS.toMillis(GEOFENCE_DEFAULT_EXPIRY_TIME_LENGTH_IN_HOURS);

    public final static int GEOFENCE_DEFAULT_LOITERING_TIME_LENGTH_IN_SECONDS = 25;
    public final static int GEOFENCE_DEFAULT_LOITERING_TIME_LENGTH_IN_MILLIS =
            (int) TimeUnit.SECONDS.toMillis(GEOFENCE_DEFAULT_LOITERING_TIME_LENGTH_IN_SECONDS);

    public static final int GEOFENCING_NOTIFICATION_ID = 203;
    public static final int GEOFENCING_NOTIFICATION_ACTION_UNDO_SILENT_ID = 204;
    public static final int GEOFENCING_NOTIFICATION_ACTION_NEVER_SILENT_ID = 205;

    //geofence handling
    public static final String ON_DEMAND_CURRENT_GEOFENCE_HANDLE_TAG = "geofence-handle-tag";
    //geofence syncing
    public static final String REFRESH_GEOFENCE_SETUP_TAG = "refresh-geofence-setup-tag";

    public static final int GEOFENCE_SYNC_START_TIME_LENGTH_IN_HOURS = 23;
    public static final int GEOFENCE_SYNC_START_TIME_LENGTH_IN_SECONDS =
            (int) TimeUnit.HOURS.toSeconds(GEOFENCE_SYNC_START_TIME_LENGTH_IN_HOURS);

    public static final int GEOFENCE_SYNC_FLEX_TIME_LENGTH_IN_SECONDS = 60;


    public static final String EXTRA_HANDLE_GEOFENCE_EVENT_TRANSITION =
            "extra-handle-geofence-event-transition";

    public static final String EXTRA_HANDLE_GEOFENCE_EVENT_REQUEST_ID =
            "extra-handle-geofence-event-request-id";

    public static final int INVALID_GEOFENCE_TRANSITION =
            -1;


    public static final int INVALID_INDEX = -1;


    //paging constants
    public static final int DEFAULT_PAGING_SIZE = 20;


    //Notification Constants
    public final static String LOCATION_SYNC_CHANNEL = "location";
    public final static String GEOFENCE_TRIGGER_CHANNEL = "geofence";

    //at location tasks constants
    public final static String ACTION_UNDO_SILENT_AT_LOCATION = BuildConfig.APPLICATION_ID +
            "ACTION_UNDO_SILENT_AT_LOCATION";

    public final static String ACTION_NEVER_SILENT_AT_LOCATION = BuildConfig.APPLICATION_ID +
            "ACTION_NEVER_SILENT_AT_LOCATION";

    //widget constants
    public static final String UPDATE_STATUS_WIDGET_TAG = "update-status-widget-tag";

    //include exclude constants
    public static final int MAX_INCLUDE_EXCLUDE_PLACES_LIST_SIZE = 20;

    //google maps link constants
    public static final String GOOGLE_MAPS_LINK_BASE_URL =
            "https://www.google.com/maps/search/?api=1";
    public static final String GOOGLE_MAPS_LINK_SCHEME = "https";
    public static final String GOOGLE_MAPS_LINK_AUTHORITY = "www.google.com";
    public static final String GOOGLE_MAPS_LINK_PATH_1_MAPS = "maps";
    public static final String GOOGLE_MAPS_LINK_PATH_2_SEARCH = "search/";
    public static final String GOOGLE_MAPS_LINK_QUERY_1_KEY_API = "api";
    public static final String GOOGLE_MAPS_LINK_QUERY_1_DEFAULT_VALUE = "1";
    public static final String GOOGLE_MAPS_LINK_QUERY_2_KEY_QUERY = "query";
    public static final String GOOGLE_MAPS_LINK_QUERY_3_KEY_QUERY_PLACE_ID = "query_place_id";

    //db tasks constants
    public final static String ACTION_RESET_EXCLUDED_PLACES = BuildConfig.APPLICATION_ID +
            "ACTION_UNDO_SILENT_AT_LOCATION";

}
