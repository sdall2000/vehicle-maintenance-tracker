package android.example.vehiclemaintenancetracker.model;

/**
 * Holds all of the vehicle info that is persisted as a shared preference.
 * This way we can make one query to get all of the info.
 */
public class VehicleInfo {
    private String vehicleUid;
    private int startingMileage;
    private long startingDateEpochMs;

    public VehicleInfo() { }

    public VehicleInfo(String vehicleUid, int startingMileage, long startingDateEpochMs) {
        this.vehicleUid = vehicleUid;
        this.startingMileage = startingMileage;
        this.startingDateEpochMs = startingDateEpochMs;
    }

    public String getVehicleUid() {
        return vehicleUid;
    }

    public void setVehicleUid(String vehicleUid) {
        this.vehicleUid = vehicleUid;
    }

    public int getStartingMileage() {
        return startingMileage;
    }

    public void setStartingMileage(int startingMileage) {
        this.startingMileage = startingMileage;
    }

    public long getStartingDateEpochMs() {
        return startingDateEpochMs;
    }

    public void setStartingDateEpochMs(long startingDateEpochMs) {
        this.startingDateEpochMs = startingDateEpochMs;
    }
}
