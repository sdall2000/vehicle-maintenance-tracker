package android.example.vehiclemaintenancetracker.data;

import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class FirebaseDatabaseUtils {
    private static FirebaseDatabaseUtils instance;
    private static FirebaseDatabase database;

    // Cache for the maintenance schedule.
    private Map<String, Set<MaintenanceScheduleEntry>> maintenanceScheduleMap = new HashMap<>();

    private FirebaseDatabaseUtils() {
    }

    public synchronized static FirebaseDatabaseUtils getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseUtils();
            database = FirebaseDatabase.getInstance();
        }

        return instance;
    }

    public void getVehicles(final HelperListener<Map<String, Vehicle>> listener) {
        DatabaseReference myRef = database.getReference("vehicles");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Vehicle> vehicles = new HashMap<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Vehicle vehicle = ds.getValue(Vehicle.class);
                    vehicles.put(ds.getKey(), vehicle);
                }

                listener.onDataReady(vehicles);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onCancelled(databaseError);
            }
        });
    }

    public void getMaintenanceSchedule(final String maintenanceScheduleUid, final HelperListener<Set<MaintenanceScheduleEntry>> listener) {
        // Allow the cache to be used.
        getMaintenanceSchedule(maintenanceScheduleUid, listener, true);
    }

    public void getMaintenanceSchedule(final String maintenanceScheduleUid, final HelperListener<Set<MaintenanceScheduleEntry>> listener, boolean useCache) {
        if (useCache && maintenanceScheduleMap.containsKey(maintenanceScheduleUid)) {
            Timber.d("Maintenance schedule cache hit for %s", maintenanceScheduleUid);
            listener.onDataReady(maintenanceScheduleMap.get(maintenanceScheduleUid));
        } else {
            final DatabaseReference myRef = database.getReference();

            myRef.child("maintenanceSchedule/" + maintenanceScheduleUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final Set<MaintenanceScheduleEntry> entries = new LinkedHashSet<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        MaintenanceScheduleEntry entry = ds.getValue(MaintenanceScheduleEntry.class);
                        entries.add(entry);
                    }

                    final AtomicInteger atomicInteger = new AtomicInteger(0);

                    // Now, we want to populate the text for the maintenance.
                    for (final MaintenanceScheduleEntry entry : entries) {
                        myRef.child("maintenanceItem/" + entry.getMaintenanceItemId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String maintenance = (String) dataSnapshot.getChildren().iterator().next().getValue();
                                entry.setMaintenance(maintenance);

                                int valuesSet = atomicInteger.incrementAndGet();

                                // See if all entries have been set.  If so we can notify the listener.
                                if (valuesSet == entries.size()) {
                                    // Put the maintenance schedule in the cache.
                                    maintenanceScheduleMap.put(maintenanceScheduleUid, entries);

                                    listener.onDataReady(entries);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Timber.e("Failed to get maintenance text");
                                listener.onCancelled(databaseError);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onCancelled(databaseError);
                }
            });
        }
    }

    public interface HelperListener<T> {
        void onDataReady(T data);

        void onCancelled(DatabaseError databaseError);
    }
}
