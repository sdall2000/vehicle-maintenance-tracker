package android.example.vehiclemaintenancetracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MileageEntryDao {
    @Query("SELECT * FROM mileageEntry ORDER BY date DESC, uid DESC")
    List<MileageEntry> getAll();

    @Query("SELECT * FROM mileageEntry ORDER BY date DESC, uid DESC")
    LiveData<List<MileageEntry>> getAllLiveData();

    @Query("SELECT * FROM mileageEntry ORDER BY date DESC, uid DESC LIMIT 1")
    MileageEntry getMostRecentMileage();

    @Insert
    long insert(MileageEntry mileageEntry);

    @Delete
    void delete(MileageEntry mileageEntry);
}
