package android.example.vehiclemaintenancetracker.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MileageEntry.class}, version=1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "vehicleMaintenanceDatabase.db";

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
}
