package android.example.vehiclemaintenancetracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.widget.MaintenanceListViewWidgetService;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class VehicleMaintenanceTrackerAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vehicle_maintenance_tracker_app_widget);

        // Create an intent to launch the main activity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Attach the on click listener to the app widget text.
//        remoteViews.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        // Populate list view
        Intent listIntent = new Intent(context, MaintenanceListViewWidgetService.class);
        remoteViews.setRemoteAdapter(R.id.maintenanceWidgetListView, listIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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

