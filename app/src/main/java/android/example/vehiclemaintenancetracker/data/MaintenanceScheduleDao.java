package android.example.vehiclemaintenancetracker.data;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MaintenanceScheduleDao {
    @Query("SELECT * FROM maintenanceSchedule")
    List<Maintenance> getAll();

    @Query("SELECT * FROM maintenanceSchedule where uid = :uid")
    Maintenance getMaintenance(int uid);
}
