package android.example.vehiclemaintenancetracker.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.ServiceNotificationCollection;
import android.example.vehiclemaintenancetracker.ui.Styler;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MaintenanceListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String ACTION_NOTIFICATIONS_CHANGED = "ACTION_NOTIFICATIONS_CHANGED";
    public static final String SERVICE_NOTIFICATIONS_EXTRA = "SERVICE_NOTIFICATION";
    public static final String PARAM_MAINTENANCE_ROW = "PARAM_MAINTENANCE_ROW";

    private Context context;
    private List<ServiceNotification> serviceNotifications;

    private BroadcastReceiver broadcastReceiver;

    public MaintenanceListRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;

        setupBroadcastReceiver();

        // Deserialize the service notifications.
        if (intent.hasExtra(SERVICE_NOTIFICATIONS_EXTRA)) {
            setNotificationsFromIntent(intent);
        } else {
            Timber.e("Remote view service created for list but there are no service notifications");
            serviceNotifications = new ArrayList<>();
        }
    }

    private void setupBroadcastReceiver() {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Timber.d("BroadcastReceiver onReceive, hasExtra: %b", intent.hasExtra(SERVICE_NOTIFICATIONS_EXTRA));
                    setNotificationsFromIntent(intent);
                }
            };

            IntentFilter intentFilter = new IntentFilter(ACTION_NOTIFICATIONS_CHANGED);
            context.registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private void setNotificationsFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra(SERVICE_NOTIFICATIONS_EXTRA)) {
            String json = intent.getStringExtra(SERVICE_NOTIFICATIONS_EXTRA);

            Gson gson = new Gson();
            ServiceNotificationCollection collection = gson.fromJson(json, ServiceNotificationCollection.class);

            if (collection != null && collection.getServiceNotifications() != null) {
                serviceNotifications = collection.getServiceNotifications();
            } else {
                // No service notifications - just create an empty list here.
                serviceNotifications = new ArrayList<>();
            }

            Timber.d("Loaded %d notifications", serviceNotifications.size());
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        ServiceNotification serviceNotification = serviceNotifications.get(position);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_list_content);

        remoteViews.setTextViewText(R.id.textViewService, serviceNotification.getService());

        if (serviceNotification.getMileageDue() != null) {
            remoteViews.setTextViewText(R.id.textViewMileage, ValueFormatter.formatDistance(serviceNotification.getMileageDue()));
        }

        if (serviceNotification.getDateDue() != null) {
            remoteViews.setTextViewText(R.id.textViewDate, DateConverter.convertDateToString(context, serviceNotification.getDateDue()));
        }

        Styler.styleImageViewResourceStatus(context, remoteViews, R.id.imageView, serviceNotification.getOverallStatus());

        // Set on click to launch main app.
        Intent fillInIntent = new Intent();

        // This extra is not currently used, but in the future we could consider launching
        // the add maintenance activity for the specific maintenance row.
        fillInIntent.putExtra(PARAM_MAINTENANCE_ROW, position);

        remoteViews.setOnClickFillInIntent(R.id.notification_list, fillInIntent);

        return remoteViews;
    }

    @Override
    public int getCount() {
        return serviceNotifications.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("onDataSetChanged");
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}