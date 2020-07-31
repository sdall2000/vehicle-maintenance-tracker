package android.example.vehiclemaintenancetracker.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Date;

@Entity
public class MaintenanceEntryJoined {
    @ColumnInfo(name="maintenanceItemUid")
    private String maintenanceItemUid;

    @ColumnInfo(name="provider")
    private String provider;

    @ColumnInfo(name="cost")
    private Double cost;

    @ColumnInfo(name = "mileage")
    private int mileage;

    @ColumnInfo(name = "date")
    private Date date;

    public MaintenanceEntryJoined(String maintenanceItemUid, String provider, Double cost, int mileage, Date date) {
        this.maintenanceItemUid = maintenanceItemUid;
        this.provider = provider;
        this.cost = cost;
        this.mileage = mileage;
        this.date = date;
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

    public int getMileage() {
        return mileage;
    }

    public Date getDate() {
        return date;
    }
}
