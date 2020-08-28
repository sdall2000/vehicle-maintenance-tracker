package android.example.vehiclemaintenancetracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VehicleDao {
    // Get all vehicles
    @Query("SELECT * FROM vehicle")
    List<Vehicle> getVehicles();

    // Get all vehicles
    @Query("SELECT * FROM vehicle")
    LiveData<List<Vehicle>> getVehiclesLive();

    // Get a specific vehicle
    @Query("SELECT * FROM vehicle WHERE UID = :uid")
    Vehicle getVehicle(int uid);

    @Insert
    long insert(Vehicle vehicle);

    @Delete
    void delete(Vehicle vehicle);

    @Query("DELETE FROM vehicle")
    void deleteAll();

    // Joins
    @Query("SELECT V.*, MLG.mileage as startingMileage, MLG.date as startingDate FROM vehicle V, mileageEntry MLG where V.startingMileageEntryUid=MLG.uid")
    List<VehicleStartingMileage> getVehicleStartingMileage();

    @Query("SELECT V.*, MLG.mileage as startingMileage, MLG.date as startingDate FROM vehicle V, mileageEntry MLG where V.startingMileageEntryUid=MLG.uid")
    LiveData<List<VehicleStartingMileage>> getVehicleStartingMileageLive();
}
