package android.example.vehiclemaintenancetracker.utilities;

import android.example.vehiclemaintenancetracker.data.MaintenanceEntry;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.Status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ServiceNotificationGenerator {

    private static final long MS_PER_DAY = 1_000L * 86_400;

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
            Set<MaintenanceEntry> maintenanceEntries,
            int mileageWarningThreshold,
            int dateWarningThresholdDays,
            int startingMileage,
            long startingDateEpochMs) {

        List<ServiceNotification> serviceNotifications = new ArrayList<>();

        // For each maintenance schedule entry, figure out if a notification is needed.
        for (MaintenanceScheduleEntry scheduleEntry : maintenanceScheduleEntries) {
            MaintenanceEntry mostRecentMaintenanceEntry = getMostRecentMaintenanceEntry(scheduleEntry.getMaintenanceItemId(), maintenanceEntries);

            // Default last mileage and date to vehicle starting values.
            int lastServiceMileage = startingMileage;
            long lastServiceDateEpochMs = startingDateEpochMs;

            // See if there was a maintenance entry
            if (mostRecentMaintenanceEntry != null) {
                // Update the last service variables to match the maintenance.
                lastServiceMileage = mostRecentMaintenanceEntry.getMileageEntry().getMileage();
                lastServiceDateEpochMs = mostRecentMaintenanceEntry.getMileageEntry().getDate().getTime();
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
            if (serviceNotification.getStatus() != Status.Good) {
                serviceNotifications.add(serviceNotification);
            }
        }

        return serviceNotifications;
    }

    private static MaintenanceEntry getMostRecentMaintenanceEntry(String maintenanceItemId, Set<MaintenanceEntry> maintenanceEntries) {
        MaintenanceEntry maintenanceEntry = null;

        if (maintenanceEntries != null) {
            for (MaintenanceEntry entry : maintenanceEntries) {
                if (entry.getMaintenanceItemUid().equals(maintenanceItemId)) {
                    if (maintenanceEntry == null) {
                        maintenanceEntry = entry;
                    } else if (entry.getMileageEntry().getDate().getTime() > maintenanceEntry.getMileageEntry().getDate().getTime()) {
                        maintenanceEntry = entry;
                    }
                }
            }
        }

        return maintenanceEntry;
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

        ServiceNotification serviceNotification = null;


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
        }

        // See if we care about date criteria.
        if (maintenanceScheduleEntry.getDayInterval() != null) {
            long dayDeltaMs = currentDateEpochMs - lastServiceDateEpochMs;
            int dayDelta = Math.round(dayDeltaMs / MS_PER_DAY);

            if (dayDelta >= maintenanceScheduleEntry.getDayInterval()) {
                dayStatus = Status.Overdue;
            } else if (dayDelta + dateWarningThresholdDays >= maintenanceScheduleEntry.getDayInterval()) {
                dayStatus = Status.Upcoming;
            }
        }

        // Combine the two statuses
        Status overallStatus = getBlendedStatus(dayStatus, mileageStatus);

        // Create our service notification
        serviceNotification = new ServiceNotification(maintenanceScheduleEntry.getMaintenance(), mileageDue, dateDue, overallStatus);

        return serviceNotification;
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
}
