package android.example.vehiclemaintenancetracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MaintenanceDao {
    @Query("SELECT * FROM maintenanceEntry")
    List<MaintenanceEntry> getAll();

    @Insert
    void insert(MaintenanceEntry maintenanceEntry);
}
