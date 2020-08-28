package android.example.vehiclemaintenancetracker.data;

import androidx.room.Entity;

@Entity
public class MaintenanceScheduleDetailJoined extends MaintenanceScheduleDetail {
    private String maintenanceName;
    private String maintenanceDescription;

    public MaintenanceScheduleDetailJoined(int uid,
                                           int maintenanceScheduleUid,
                                           int maintenanceUid,
                                           Integer mileInterval,
                                           Integer kilometerInterval,
                                           Integer dayInterval,
                                           String maintenanceName,
                                           String maintenanceDescription) {
        super(uid, maintenanceScheduleUid, maintenanceUid, mileInterval, kilometerInterval, dayInterval);
        this.maintenanceName = maintenanceName;
        this.maintenanceDescription = maintenanceDescription;
    }

    public String getMaintenanceName() {
        return maintenanceName;
    }

    public String getMaintenanceDescription() {
        return maintenanceDescription;
    }

    // This is what populates the spinner
    @Override
    public String toString() {
        return maintenanceName;
    }
}
