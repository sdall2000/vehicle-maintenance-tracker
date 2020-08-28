package android.example.vehiclemaintenancetracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MaintenanceEntryDao {
    @Query(
            "SELECT MAINT.uid, MAINT.maintenanceItemUid, MAINT.provider, MAINT.cost, MLG.mileage, MLG.date " +
                    "FROM maintenanceEntry MAINT " +
                    "INNER JOIN mileageEntry MLG on MAINT.mileageUid = MLG.uid ORDER BY MLG.date DESC")
    List<MaintenanceEntryJoined> getAllJoined();

    @Query(
            "SELECT MAINT.uid, MAINT.maintenanceItemUid, MAINT.provider, MAINT.cost, MLG.mileage, MLG.date " +
                    "FROM maintenanceEntry MAINT " +
                    "INNER JOIN mileageEntry MLG on MAINT.mileageUid = MLG.uid ORDER BY MLG.date DESC")
    LiveData<List<MaintenanceEntryJoined>> getAllJoinedLiveData();

    @Insert
    void insert(MaintenanceEntry maintenanceEntry);

    @Query("DELETE FROM maintenanceEntry")
    void deleteAll();
}
