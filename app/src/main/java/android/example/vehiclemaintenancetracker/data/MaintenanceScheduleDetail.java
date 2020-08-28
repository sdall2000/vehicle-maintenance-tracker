package android.example.vehiclemaintenancetracker.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MaintenanceScheduleDetail {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @NonNull
    private int maintenanceScheduleUid;

    @NonNull
    private int maintenanceUid;

    private Integer mileInterval;
    private Integer kilometerInterval;
    private Integer dayInterval;

    public MaintenanceScheduleDetail(int uid, int maintenanceScheduleUid, int maintenanceUid, Integer mileInterval, Integer kilometerInterval, Integer dayInterval) {
        this.uid = uid;
        this.maintenanceScheduleUid = maintenanceScheduleUid;
        this.maintenanceUid = maintenanceUid;
        this.mileInterval = mileInterval;
        this.kilometerInterval = kilometerInterval;
        this.dayInterval = dayInterval;
    }

    public int getUid() {
        return uid;
    }

    public int getMaintenanceScheduleUid() {
        return maintenanceScheduleUid;
    }

    public int getMaintenanceUid() {
        return maintenanceUid;
    }

    public Integer getMileInterval() {
        return mileInterval;
    }

    public Integer getKilometerInterval() {
        return kilometerInterval;
    }

    public Integer getDayInterval() {
        return dayInterval;
    }
}
