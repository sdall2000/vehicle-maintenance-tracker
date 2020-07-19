package android.example.vehiclemaintenancetracker.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class MaintenanceEntry {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name="mileageUid")
    private long mileageUid;

    @ColumnInfo(name="maintenanceItemUid")
    private String maintenanceItemUid;

    @ColumnInfo(name="provider")
    private String provider;

    @ColumnInfo(name="cost")
    private Double cost;

    // Not part of the actual table, but this information will be populated
    // from the mileage uid.
    @Ignore
    private MileageEntry mileageEntry;

    public MaintenanceEntry(long mileageUid, String maintenanceItemUid, String provider, Double cost) {
        this.mileageUid = mileageUid;
        this.maintenanceItemUid = maintenanceItemUid;
        this.provider = provider;
        this.cost = cost;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setMileageUid(long mileageUid) {
        this.mileageUid = mileageUid;
    }

    public void setMaintenanceItemUid(String maintenanceItemUid) {
        this.maintenanceItemUid = maintenanceItemUid;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public void setMileageEntry(MileageEntry mileageEntry) {
        this.mileageEntry = mileageEntry;
    }

    public long getUid() {
        return uid;
    }

    public long getMileageUid() {
        return mileageUid;
    }

    public String getMaintenanceItemUid() {
        return maintenanceItemUid;
    }

    public String getProvider() {
        return provider;
    }

    public Double getCost() {
        return cost;
    }

    public MileageEntry getMileageEntry() {
        return mileageEntry;
    }
}
