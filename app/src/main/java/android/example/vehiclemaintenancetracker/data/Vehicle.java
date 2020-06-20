package android.example.vehiclemaintenancetracker.data;

public class Vehicle {
    private int year;
    private String make;
    private String model;
    private String maintenanceScheduleUid;

    public Vehicle() { };

    public Vehicle(int year, String make, String model, String maintenanceScheduleUid) {
        this.year = year;
        this.make = make;
        this.model = model;
        this.maintenanceScheduleUid = maintenanceScheduleUid;
    }

    public int getYear() {
        return year;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getMaintenanceScheduleUid() {
        return maintenanceScheduleUid;
    }
}
