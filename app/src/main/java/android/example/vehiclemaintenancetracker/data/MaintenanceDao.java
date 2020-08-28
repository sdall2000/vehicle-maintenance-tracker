package android.example.vehiclemaintenancetracker.data;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MaintenanceDao {
    @Query("SELECT * FROM maintenance")
    List<Maintenance> getAll();

    @Query("SELECT * FROM maintenance where uid = :uid")
    Maintenance getMaintenance(int uid);
}
