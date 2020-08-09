package android.example.vehiclemaintenancetracker.utilities;

import android.example.vehiclemaintenancetracker.data.MaintenanceEntryJoined;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.Status;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ServiceNotificationGeneratorTest {
    private static final long MS_PER_DAY = 86_400_000;

    // Makes the tests a bit more readable.
    private static final int WARNING_THRESHOLD_HUNDRED_MILES = 100;
    private static final int WARNING_THRESHOLD_TEN_DAYS = 10;

    // Sunday, July 5, 2020 11:54:36 PM GMT
    private long referenceTimeMs = 1_593_993_276_000L;

    private int startingMiles = 0;
    // Tuesday, July 13, 2010 2:21:47 PM GMT
    private long startingDateMs = 1_279_030_907_000L;

    @Test
    public void testSimpleMileage() {
        // Oil change every 3,000 miles
        MaintenanceScheduleEntry maintenanceScheduleEntry = new MaintenanceScheduleEntry(
                "oil",
                "Oil Change",
                null,
                3000);

        Set<MaintenanceScheduleEntry> maintenanceScheduleEntries = new HashSet<>();
        maintenanceScheduleEntries.add(maintenanceScheduleEntry);

        MaintenanceEntryJoined maintenanceEntryJoined = new MaintenanceEntryJoined(
                "oil",
                null,
                null,
                3050,
                new Date());

        List<MaintenanceEntryJoined> maintenanceEntries = new ArrayList<>();
        maintenanceEntries.add(maintenanceEntryJoined);

        List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS,
                startingMiles,
                startingDateMs);

        assertEquals(1, serviceNotifications.size());

        ServiceNotification serviceNotification = serviceNotifications.get(0);

        assertEquals("Oil Change", serviceNotification.getService());
        assertEquals(6_050, (long) serviceNotification.getMileageDue());
        assertNull(serviceNotification.getDateDue());
        assertEquals(Status.Upcoming, serviceNotification.getOverallStatus());

        maintenanceEntryJoined = new MaintenanceEntryJoined(
                "oil",
                null,
                null,
                2950,
                new Date());

        maintenanceEntries.clear();
        maintenanceEntries.add(maintenanceEntryJoined);

        serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS,
                startingMiles,
                startingDateMs);

        assertEquals(1, serviceNotifications.size());

        serviceNotification = serviceNotifications.get(0);

        assertEquals("Oil Change", serviceNotification.getService());
        assertEquals(5_950, (long) serviceNotification.getMileageDue());
        assertNull(serviceNotification.getDateDue());
        assertEquals(Status.Overdue, serviceNotification.getOverallStatus());

        maintenanceEntryJoined = new MaintenanceEntryJoined(
                "oil",
                null,
                null,
                4000,
                new Date());

        maintenanceEntries.clear();
        maintenanceEntries.add(maintenanceEntryJoined);

        serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS,
                startingMiles,
                startingDateMs);

        assertEquals(0, serviceNotifications.size());
    }

    @Test
    public void testSimpleDate() {
        // Oil change every 90 days
        MaintenanceScheduleEntry maintenanceScheduleEntry = new MaintenanceScheduleEntry(
                "oil",
                "Oil Change",
                90,
                null);

        Set<MaintenanceScheduleEntry> maintenanceScheduleEntries = new HashSet<>();
        maintenanceScheduleEntries.add(maintenanceScheduleEntry);

        Date referenceDate = new Date(referenceTimeMs);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(referenceDate);
        calendar.add(Calendar.DATE, -85);
        Date previousDate = calendar.getTime();

        MaintenanceEntryJoined maintenanceEntryJoined = new MaintenanceEntryJoined(
                "oil",
                null,
                null,
                3050,
                previousDate);

        List<MaintenanceEntryJoined> maintenanceEntries = new ArrayList<>();
        maintenanceEntries.add(maintenanceEntryJoined);

        List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS,
                startingMiles,
                startingDateMs);

        assertEquals(1, serviceNotifications.size());

        ServiceNotification serviceNotification = serviceNotifications.get(0);

        assertEquals("Oil Change", serviceNotification.getService());

        // Calculate what the due date should be to see if it matches the service notification.
        calendar.setTime(previousDate);
        calendar.add(Calendar.DATE, 90);
        Date expectedDateDue = calendar.getTime();

        assertEquals(expectedDateDue, serviceNotification.getDateDue());
        assertEquals(Status.Upcoming, serviceNotification.getOverallStatus());
    }

    @Test
    public void testMileageNoExistingMaintenance() {
        // Oil change every 3,000 miles
        MaintenanceScheduleEntry maintenanceScheduleEntry = new MaintenanceScheduleEntry(
                "oil",
                "Oil Change",
                null,
                3_000);

        Set<MaintenanceScheduleEntry> maintenanceScheduleEntries = new HashSet<>();
        maintenanceScheduleEntries.add(maintenanceScheduleEntry);

        List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                null,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS,
                2_000,
                startingDateMs);

        assertEquals(1, serviceNotifications.size());

        ServiceNotification serviceNotification = serviceNotifications.get(0);

        assertEquals("Oil Change", serviceNotification.getService());
        assertEquals(5_000, (long) serviceNotification.getMileageDue());
        assertNull(serviceNotification.getDateDue());
        assertEquals(Status.Overdue, serviceNotification.getOverallStatus());
    }

    @Test
    public void testDateNoExistingMaintenance() {
        // Oil change every 90 days
        MaintenanceScheduleEntry maintenanceScheduleEntry = new MaintenanceScheduleEntry(
                "oil",
                "Oil Change",
                90,
                null);

        Set<MaintenanceScheduleEntry> maintenanceScheduleEntries = new HashSet<>();
        maintenanceScheduleEntries.add(maintenanceScheduleEntry);

        Date referenceDate = new Date(referenceTimeMs);

        // Calculate 85 days before the reference date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(referenceDate);
        calendar.add(Calendar.DATE, -85);
        Date startingDate = calendar.getTime();


        List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                null,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS,
                2_000,
                startingDate.getTime());

        assertEquals(1, serviceNotifications.size());

        ServiceNotification serviceNotification = serviceNotifications.get(0);

        // Calculate what the due date should be to see if it matches the service notification.
        calendar.setTime(startingDate);
        calendar.add(Calendar.DATE, 90);
        Date expectedDateDue = calendar.getTime();

        assertEquals("Oil Change", serviceNotification.getService());
        assertEquals(expectedDateDue, serviceNotification.getDateDue());
        assertEquals(Status.Upcoming, serviceNotification.getOverallStatus());
    }

    @Test
    public void testDateSingleNotification() {
        MaintenanceScheduleEntry maintenanceScheduleEntry = new MaintenanceScheduleEntry(
                "oil",
                "Oil Change",
                90,
                null);

        long goodLastServiceDateMs = addDays(referenceTimeMs, -79);
        long goodServiceDueDateMs = addDays(goodLastServiceDateMs, maintenanceScheduleEntry.getDayInterval());


        ServiceNotification serviceNotification = ServiceNotificationGenerator.generateServiceNotification(
                3_000,
                referenceTimeMs,
                0,
                goodLastServiceDateMs,
                maintenanceScheduleEntry,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS);

        assertEquals(Status.Good, serviceNotification.getDateStatus());
        assertEquals(Status.Good, serviceNotification.getOverallStatus());
        assertEquals(goodServiceDueDateMs, serviceNotification.getDateDue().getTime());

        long upcomingLastServiceDateMs = addDays(referenceTimeMs, -80);
        long upcomingServiceDueDateMs = addDays(upcomingLastServiceDateMs, maintenanceScheduleEntry.getDayInterval());

        serviceNotification = ServiceNotificationGenerator.generateServiceNotification(
                3_000,
                referenceTimeMs,
                0,
                upcomingLastServiceDateMs,
                maintenanceScheduleEntry,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS);

        assertEquals(Status.Upcoming, serviceNotification.getDateStatus());
        assertEquals(Status.Upcoming, serviceNotification.getOverallStatus());
        assertEquals(upcomingServiceDueDateMs, serviceNotification.getDateDue().getTime());

        long overdueLastServiceDateMs = addDays(referenceTimeMs, -90);
        long overdueServiceDueDateMs = addDays(overdueLastServiceDateMs, maintenanceScheduleEntry.getDayInterval());

        serviceNotification = ServiceNotificationGenerator.generateServiceNotification(
                3_000,
                referenceTimeMs,
                0,
                overdueLastServiceDateMs,
                maintenanceScheduleEntry,
                WARNING_THRESHOLD_HUNDRED_MILES,
                WARNING_THRESHOLD_TEN_DAYS);

        assertEquals(Status.Overdue, serviceNotification.getDateStatus());
        assertEquals(Status.Overdue, serviceNotification.getOverallStatus());
        assertEquals(overdueServiceDueDateMs, serviceNotification.getDateDue().getTime());
    }

    private long addDays(long referenceTimeMs, int days) {
        return referenceTimeMs + days * MS_PER_DAY;
    }
}