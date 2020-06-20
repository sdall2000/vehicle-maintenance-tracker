package android.example.vehiclemaintenancetracker.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import timber.log.Timber;

@Database(entities = {MileageEntry.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "vehicleMaintenanceDatabase.db";
    public static final String SELECTED_VEHICLE_UID_KEY = "selectedVehicleUid";

    // This class holds the singleton.
    private static volatile AppDatabase instance;

    public abstract MileageEntryDao getMileageEntryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    DB_NAME).build();
        }

        return instance;
    }

    /**
     * Gets the selected vehicle uid
     *
     * @param activity The activity to use for fetching the shared preferences
     * @return The vehicle uid, or null if it is not set.
     */
    public static String getVehicleUid(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        String vehicleUid = preferences.getString(SELECTED_VEHICLE_UID_KEY, null);

        Timber.d("Got vehicle uid " + vehicleUid + " from preferences.");

        return vehicleUid;
    }

    public static void setVehicleUid(Activity activity, String vehicleUid) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (!TextUtils.isEmpty(vehicleUid)) {
            editor.putString(SELECTED_VEHICLE_UID_KEY, vehicleUid);
        } else {
            editor.remove(SELECTED_VEHICLE_UID_KEY);
        }

        Timber.d("Set vehicle id to " + vehicleUid + " in preferences.");

        editor.commit();
    }
}
