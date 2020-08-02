package android.example.vehiclemaintenancetracker.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntryJoined;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.VehicleInfo;
import android.example.vehiclemaintenancetracker.ui.Styler;
import android.example.vehiclemaintenancetracker.utilities.ServiceNotificationGenerator;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaintenanceListViewWidgetService extends RemoteViewsService {
    public static final String PARAM_MAINTENANCE_ROW = "PARAM_MAINTENANCE_ROW";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MaintenanceListRemoteViewsFactory(getApplicationContext());
    }
}

class MaintenanceListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;

    private boolean dataLoaded = false;

    List<ServiceNotification> serviceNotifications = new ArrayList<>();

    public MaintenanceListRemoteViewsFactory(Context context) {
        this.context = context;
    }

    private void renderWidget() {

        final VehicleInfo vehicleInfo = AppDatabase.getVehicleInfo(context);

        if (vehicleInfo != null) {
            // Load mileage entries, so we can get the most recent recorded mileage.
            final List<MileageEntry> mileageEntries = AppDatabase.getInstance(context).getMileageEntryDao().getAll();

            // Load the maintenance performed.
            final List<MaintenanceEntryJoined> maintenanceEntries = AppDatabase.getInstance(context).getMaintenanceDao().getAllJoined();

            // Now fetch the vehicle so we can get the maintenanceEntries schedule uid.
            FirebaseDatabaseUtils.getInstance().getVehicle(vehicleInfo.getVehicleUid(), new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
                @Override
                public void onDataReady(final Vehicle vehicle) {
                    FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
                        @Override
                        public void onDataReady(Set<MaintenanceScheduleEntry> maintenanceScheduleEntries) {
                            // Now we should be able to calculate the notifications.
                            calculateServiceNotifications(
                                    vehicleInfo,
                                    mileageEntries,
                                    maintenanceEntries,
                                    maintenanceScheduleEntries);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void calculateServiceNotifications(
            VehicleInfo vehicleInfo,
            List<MileageEntry> mileageEntries,
            List<MaintenanceEntryJoined> maintenanceEntries,
            Set<MaintenanceScheduleEntry> maintenanceScheduleEntries) {

        MileageEntry mostRecentMileage = null;

        if (mileageEntries != null && mileageEntries.size() > 0) {
            mostRecentMileage = mileageEntries.get(0);
        }

        int currentMileage = mostRecentMileage != null ? mostRecentMileage.getMileage() : vehicleInfo.getStartingMileage();
        long currentDate = System.currentTimeMillis();

        // Get the vehicle UID, start date, and start mileage.
        serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                currentMileage,
                currentDate,
                maintenanceScheduleEntries,
                maintenanceEntries,
                AppDatabase.getMileageWarningThreshold(context),
                AppDatabase.getDayWarningThreshold(context),
                vehicleInfo.getStartingMileage(),
                vehicleInfo.getStartingDateEpochMs()
        );

        // Notify the widget that it should re-render.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VehicleMaintenanceTrackerAppWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.maintenanceWidgetListView);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (!dataLoaded) {
            dataLoaded = true;
            renderWidget();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return serviceNotifications.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        ServiceNotification serviceNotification = serviceNotifications.get(position);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_list_content);

        remoteViews.setTextViewText(R.id.textViewService, serviceNotification.getService());

        if (serviceNotification.getMileageDue() != null) {
            remoteViews.setTextViewText(R.id.textViewMileage, ValueFormatter.formatDistance(serviceNotification.getMileageDue()));
            Styler.styleResourceStatus(context, remoteViews, R.id.textViewMileage, serviceNotification.getMileageStatus());
        }

        if (serviceNotification.getDateDue() != null) {
            remoteViews.setTextViewText(R.id.textViewDate, DateConverter.convertDateToString(context, serviceNotification.getDateDue()));
            Styler.styleResourceStatus(context, remoteViews, R.id.textViewDate, serviceNotification.getDateStatus());
        }

        // Set on click to launch main app.
        Intent fillInIntent = new Intent();

        // This is extra is not currently used, but in the future we could consider launching
        // the add maintenance activity for the specific maintenance row.
        fillInIntent.putExtra(MaintenanceListViewWidgetService.PARAM_MAINTENANCE_ROW, position);

        remoteViews.setOnClickFillInIntent(R.id.notification_list, fillInIntent);

        return remoteViews;
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
