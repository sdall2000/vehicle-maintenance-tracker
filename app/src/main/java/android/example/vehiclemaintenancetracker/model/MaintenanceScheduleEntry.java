package android.example.vehiclemaintenancetracker.model;

public class MaintenanceScheduleEntry {
    private int maintenanceItemId;
    private String maintenance;
    private Integer dayInterval;
    private Integer mileageInterval;

    public MaintenanceScheduleEntry() {

    }

    public MaintenanceScheduleEntry(int maintenanceItemId, String maintenance, Integer dayInterval, Integer mileageInterval) {
        this.maintenanceItemId = maintenanceItemId;
        this.maintenance = maintenance;
        this.dayInterval = dayInterval;
        this.mileageInterval = mileageInterval;
    }

    public int getMaintenanceItemId() {
        return maintenanceItemId;
    }

    public void setMaintenance(String maintenance) {
        this.maintenance = maintenance;
    }

    public String getMaintenance() {
        return maintenance;
    }

    public Integer getDayInterval() {
        return dayInterval;
    }

    public Integer getMileageInterval() {
        return mileageInterval;
    }

    // This override is mainly to support showing just the maintenance text in the spinner.
    @Override
    public String toString() {
        return maintenance;
    }
}
