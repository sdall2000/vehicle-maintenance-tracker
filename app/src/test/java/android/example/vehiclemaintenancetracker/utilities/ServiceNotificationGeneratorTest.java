package android.example.vehiclemaintenancetracker.utilities;

import android.example.vehiclemaintenancetracker.data.MaintenanceEntry;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.Status;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ServiceNotificationGeneratorTest {
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

        // Add an oil change service that was performed at 3,050 miles.  That means the next
        // one would be due at 6,050.
        MaintenanceEntry maintenanceEntry = new MaintenanceEntry(
                1, "oil", "Jiffy Lube", 30.0);

        MileageEntry mileageEntry = new MileageEntry(3050, new Date());
        maintenanceEntry.setMileageEntry(mileageEntry);

        Set<MaintenanceEntry> maintenanceEntries = new HashSet<>();
        maintenanceEntries.add(maintenanceEntry);

        List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                100,
                10,
                startingMiles,
                startingDateMs);

        assertEquals(1, serviceNotifications.size());

        ServiceNotification serviceNotification = serviceNotifications.get(0);

        assertEquals("Oil Change", serviceNotification.getService());
        assertEquals(6_050, (long) serviceNotification.getMileageDue());
        assertNull(serviceNotification.getDateDue());
        assertEquals(Status.Upcoming, serviceNotification.getOverallStatus());

        // Change the mileage entry for the existing maintenance entry.
        mileageEntry = new MileageEntry(2950, new Date());
        maintenanceEntry.setMileageEntry(mileageEntry);

        serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                100,
                10,
                startingMiles,
                startingDateMs);

        assertEquals(1, serviceNotifications.size());

        serviceNotification = serviceNotifications.get(0);

        assertEquals("Oil Change", serviceNotification.getService());
        assertEquals(5_950, (long) serviceNotification.getMileageDue());
        assertNull(serviceNotification.getDateDue());
        assertEquals(Status.Overdue, serviceNotification.getOverallStatus());

        // Change the mileage entry so we don't get a service notification.
        mileageEntry = new MileageEntry(4000, new Date());
        maintenanceEntry.setMileageEntry(mileageEntry);

        serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                100,
                10,
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

        // Add an oil change service at the reference time.
        MaintenanceEntry maintenanceEntry = new MaintenanceEntry(
                1, "oil", "Jiffy Lube", 30.0);

        Date referenceDate = new Date(referenceTimeMs);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(referenceDate);
        calendar.add(Calendar.DATE, -85);
        Date previousDate = calendar.getTime();

        MileageEntry mileageEntry = new MileageEntry(3050, previousDate);
        maintenanceEntry.setMileageEntry(mileageEntry);

        Set<MaintenanceEntry> maintenanceEntries = new HashSet<>();
        maintenanceEntries.add(maintenanceEntry);

        List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                6_000,
                referenceTimeMs,
                maintenanceScheduleEntries,
                maintenanceEntries,
                100,
                10,
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
                100,
                10,
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
                100,
                10,
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
}