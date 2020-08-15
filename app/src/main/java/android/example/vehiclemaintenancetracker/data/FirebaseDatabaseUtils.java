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
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class FirebaseDatabaseUtils {
    private static FirebaseDatabaseUtils instance;
    private static FirebaseDatabase database;

    // Cache for vehicle(s) that the user selects and their associated maintenance schedule.
    private Map<String, Vehicle> vehicleMap = new HashMap<>();
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

    public void getVehicle(final String vehicleUid, final HelperListener<Vehicle> listener) {

        if (vehicleMap.containsKey(vehicleUid)) {
            Timber.d("Vehicle cache hit for %s", vehicleUid);
            listener.onDataReady(vehicleMap.get(vehicleUid));
        } else {
            DatabaseReference myRef = database.getReference("vehicles/" + vehicleUid);

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Vehicle vehicle = dataSnapshot.getValue(Vehicle.class);

                    // Put vehicle in cache.
                    vehicleMap.put(vehicleUid, vehicle);

                    listener.onDataReady(vehicle);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onCancelled(databaseError);
                }
            });
        }
    }

    public void getYears(final HelperListener<Set<Integer>> listener) {

        // Leverage the existing getVehicles API.
        getVehicles(new HelperListener<Map<String, Vehicle>>() {
            @Override
            public void onDataReady(Map<String, Vehicle> data) {
                Set<Integer> years = new TreeSet<>();

                for (Vehicle vehicle : data.values()) {
                    years.add(vehicle.getYear());
                }

                listener.onDataReady(years);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCancelled(databaseError);
            }
        });
    }

    public void getMakesForYear(final int year, final HelperListener<Set<String>> listener) {
        // Leverage the existing getVehicles API.
        getVehicles(new HelperListener<Map<String, Vehicle>>() {
            @Override
            public void onDataReady(Map<String, Vehicle> data) {
                Set<String> makes = new TreeSet<>();

                for (Vehicle vehicle : data.values()) {
                    if (vehicle.getYear() == year) {
                        makes.add(vehicle.getMake());
                    }
                }

                listener.onDataReady(makes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCancelled(databaseError);
            }
        });
    }

    public void getModelsForMakeYear(final int year, final String make, final HelperListener<Set<String>> listener) {
        // Leverage the existing getVehicles API.
        getVehicles(new HelperListener<Map<String, Vehicle>>() {
            @Override
            public void onDataReady(Map<String, Vehicle> data) {
                Set<String> models = new TreeSet<>();

                for (Vehicle vehicle : data.values()) {
                    if (vehicle.getYear() == year && vehicle.getMake().equals(make)) {
                        models.add(vehicle.getModel());
                    }
                }

                listener.onDataReady(models);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCancelled(databaseError);
            }
        });
    }

    public void getVehicleUid(final int year, final String make, final String model, final HelperListener<String> listener) {
        getVehicles(new HelperListener<Map<String, Vehicle>>() {
            @Override
            public void onDataReady(Map<String, Vehicle> data) {
                for (Map.Entry<String, Vehicle> v : data.entrySet()) {
                    Vehicle vehicle = v.getValue();
                    if (year == vehicle.getYear() && make.equals(vehicle.getMake()) && model.equals(vehicle.getModel())) {
                        listener.onDataReady(v.getKey());

                        // We can exit this method since we found the matching vehicle and have notified the listener.
                        return;
                    }
                }

                // No data found.  Return null.
                listener.onDataReady(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

    /**
     * Get the Vehicle and MaintenanceScheduleEntry list for a vehicle
     *
     * @param vehicleUid The vehicle uid to retrieve the data for
     * @param listener The receiver of the data
     */
    public void getVehicleDetails(final String vehicleUid, final HelperListener<VehicleDetails> listener) {
        boolean dataResolved = false;

        Vehicle vehicle = vehicleMap.get(vehicleUid);

        if (vehicle != null) {
            if (maintenanceScheduleMap.containsKey(vehicle.getMaintenanceScheduleUid())) {
                VehicleDetails vehicleDetails = new VehicleDetails(vehicleUid, vehicle, maintenanceScheduleMap.get(vehicle.getMaintenanceScheduleUid()));
                dataResolved = true;
                listener.onDataReady(vehicleDetails);
            }
        }

        if (!dataResolved) {
            getVehicle(vehicleUid, new HelperListener<Vehicle>() {
                @Override
                public void onDataReady(final Vehicle vehicle) {
                    getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new HelperListener<Set<MaintenanceScheduleEntry>>() {
                        @Override
                        public void onDataReady(Set<MaintenanceScheduleEntry> maintenanceSchedule) {
                            // Create vehicle details.
                            VehicleDetails vehicleDetails = new VehicleDetails(vehicleUid, vehicle, maintenanceSchedule);

                            // Notify listener
                            listener.onDataReady(vehicleDetails);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            listener.onCancelled(databaseError);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
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
