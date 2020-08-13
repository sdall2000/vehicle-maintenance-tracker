package android.example.vehiclemaintenancetracker.utilities;

import android.content.Context;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntryJoined;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.VehicleDetails;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.Status;
import android.example.vehiclemaintenancetracker.model.VehicleInfo;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class ServiceNotificationGenerator {

    private static final float MS_PER_DAY = 1_000.0f * 86_400.0f;

    /**
     * @param currentMileage             Represents the last recorded mileage of the vehicle for calculating when service is due based on mileage
     * @param currentDateEpochMs         Represents the date reference for calculating when service is due based on date.  Typically would just be the current date.
     * @param maintenanceScheduleEntries The maintenance schedule for the vehicle
     * @param maintenanceEntries         Existing vehicle maintenance entries for when the vehicle was actually serviced
     * @param mileageWarningThreshold    Defines the criteria to use for noting upcoming maintenance based on mileage
     * @param dateWarningThresholdDays   Defines the criteria to use for noting upcoming maintenance based on elapsed time between service
     * @param startingMileage            The mileage when the vehicle was purchased
     * @param startingDateEpochMs        The date the vehicle was purchased
     * @return A list of service notifications
     */
    public static List<ServiceNotification> generateServiceNotifications(
            int currentMileage,
            long currentDateEpochMs,
            Set<MaintenanceScheduleEntry> maintenanceScheduleEntries,
            List<MaintenanceEntryJoined> maintenanceEntries,
            int mileageWarningThreshold,
            int dateWarningThresholdDays,
            int startingMileage,
            long startingDateEpochMs) {

        List<ServiceNotification> serviceNotifications = new ArrayList<>();

        // For each maintenance schedule entry, figure out if a notification is needed.
        for (MaintenanceScheduleEntry scheduleEntry : maintenanceScheduleEntries) {
            MaintenanceEntryJoined mostRecentMaintenanceEntry = getMostRecentMaintenanceEntry(scheduleEntry.getMaintenanceItemId(), maintenanceEntries);

            // Default last mileage and date to vehicle starting values.
            int lastServiceMileage = startingMileage;
            long lastServiceDateEpochMs = startingDateEpochMs;

            // See if there was a maintenance entry
            if (mostRecentMaintenanceEntry != null) {
                // Update the last service variables to match the maintenance.
                lastServiceMileage = mostRecentMaintenanceEntry.getMileage();
                lastServiceDateEpochMs = mostRecentMaintenanceEntry.getDate().getTime();
            }

            ServiceNotification serviceNotification = generateServiceNotification(
                    currentMileage,
                    currentDateEpochMs,
                    lastServiceMileage,
                    lastServiceDateEpochMs,
                    scheduleEntry,
                    mileageWarningThreshold,
                    dateWarningThresholdDays);

            // We are only including service that is needed.  Ignore status of Good.
            if (serviceNotification.getOverallStatus() != Status.Good) {
                serviceNotifications.add(serviceNotification);
            }
        }

        return serviceNotifications;
    }

    /**
     * This method generates the service notifications by pulling the required data from their
     * various sources - shared preferences, SQLite, Firebase, etc.
     *
     * @param context                     The application context
     * @param serviceNotificationListener The listener to receive the service notifications
     */
    public static void generateServiceNotifications(final Context context, final ServiceNotificationsListener serviceNotificationListener) {
        final VehicleInfo vehicleInfo = AppDatabase.getVehicleInfo(context);

        if (vehicleInfo != null) {
            // Load mileage entries, so we can get the most recent recorded mileage.
            final List<MileageEntry> mileageEntries = AppDatabase.getInstance(context).getMileageEntryDao().getAll();

            // Load the maintenance performed.
            final List<MaintenanceEntryJoined> maintenanceEntries = AppDatabase.getInstance(context).getMaintenanceDao().getAllJoined();

            FirebaseDatabaseUtils.getInstance().getVehicleDetails(vehicleInfo.getVehicleUid(), new FirebaseDatabaseUtils.HelperListener<VehicleDetails>() {
                @Override
                public void onDataReady(VehicleDetails vehicleDetails) {
                    // Now we should be able to calculate the notifications.
                    MileageEntry mostRecentMileage = null;

                    if (mileageEntries != null && mileageEntries.size() > 0) {
                        mostRecentMileage = mileageEntries.get(0);
                    }

                    int currentMileage = mostRecentMileage != null ? mostRecentMileage.getMileage() : vehicleInfo.getStartingMileage();
                    long currentDate = System.currentTimeMillis();

                    List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                            currentMileage,
                            currentDate,
                            vehicleDetails.getMaintenanceSchedule(),
                            maintenanceEntries,
                            AppDatabase.getMileageWarningThreshold(context),
                            AppDatabase.getDayWarningThreshold(context),
                            vehicleInfo.getStartingMileage(),
                            vehicleInfo.getStartingDateEpochMs());

                    serviceNotificationListener.onNotificationsReady(serviceNotifications);                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.e("Database error when retrieving vehicle details: %s", databaseError.getMessage());
                    serviceNotificationListener.onError(databaseError.getMessage());
                }
            });
        } else {
            // No vehicle is selected.  Just return an empty list of service notifications.
            serviceNotificationListener.onNotificationsReady(new ArrayList<ServiceNotification>());
        }
    }

    private static MaintenanceEntryJoined getMostRecentMaintenanceEntry(String maintenanceItemId, List<MaintenanceEntryJoined> maintenanceEntries) {
        // Maintenance entries are sorted by date descending.  Return the first match if any.
        if (maintenanceEntries != null) {
            for (MaintenanceEntryJoined entry : maintenanceEntries) {
                if (entry.getMaintenanceItemUid().equals(maintenanceItemId)) {
                    return entry;
                }
            }
        }

        return null;
    }

    /**
     * @param currentMileage           The vehicle's current mileage
     * @param currentDateEpochMs       The current date in ms since epoch
     * @param lastServiceMileage       The mileage when the vehicle had the service, or if never, the vehicle start mileage
     * @param lastServiceDateEpochMs   The date when the vehicle had the sservice, or if never, the vehicle start date
     * @param maintenanceScheduleEntry The maintenance being checked
     * @param mileageWarningThreshold  Defines the criteria to use for noting upcoming maintenance based on mileage
     * @param dateWarningThresholdDays Defines the criteria to use for noting upcoming maintenance based on elapsed time between service
     * @return The populated service notification
     */
    public static ServiceNotification generateServiceNotification(
            int currentMileage,
            long currentDateEpochMs,
            int lastServiceMileage,
            long lastServiceDateEpochMs,
            MaintenanceScheduleEntry maintenanceScheduleEntry,
            int mileageWarningThreshold,
            int dateWarningThresholdDays) {

        // Initialize mileage status variables.
        Status mileageStatus = Status.Good;
        Integer mileageDue = null;

        // See if mileage applies to this maintenance schedule entry
        if (maintenanceScheduleEntry.getMileageInterval() != null) {
            mileageDue = lastServiceMileage + maintenanceScheduleEntry.getMileageInterval();

            // Determine how many miles have passed since the last service.
            int milesDelta = currentMileage - lastServiceMileage;

            if (milesDelta >= maintenanceScheduleEntry.getMileageInterval()) {
                mileageStatus = Status.Overdue;
            } else if (milesDelta + mileageWarningThreshold >= maintenanceScheduleEntry.getMileageInterval()) {
                mileageStatus = Status.Upcoming;
            }
        }

        // Initialize date status variables.
        Status dayStatus = Status.Good;
        Date dateDue = null;

        // See if the schedule maintenance has a date component.
        if (maintenanceScheduleEntry.getDayInterval() != null) {
            // For example, the day interval might be 90 days.
            // If it has been 85 days since the last service, this should be flagged
            // as upcoming.
            // If it has been 100 days, it should be flagged as overdue.

            // The last time the service was done + dayInterval = when it should be done.
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(lastServiceDateEpochMs));
            calendar.add(Calendar.DATE, maintenanceScheduleEntry.getDayInterval());
            dateDue = calendar.getTime();

            // Calculate how many days it has been since the last service or vehicle start date.
            long dayDeltaMs = currentDateEpochMs - lastServiceDateEpochMs;
            int dayDelta = Math.round(dayDeltaMs / MS_PER_DAY);

            // See if the day delta exceeds the upcoming or overdue timeframes.
            if (dayDelta >= maintenanceScheduleEntry.getDayInterval()) {
                dayStatus = Status.Overdue;
            } else if (dayDelta + dateWarningThresholdDays >= maintenanceScheduleEntry.getDayInterval()) {
                dayStatus = Status.Upcoming;
            }
        }

        // Combine the two statuses
        Status overallStatus = getBlendedStatus(dayStatus, mileageStatus);

        // Create our service notification
        return new ServiceNotification(
                maintenanceScheduleEntry.getMaintenance(),
                mileageDue,
                dateDue,
                mileageStatus,
                dayStatus,
                overallStatus);
    }


    private static Status getBlendedStatus(Status status1, Status status2) {
        Status status = Status.Good;

        if (status1 == Status.Overdue || status2 == Status.Overdue) {
            status = Status.Overdue;
        } else if (status1 == Status.Upcoming || status2 == Status.Upcoming) {
            status = Status.Upcoming;
        }

        return status;
    }

    public interface ServiceNotificationsListener {
        void onNotificationsReady(List<ServiceNotification> serviceNotifications);

        void onError(String error);
    }
}
