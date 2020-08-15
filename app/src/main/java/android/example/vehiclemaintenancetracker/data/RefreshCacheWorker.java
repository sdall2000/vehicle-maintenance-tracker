package android.example.vehiclemaintenancetracker.data;

import android.content.Context;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.VehicleInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseError;

import java.util.Set;

import timber.log.Timber;

/**
 * Listenable worker invoked by the work manager.  This class is responsible for requerying
 * the maintenance schedule in case anything changed.  Then an intent is fired that components
 * can listen to in order to re-render, for example, the service notifications fragment.
 */
public class RefreshCacheWorker extends ListenableWorker {
    public static final String REFRESH_DATA_ACTION = "REFRESH_DATA";

    public RefreshCacheWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull final CallbackToFutureAdapter.Completer<Result> completer) {
                VehicleInfo vehicleInfo = AppDatabase.getVehicleInfo(getApplicationContext());

                if (vehicleInfo != null) {
                    FirebaseDatabaseUtils.getInstance().getVehicle(vehicleInfo.getVehicleUid(), new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
                        @Override
                        public void onDataReady(Vehicle vehicle) {
                            FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
                                        @Override
                                        public void onDataReady(Set<MaintenanceScheduleEntry> data) {
                                            Timber.d("Data has been fetched and cache has been updated");

                                            // Fire an intent indicating the data should be recalculated.
                                            Intent intent = new Intent();
                                            intent.setAction(REFRESH_DATA_ACTION);
                                            RefreshCacheWorker.this.getApplicationContext().sendBroadcast(intent);

                                            completer.set(Result.success());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Timber.e("RefreshCache: Failed to query maintenance schedule");
                                            completer.set(Result.failure());
                                        }
                                    }
                                    // Tell the firebase utility that we do not want to use the cache.
                                    // We want to reach out to the data server to get the latest data.
                                    , false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Timber.e("RefreshCache: Failed to query vehicle");
                            completer.set(Result.failure());
                        }
                    });
                } else {
                    // There is no vehicle selected yet.  This is not a failure condition.
                    Timber.d("RefreshCache: no vehicle selected yet");
                    completer.set(Result.success());
                }

                return "refreshCache vehicleUid " + vehicleInfo.getVehicleUid();
            }
        });
    }
}
