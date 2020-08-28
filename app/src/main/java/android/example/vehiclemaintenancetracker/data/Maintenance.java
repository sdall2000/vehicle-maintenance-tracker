package android.example.vehiclemaintenancetracker.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Maintenance {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @NonNull
    private String name;

    private String description;

    public Maintenance(int uid, String name, String description) {
        this.uid = uid;
        this.name = name;
        this.description = description;
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
}
