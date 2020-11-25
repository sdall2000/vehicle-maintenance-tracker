package android.example.vehiclemaintenancetracker;

import android.app.Application;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class VehicleMaintenanceTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // This is the main reason a custom application object was created.
        // With this line in the MainActivity, a new tree would be "planted" every time
        // the activity was created, including screen rotations.  This caused duplicate
        // messages to be displayed.
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected void log(int priority, String tag, @NotNull String message, Throwable t) {
                super.log(priority, "*** timber *** " + tag, message, t);
            }
        });
    }
}
