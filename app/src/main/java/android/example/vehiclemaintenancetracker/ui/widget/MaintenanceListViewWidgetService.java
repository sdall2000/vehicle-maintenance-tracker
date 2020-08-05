package android.example.vehiclemaintenancetracker.ui.widget;

import android.content.Context;
import android.content.Intent;
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

public class MaintenanceListViewWidgetService extends RemoteViewsService {
    public static final String SERVICE_NOTIFICATIONS = "Parcelable Service Notifications";
    public static final String PARAM_MAINTENANCE_ROW = "PARAM_MAINTENANCE_ROW";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MaintenanceListRemoteViewsFactory(getApplicationContext(), intent);
    }
}

class MaintenanceListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;

    private final List<ServiceNotification> serviceNotifications;

    public MaintenanceListRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;

        // Deserialize the service notifications.
        if (intent.hasExtra(MaintenanceListViewWidgetService.SERVICE_NOTIFICATIONS)) {

            String json = intent.getStringExtra(MaintenanceListViewWidgetService.SERVICE_NOTIFICATIONS);

            Gson gson = new Gson();
            ServiceNotificationCollection collection = gson.fromJson(json, ServiceNotificationCollection.class);

            serviceNotifications = collection.getServiceNotifications();

            Timber.d("Loaded %d notifications", serviceNotifications.size());
        } else {
            Timber.e("Remote view service created for list but there are no service notifications");
            serviceNotifications = new ArrayList<>();
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
        fillInIntent.putExtra(MaintenanceListViewWidgetService.PARAM_MAINTENANCE_ROW, position);

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
    }

    @Override
    public void onDestroy() {

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
