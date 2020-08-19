package android.example.vehiclemaintenancetracker.data;

import androidx.room.Entity;

import java.util.Date;

/**
 * Join between vehicle and mileage, in order to populate the starting mileage/date.
 */
@Entity
public class VehicleStartingMileage extends Vehicle {
    private int startingMileage;
    private Date startingDate;

    public VehicleStartingMileage(int uid, String name, String description, String maintenanceScheduleUid, int startingMileageEntryUid, int startingMileage, Date startingDate) {
        super(uid, name, description, maintenanceScheduleUid, startingMileageEntryUid);

        this.startingMileage = startingMileage;
        this.startingDate = startingDate;
    }

    public int getStartingMileage() {
        return startingMileage;
    }

    public Date getStartingDate() {
        return startingDate;
    }
}
