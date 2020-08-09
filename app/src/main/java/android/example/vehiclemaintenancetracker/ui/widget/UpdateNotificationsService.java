package android.example.vehiclemaintenancetracker.ui.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.ServiceNotificationCollection;
import android.example.vehiclemaintenancetracker.ui.MainActivity;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.example.vehiclemaintenancetracker.utilities.ServiceNotificationGenerator;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import java.util.List;

import timber.log.Timber;

public class UpdateNotificationsService extends Service {
    public UpdateNotificationsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        final int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        if (appWidgetIds != null) {
            AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    // Build up the notifications once.
                    ServiceNotificationGenerator.generateServiceNotifications(getApplicationContext(), new ServiceNotificationGenerator.ServiceNotificationsListener() {
                        @Override
                        public void onNotificationsReady(List<ServiceNotification> serviceNotifications) {
                            Timber.d("%d service notifications generated", serviceNotifications.size());
                            // Then pass the notifications to the app widgets.
                            for (int appWidgetId : appWidgetIds) {
                                updateAppWidget(getApplicationContext(), appWidgetManager, appWidgetId, serviceNotifications);
                            }
                            stopSelf(startId);
                        }

                        @Override
                        public void onError(String error) {
                            stopSelf(startId);
                        }
                    });
                }
            });
        }

        return START_NOT_STICKY;
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId, List<ServiceNotification> serviceNotifications) {

        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vehicle_maintenance_tracker_app_widget);

        // Create an intent to launch the main activity when the app widget is clicked
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_layout, clickPendingIntent);
        remoteViews.setPendingIntentTemplate(R.id.maintenanceWidgetListView, clickPendingIntent);

        String json = "";

        if (serviceNotifications.size() > 0) {
            // There are notifications.  The up-to-date view should not be displayed.
            remoteViews.setViewVisibility(R.id.textViewServiceUpToDate, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.header_rows, View.VISIBLE);

            // Populate list view
            Gson gson = new Gson();
            ServiceNotificationCollection collection = new ServiceNotificationCollection();
            collection.setServiceNotifications(serviceNotifications);

            // Serialize our data to a JSON string.
            json = gson.toJson(collection);

            Intent listIntent = new Intent(context, MaintenanceListViewWidgetService.class);
            listIntent.putExtra(MaintenanceListRemoteViewsFactory.SERVICE_NOTIFICATIONS_EXTRA, json);
            remoteViews.setRemoteAdapter(R.id.maintenanceWidgetListView, listIntent);
        } else {
            // There are no notifications.  The up-to-date view should be displayed.
            remoteViews.setViewVisibility(R.id.textViewServiceUpToDate, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.header_rows, View.INVISIBLE);
        }

        // Post the intent to update the service notifications in the list.
        Intent listIntent = new Intent(MaintenanceListRemoteViewsFactory.ACTION_NOTIFICATIONS_CHANGED);
        listIntent.putExtra(MaintenanceListRemoteViewsFactory.SERVICE_NOTIFICATIONS_EXTRA, json);
        sendBroadcast(listIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.maintenanceWidgetListView);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
