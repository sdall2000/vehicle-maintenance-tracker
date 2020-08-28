package android.example.vehiclemaintenancetracker.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MileageEntry.class, MaintenanceEntry.class, Vehicle.class, Maintenance.class, MaintenanceSchedule.class, MaintenanceScheduleDetail.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "vehicleMaintenanceDatabase.db";
    private static final String PREPOPULATED_DATABASE_ASSET = "database/maintenance.db";

    public static final String MAINTENANCE_SCHEDULE_UID_KEY = "maintenanceScheduleUid";

    private static final String SHARED_PREFERENCES_KEY = "VehicheMaintenanceSharedPreferences";

    private static final String DAY_WARNING_THRESHOLD_KEY = "dayWarningThreshold";
    private static final String MILEAGE_WARNING_THRESHOLD_KEY = "mileageWarningThreshold";

    private static final int DEFAULT_DAY_WARNING_THRESHOLD = 10;
    private static final int DEFAULT_MILEAGE_WARNING_THRESHOLD = 100;

    // This class holds the singleton.
    private static volatile AppDatabase instance;

    public abstract MileageEntryDao getMileageEntryDao();

    public abstract MaintenanceEntryDao getMaintenanceEntryDao();

    public abstract VehicleDao getVehicleDao();

    public abstract MaintenanceDao getMaintenanceDao();
    public abstract MaintenanceScheduleDao getMaintenanceScheduleDao();
    public abstract MaintenanceScheduleDetailDao getMaintenanceScheduleDetailDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    DB_NAME)
                    .createFromAsset(PREPOPULATED_DATABASE_ASSET)
                    .build();
        }

        return instance;
    }

    public static int getDayWarningThreshold(Context context) {
        return getSharedPreferences(context).getInt(DAY_WARNING_THRESHOLD_KEY, DEFAULT_DAY_WARNING_THRESHOLD);
    }

    // We don't use the setter yet, but eventually this setting would be part of the user
    // modifiable configuration.
    public static void setDayWarningThreshold(Context context, Integer dayWarningThreshold) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        if (dayWarningThreshold != null) {
            editor.putInt(DAY_WARNING_THRESHOLD_KEY, dayWarningThreshold);
        } else {
            editor.remove(DAY_WARNING_THRESHOLD_KEY);
        }

        editor.apply();
    }

    public static int getMileageWarningThreshold(Context context) {
        return getSharedPreferences(context).getInt(MILEAGE_WARNING_THRESHOLD_KEY, DEFAULT_MILEAGE_WARNING_THRESHOLD);
    }

    // We don't use the setter yet, but eventually this setting would be part of the user
    // modifiable configuration.
    public static void setMileageWarningThreshold(Context context, Integer mileageWarningThreshold) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        if (mileageWarningThreshold != null) {
            editor.putInt(MILEAGE_WARNING_THRESHOLD_KEY, mileageWarningThreshold);
        } else {
            editor.remove(MILEAGE_WARNING_THRESHOLD_KEY);
        }

        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public void deleteData() {
        getMaintenanceEntryDao().deleteAll();
        getMileageEntryDao().deleteAll();
        getVehicleDao().deleteAll();
    }
}
