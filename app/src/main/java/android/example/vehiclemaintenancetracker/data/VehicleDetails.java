package android.example.vehiclemaintenancetracker.data;

import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;

import java.util.Set;

/**
 * Holds all static data related to a vehicle
 */
public class VehicleDetails {
    private String vehicleUid;
    private Vehicle vehicle;
    private Set<MaintenanceScheduleEntry> maintenanceSchedule;

    public VehicleDetails(String vehicleUid, Vehicle vehicle, Set<MaintenanceScheduleEntry> maintenanceSchedule) {
        this.vehicleUid = vehicleUid;
        this.vehicle = vehicle;
        this.maintenanceSchedule = maintenanceSchedule;
    }

    public String getVehicleUid() {
        return vehicleUid;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Set<MaintenanceScheduleEntry> getMaintenanceSchedule() {
        return maintenanceSchedule;
    }
}
