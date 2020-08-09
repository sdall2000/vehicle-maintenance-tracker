package android.example.vehiclemaintenancetracker.utilities;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Use the pattern from the Udacity course.
public class AppExecutor {
    private static AppExecutor instance;

    private final Executor dbExecutor;
    private final Executor uiExecutor;

    private AppExecutor(Executor dbExecutor, Executor uiExecutor) {
        this.dbExecutor = dbExecutor;
        this.uiExecutor = uiExecutor;
    }

    public synchronized static AppExecutor getInstance() {
        if (instance == null) {
            instance = new AppExecutor(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        }

        return instance;
    }

    public Executor getDbExecutor() {
        return dbExecutor;
    }

    public Executor getUiExecutor() {
        return uiExecutor;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
