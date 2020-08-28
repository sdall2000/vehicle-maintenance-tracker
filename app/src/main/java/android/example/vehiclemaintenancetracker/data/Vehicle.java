package android.example.vehiclemaintenancetracker.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Vehicle {
    @PrimaryKey(autoGenerate = true)
    private final int uid;

    private final String name;
    private final String description;
    private final int maintenanceScheduleUid;
    private final int startingMileageEntryUid;

    @Ignore
    public Vehicle(
            String name,
            String description,
            int maintenanceScheduleUid,
            int startingMileageEntryUid) {
        this(0, name, description, maintenanceScheduleUid, startingMileageEntryUid);
    }

    public Vehicle(
            int uid,
            String name,
            String description,
            int maintenanceScheduleUid,
            int startingMileageEntryUid) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.maintenanceScheduleUid = maintenanceScheduleUid;
        this.startingMileageEntryUid = startingMileageEntryUid;
    }
    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaintenanceScheduleUid() {
        return maintenanceScheduleUid;
    }

    public int getStartingMileageEntryUid() {
        return startingMileageEntryUid;
    }
}
