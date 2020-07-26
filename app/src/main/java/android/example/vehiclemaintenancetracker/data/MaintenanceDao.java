package android.example.vehiclemaintenancetracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MaintenanceDao {
    @Query("SELECT * FROM maintenanceEntry")
    List<MaintenanceEntry> getAll();

    @Query("SELECT * FROM maintenanceEntry")
    LiveData<List<MaintenanceEntry>> getAllLiveData();

    @Insert
    void insert(MaintenanceEntry maintenanceEntry);
}
