package com.example.android.gurudwaratime.geofencing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.example.android.gurudwaratime.R;
import com.example.android.gurudwaratime.background_tasks.HandleAtLocationTasksIntentService;
import com.example.android.gurudwaratime.ui.status.StatusActivity;
import com.example.android.gurudwaratime.ui.status.StatusViewModel;
import com.example.android.gurudwaratime.ui.welcome.PermissionsViewModel;

import static com.example.android.gurudwaratime.data.Constants.ACTION_NEVER_SILENT_AT_LOCATION;
import static com.example.android.gurudwaratime.data.Constants.ACTION_UNDO_SILENT_AT_LOCATION;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCE_TRIGGER_CHANNEL;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCING_NOTIFICATION_ACTION_NEVER_SILENT_ID;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCING_NOTIFICATION_ACTION_UNDO_SILENT_ID;
import static com.example.android.gurudwaratime.data.Constants.GEOFENCING_NOTIFICATION_ID;
import static com.example.android.gurudwaratime.data.Constants.INVALID_RINGER_MODE;
import static com.example.android.gurudwaratime.data.Constants.KEY_CURRENT_GEOFENCE_PLACE_ID;
import static com.example.android.gurudwaratime.data.Constants.KEY_CURRENT_GEOFENCE_PLACE_NAME;
import static com.example.android.gurudwaratime.data.Constants.KEY_CURRENT_GEOFENCE_PLACE_VICINITY;
import static com.example.android.gurudwaratime.data.Constants.KEY_CURRENT_GEOFENCE_RESTORE_RINGER_MODE;

public class GeofencingResultHelper {

    private static final String TAG = GeofencingResultHelper.class.getSimpleName();

    private Context mContext;

    private NotificationManager mNotificationManager;

    public GeofencingResultHelper(Context context) {
        mContext = context;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(GEOFENCE_TRIGGER_CHANNEL,
                    context.getString(R.string.geofence_notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            getNotificationManager().createNotificationChannel(channel);
        }

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
     * Posts a notification in the notification bar when a transition is detected
     * Uses different icon drawables for different transition types
     * If the user clicks the notification, control goes to the MainActivity
     *
     * @param ringerMode the ringer mode which is being set
     * @param placeName  place name
     * @param isEnter    whether this is a enter transition
     */
    public void sendNotification(int ringerMode, String placeName, boolean isEnter) {
        // Create an explicit content Intent that starts the main Activity.
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

        // Get a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,
                GEOFENCE_TRIGGER_CHANNEL);
        builder.setContentText(mContext.getString(R.string.msg_touch_to_launch));
        builder.setSmallIcon(R.drawable.ic_notif);

        // Check the transition type to display the relevant icon image
        switch (ringerMode) {

            case AudioManager.RINGER_MODE_SILENT:
                builder.setLargeIcon(
                        getDarkBitmapFromVectorDrawable(
                                mContext,
                                R.drawable.ic_action_setting_silent_white_24dp));
                setNotificationText(placeName, isEnter, builder);
                setNotificationActions(builder);
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                builder.setLargeIcon(
                        getDarkBitmapFromVectorDrawable(
                                mContext,
                                R.drawable.ic_action_setting_vibrate_white_24dp));
                setNotificationText(placeName, isEnter, builder);
                setNotificationActions(builder);
                break;

            case AudioManager.RINGER_MODE_NORMAL:
                builder.setLargeIcon(
                        getDarkBitmapFromVectorDrawable(
                                mContext,
                                R.drawable.ic_action_setting_normal_white_24dp));
                setNotificationText(placeName, isEnter, builder);
                break;
            default:
                Log.e(TAG, "sendNotification: invalid ringer mode: " + ringerMode);
                return;
        }

        // Continue building the notification
        builder.setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        getNotificationManager().notify(GEOFENCING_NOTIFICATION_ID, builder.build());
    }

    private void setNotificationActions(NotificationCompat.Builder builder) {
        // set actions for current place notification
        //undo silent action
        Intent undoIntent = new Intent(mContext, HandleAtLocationTasksIntentService.class);
        undoIntent.setAction(ACTION_UNDO_SILENT_AT_LOCATION);
        PendingIntent undoPendingIntent = PendingIntent.getService(mContext,
                GEOFENCING_NOTIFICATION_ACTION_UNDO_SILENT_ID, undoIntent,
                PendingIntent.FLAG_ONE_SHOT);
        builder.addAction(R.drawable.ic_action_undo_white_24dp,
                mContext.getString(R.string.action_undo_silent), undoPendingIntent);

        //never silent here action
        Intent neverSilentIntent = new Intent(mContext,
                HandleAtLocationTasksIntentService.class);
        undoIntent.setAction(ACTION_NEVER_SILENT_AT_LOCATION);
        PendingIntent neverSilentPendingIntent = PendingIntent.getService(mContext,
                GEOFENCING_NOTIFICATION_ACTION_NEVER_SILENT_ID, neverSilentIntent,
                PendingIntent.FLAG_ONE_SHOT);
        builder.addAction(R.drawable.ic_action_exclude_white_24dp,
                mContext.getString(R.string.action_never_silent), neverSilentPendingIntent);

    }

    public void clearNotification() {
        getNotificationManager().cancel(GEOFENCING_NOTIFICATION_ID);
    }

    private void setNotificationText(String placeName, boolean isEnter,
                                     NotificationCompat.Builder builder) {
        if (isEnter) {
            builder.setContentTitle
                    (mContext.getString(R.string.msg_silent_mode_activated));
            if (placeName != null) {
                builder.setContentText(placeName);
            }
        } else {
            builder.setContentTitle(mContext.getString(R.string.msg_back_to_normal));
        }
    }

    /**
     * Changes the ringer mode on the device to either silent or back to normal
     *
     * @param mode The desired mode to switch device to,
     *             can be
     *             AudioManager.RINGER_MODE_SILENT or
     *             AudioManager.RINGER_MODE_NORMAL or
     *             AudioManager.RINGER_MODE_VIBRATE
     */
    public void setCurrentRingerMode(int mode) {
        // Check for DND permissions for API 24+
        if (PermissionsViewModel.checkLocationAndDndPermissions(mContext)
                && StatusViewModel.getAutoSilentRequestedStatus(mContext)) {
            AudioManager audioManager = (AudioManager)
                    mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }

    /**
     * gets the ringer mode on the device
     *
     * @return gets the current ringer mode
     * AudioManager.RINGER_MODE_SILENT or
     * AudioManager.RINGER_MODE_NORMAL or
     * AudioManager.RINGER_MODE_VIBRATE
     */
    public int getCurrentRingerMode() {
        AudioManager audioManager = (AudioManager)
                mContext.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getRingerMode();
    }

    /**
     * save place id for geofence device is currently inside
     *
     * @param placeId Place Id for geofence location
     */
    public void saveCurrentGeofencePlaceId(String placeId) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_CURRENT_GEOFENCE_PLACE_ID, placeId)
                .apply();
    }

    /**
     * get place id for geofence device is currently inside
     *
     * @return current geofence place id
     */
    public String getCurrentGeofencePlaceId() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(KEY_CURRENT_GEOFENCE_PLACE_ID, null);
    }

    /**
     * saves ringer mode to restore once the device exits current geofence
     *
     * @param currentRingerMode current geofence restore ringer mode
     */
    public void saveCurrentGeofenceRestoreRingerMode(int currentRingerMode) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putInt(KEY_CURRENT_GEOFENCE_RESTORE_RINGER_MODE, currentRingerMode)
                .apply();
    }

    /**
     * gets ringer mode to restore once the device exits current geofence
     *
     * @return restore ringer mode for current geofence
     */
    public int getCurrentGeofenceRestoreRingerMode() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(KEY_CURRENT_GEOFENCE_RESTORE_RINGER_MODE, INVALID_RINGER_MODE);
    }

    /**
     * save place name for geofence device is currently inside
     *
     * @param placeName Place Id for geofence location
     */
    public void saveCurrentGeofencePlaceName(String placeName) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_CURRENT_GEOFENCE_PLACE_NAME, placeName)
                .apply();
    }

    /**
     * get place name for geofence device is currently inside
     *
     * @return current geofence place name
     */
    public String getCurrentGeofencePlaceName() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(KEY_CURRENT_GEOFENCE_PLACE_NAME, null);
    }

    /**
     * save place vicinity for geofence device is currently inside
     *
     * @param placeVicinity Place Id for geofence location
     */
    public void saveCurrentGeofencePlaceVicinity(String placeVicinity) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_CURRENT_GEOFENCE_PLACE_VICINITY, placeVicinity)
                .apply();
    }

    /**
     * get place vicinity for geofence device is currently inside
     *
     * @return current geofence place vicinity
     */
    public String getCurrentGeofencePlaceVicinity() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(KEY_CURRENT_GEOFENCE_PLACE_VICINITY, null);
    }

    /**
     * clear current geofence data
     */
    public void clearCurrentGeofenceData() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .remove(KEY_CURRENT_GEOFENCE_RESTORE_RINGER_MODE)
                .remove(KEY_CURRENT_GEOFENCE_PLACE_ID)
                .remove(KEY_CURRENT_GEOFENCE_PLACE_NAME)
                .remove(KEY_CURRENT_GEOFENCE_PLACE_VICINITY)
                .apply();
    }

    /**
     * source: https://stackoverflow.com/a/38244327/10030480
     *
     * @param context    context for bitmap creation
     * @param drawableId drawable id
     * @return bitmap
     */
    public static Bitmap getDarkBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent),
                    PorterDuff.Mode.DST);
            drawable.draw(canvas);
            return bitmap;
        }

        return null;
    }


}
