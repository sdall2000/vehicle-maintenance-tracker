package android.example.vehiclemaintenancetracker.data;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MaintenanceScheduleDetailDao {
    @Query("SELECT * FROM maintenanceScheduleDetail")
    List<MaintenanceScheduleDetail> getAll();

    @Query("SELECT * FROM maintenanceScheduleDetail where maintenanceScheduleUid = :maintenanceScheduleUid")
    List<MaintenanceScheduleDetail> getAllByMaintenanceSchedule(int maintenanceScheduleUid);

    @Query("SELECT * FROM maintenanceScheduleDetail where uid = :uid")
    MaintenanceScheduleDetail getMaintenanceScheduleDetail(int uid);

    @Query("SELECT MSD.*, M.name AS maintenanceName, M.description AS maintenanceDescription FROM maintenanceScheduleDetail MSD, maintenance M where MSD.maintenanceUid = M.uid and MSD.maintenanceScheduleUid = :maintenanceScheduleUid")
    List<MaintenanceScheduleDetailJoined> getMaintenanceScheduleDetailJoined(int maintenanceScheduleUid);
}
