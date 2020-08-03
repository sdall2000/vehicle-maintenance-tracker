package android.example.vehiclemaintenancetracker.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.example.vehiclemaintenancetracker.model.VehicleInfo;
import android.text.TextUtils;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MileageEntry.class, MaintenanceEntry.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "vehicleMaintenanceDatabase.db";

    public static final String MAINTENANCE_SCHEDULE_UID_KEY = "maintenanceScheduleUid";

    private static final String SHARED_PREFERENCES_KEY = "VehicheMaintenanceSharedPreferences";

    private static final String SELECTED_VEHICLE_UID_KEY = "selectedVehicleUid";
    private static final String STARTING_MILEAGE_KEY = "startingMileageKey";
    private static final String STARTING_DATE_KEY = "startingDateKey";

    private static final String DAY_WARNING_THRESHOLD_KEY = "dayWarningThreshold";
    private static final String MILEAGE_WARNING_THRESHOLD_KEY = "mileageWarningThreshold";

    private static final int DEFAULT_DAY_WARNING_THRESHOLD = 10;
    private static final int DEFAULT_MILEAGE_WARNING_THRESHOLD = 100;

    // This class holds the singleton.
    private static volatile AppDatabase instance;

    public abstract MileageEntryDao getMileageEntryDao();

    public abstract MaintenanceDao getMaintenanceDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    DB_NAME).build();
        }

        return instance;
    }

    public static VehicleInfo getVehicleInfo(Context context) {
        VehicleInfo vehicleInfo = null;

        String vehicleUid = getVehicleUid(context);
        int startingMileage = getStartingMileage(context);
        long startingDateEpochMs = getStartingDateEpochMs(context);

        if (!TextUtils.isEmpty(vehicleUid)) {
            vehicleInfo = new VehicleInfo(vehicleUid, startingMileage, startingDateEpochMs);
        }

        return vehicleInfo;
    }

    public static void setVehicleInfo(Context context, VehicleInfo vehicleInfo) {
        if (vehicleInfo != null) {
            setVehicleUid(context, vehicleInfo.getVehicleUid());
            setStartingMileage(context, vehicleInfo.getStartingMileage());
            setStartingDateEpochMs(context, vehicleInfo.getStartingDateEpochMs());
        } else {
            // Pass nulls to clear settings.
            setVehicleUid(context, null);
            setStartingMileage(context, null);
            setStartingDateEpochMs(context, null);
        }
    }

    /**
     * Gets the selected vehicle uid
     *
     * @param context The context to use for fetching the shared preferences
     * @return The vehicle uid, or null if it is not set.
     */
    public static String getVehicleUid(Context context) {
        return getSharedPreferences(context).getString(SELECTED_VEHICLE_UID_KEY, null);
    }

    public static void setVehicleUid(Context context, String vehicleUid) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        if (!TextUtils.isEmpty(vehicleUid)) {
            editor.putString(SELECTED_VEHICLE_UID_KEY, vehicleUid);
        } else {
            editor.remove(SELECTED_VEHICLE_UID_KEY);
        }

        editor.apply();
    }

    public static int getStartingMileage(Context context) {
        return getSharedPreferences(context).getInt(STARTING_MILEAGE_KEY, 0);
    }

    public static void setStartingMileage(Context context, Integer startingMileage) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        if (startingMileage != null) {
            editor.putInt(STARTING_MILEAGE_KEY, startingMileage);
        } else {
            editor.remove(STARTING_MILEAGE_KEY);
        }

        editor.apply();
    }

    public static long getStartingDateEpochMs(Context context) {
        return getSharedPreferences(context).getLong(STARTING_DATE_KEY, 0);
    }

    public static void setStartingDateEpochMs(Context context, Long startingDateEpochMs) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        if (startingDateEpochMs != null) {
            editor.putLong(STARTING_DATE_KEY, startingDateEpochMs);
        } else {
            editor.remove(STARTING_DATE_KEY);
        }

        editor.apply();
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
        getMaintenanceDao().deleteAll();
        getMileageEntryDao().deleteAll();
    }
}
