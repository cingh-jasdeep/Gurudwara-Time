package com.example.android.gurudwaratime.location_updates;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.android.gurudwaratime.R;
import com.example.android.gurudwaratime.ui.status.StatusActivity;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.android.gurudwaratime.data.Constants.KEY_LAST_SYNC_LOCATION_JSON;
import static com.example.android.gurudwaratime.data.Constants.KEY_LAST_SYNC_TIME_IN_MILLIS;
import static com.example.android.gurudwaratime.data.Constants.LOCATION_JSON_LAT;
import static com.example.android.gurudwaratime.data.Constants.LOCATION_JSON_LONG;
import static com.example.android.gurudwaratime.data.Constants.LOCATION_JSON_PROVIDER;
import static com.example.android.gurudwaratime.data.Constants.SYNC_EXPIRY_TIME_LENGTH_IN_MILLIS;

/**
 * Class to process location results.
 */
public class LocationResultHelper {

    private static final String TAG = LocationResultHelper.class.getSimpleName();

    final private static String PRIMARY_CHANNEL = "location";


    private Context mContext;
    private Location mLocation;
    private NotificationManager mNotificationManager;

    public LocationResultHelper(Context context, @NonNull String locationJsonString) {
        mContext = context;
        mLocation = toLocation(locationJsonString);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                    context.getString(R.string.location_notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            getNotificationManager().createNotificationChannel(channel);
        }

    }

    /**
     * returns last location stored after successful sync from
     * {@link android.content.SharedPreferences}
     *
     * @param context application context
     * @return returns last sync {@link android.location.Location}
     */
    public static Location getLastSyncLocation(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String locationJsonString = sp.getString(KEY_LAST_SYNC_LOCATION_JSON, "");

        //check valid location data
        if (locationJsonString != null && !locationJsonString.equals("")) {
            return toLocation(locationJsonString);
        }

        return null;
    }

    /**
     * returns last location stored after successful sync from
     * {@link android.content.SharedPreferences}
     *
     * @param context application context
     * @return returns last sync {@link android.location.Location}
     */
    public static long getLastSyncTimeInMillis(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getLong(KEY_LAST_SYNC_TIME_IN_MILLIS, 0);
    }

    /**
     * returns last location stored after successful sync from
     * {@link android.content.SharedPreferences}
     *
     * @param context application context
     * @return returns last sync {@link android.location.Location}
     */
    public static void clearLastSyncLocation(Context context) {

        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();
        editor.remove(KEY_LAST_SYNC_LOCATION_JSON);
        editor.remove(KEY_LAST_SYNC_TIME_IN_MILLIS);
        editor.apply();

    }

    /**
     * converts location json string to location object
     * uses only lat, long and provider in json
     *
     * @param locationJsonString location json string with lat, long and provider
     *                           see {@link com.example.android.gurudwaratime.data.Constants}
     * @return {@link android.location} object for valid json
     */
    private static Location toLocation(@NonNull String locationJsonString) {
        try {
            JSONObject locationJson = new JSONObject(locationJsonString);
            if (locationJson.has(LOCATION_JSON_PROVIDER)
                    && locationJson.has(LOCATION_JSON_LAT)
                    && locationJson.has(LOCATION_JSON_LONG)) {

                Location location = new Location(locationJson.getString(LOCATION_JSON_PROVIDER));
                location.setLatitude(locationJson.getDouble(LOCATION_JSON_LAT));
                location.setLongitude(locationJson.getDouble(LOCATION_JSON_LONG));

                return location;
            } else {
                throw new JSONException("Invalid json object");
            }
        } catch (JSONException e) {
            Log.e(TAG, "toLocation: ", e);
            return null;
        }
    }

    /**
     * converts {@link android.location} object to a json string
     * uses only lat, long and provider in json
     *
     * @param location location to be converted
     * @return json string
     */
    public static String toJsonString(@NonNull Location location) {
        JSONObject locationJson = new JSONObject();

        try {

            locationJson.put(LOCATION_JSON_PROVIDER, location.getProvider());
            locationJson.put(LOCATION_JSON_LAT, location.getLatitude());
            locationJson.put(LOCATION_JSON_LONG, location.getLongitude());
            return locationJson.toString();

        } catch (JSONException e) {
            Log.e(TAG, "toLocation: ", e);
            return null;
        }
    }

    private String getLocationResultText() {
        if (mLocation == null) {
            return null;
        }
        return mLocation.getLatitude() + "," + mLocation.getLongitude();
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * Displays a notification with the location results.
     */
    public void showNotification() {
        Intent notificationIntent = new Intent(mContext, StatusActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(StatusActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext, PRIMARY_CHANNEL)
                        .setContentTitle(mContext.getString(R.string.msg_synced_nearby_gurudwaras))
                        .setContentText((getLocationResultText() == null) ?
                                mContext.getString(R.string.unknown_location)
                                : getLocationResultText())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent);

        getNotificationManager().notify(0, notificationBuilder.build());
    }

    /**
     * saves last location after successful sync to {@link android.content.SharedPreferences}
     */
    public void saveSyncLocation() {

        String locationJson = toJsonString(mLocation);
        //check valid location data
        if (locationJson != null && !locationJson.equals("")) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .edit();
            editor.putString(KEY_LAST_SYNC_LOCATION_JSON, locationJson);
            editor.putLong(KEY_LAST_SYNC_TIME_IN_MILLIS, System.currentTimeMillis());
            editor.apply();
        }
    }

    /**
     * @return true if the new location is fresh to perform a new nearby sync
     */
    public boolean isFreshLocation() {
        Location lastSyncLocation = getLastSyncLocation(mContext);
        if (lastSyncLocation != null) {
            final float currDisplacement = mLocation.distanceTo(lastSyncLocation);
            boolean isLocationFresh = (currDisplacement >=
                    LocationRequestHelper.SMALLEST_DISPLACEMENT_IN_METERS);
            long lastSyncTimeInMillis = getLastSyncTimeInMillis(mContext);
            boolean isTimeExpired = (System.currentTimeMillis() - lastSyncTimeInMillis)
                    >= SYNC_EXPIRY_TIME_LENGTH_IN_MILLIS;
            return isLocationFresh || isTimeExpired;
        }
        return true;
    }

    /**
     * @return new location recieved in constructor
     */
    public Location getLocation() {
        return mLocation;
    }
}
