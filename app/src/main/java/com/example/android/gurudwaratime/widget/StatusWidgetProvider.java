package com.example.android.gurudwaratime.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.gurudwaratime.R;
import com.example.android.gurudwaratime.background_tasks.GurudwaraTimeSyncTasks;
import com.example.android.gurudwaratime.ui.status.StatusActivity;
import com.example.android.gurudwaratime.ui.status.StatusViewModel;

/**
 * Implementation of App Widget functionality.
 */
public class StatusWidgetProvider extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId,
                                        StatusViewModel.AutoSilentStatusStates silentStatus,
                                        String locationName, String locationVicinity) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_status);

        CharSequence statusText;
        switch (silentStatus) {
            case TURNED_OFF:
                //show appropriate status
                statusText = context.getString(R.string.msg_auto_silent_off);
                views.setTextViewText(R.id.text_subtitle_status_widget, statusText);

                //hide location texts
                views.setViewVisibility(R.id.text_location_name_status_widget, View.GONE);
                views.setViewVisibility(R.id.text_location_vicinity_status_widget, View.GONE);
                break;

            case INIT:
                //show appropriate status
                statusText = context.getString(R.string.msg_auto_silent_init);
                views.setTextViewText(R.id.text_subtitle_status_widget, statusText);

                //hide location texts
                views.setViewVisibility(R.id.text_location_name_status_widget, View.GONE);
                views.setViewVisibility(R.id.text_location_vicinity_status_widget, View.GONE);
                break;
            case NO_LOCATION:
                //show appropriate status
                statusText = context.getString(R.string.msg_no_location_detected);
                views.setTextViewText(R.id.text_subtitle_status_widget, statusText);

                //hide location texts
                views.setViewVisibility(R.id.text_location_name_status_widget, View.GONE);
                views.setViewVisibility(R.id.text_location_vicinity_status_widget, View.GONE);
                break;
            case AT_LOCATION:
                //show appropriate status
                statusText = context.getString(R.string.msg_at_location);
                views.setTextViewText(R.id.text_subtitle_status_widget, statusText);

                //set location texts
                if (!TextUtils.isEmpty(locationName)) {
                    //there is location name
                    views.setTextViewText(R.id.text_location_name_status_widget,
                            locationName);
                    views.setTextViewText(R.id.text_location_vicinity_status_widget,
                            locationVicinity);
                    //show location texts
                    views.setViewVisibility(R.id.text_location_name_status_widget,
                            View.VISIBLE);
                    views.setViewVisibility(R.id.text_location_vicinity_status_widget,
                            View.VISIBLE);
                } else {
                    //location name is empty
                    views.setTextViewText(R.id.text_location_name_status_widget,
                            context.getString(R.string.msg_place_name_default_at_location));

                    //show location texts
                    views.setViewVisibility(R.id.text_location_name_status_widget,
                            View.VISIBLE);
                    views.setViewVisibility(R.id.text_location_vicinity_status_widget,
                            View.GONE);
                }
                break;
        }

        Intent intent = new Intent(context, StatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.layout_status_widget, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        GurudwaraTimeSyncTasks.scheduleOnDemandStatusWidgetUpdate(context);
    }

    public static void updateStatusWidgets(Context context, AppWidgetManager appWidgetManager,
                                           int[] appWidgetIds,
                                           StatusViewModel.AutoSilentStatusStates silentStatus,
                                           String locationName, String locationVicinity) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, silentStatus,
                    locationName, locationVicinity);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

