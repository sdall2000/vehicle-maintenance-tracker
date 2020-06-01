package android.example.vehiclemaintenancetracker.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class MileageEntry {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "mileage")
    private int mileage;

    @ColumnInfo(name = "date")
    private Date date;

    public MileageEntry(int mileage, Date date) {
        this.mileage = mileage;
        this.date = date;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getMileage() {
        return mileage;
    }

    public Date getDate() {
        return date;
    }
}
