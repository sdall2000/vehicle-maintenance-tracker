package android.example.vehiclemaintenancetracker;

import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntry;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.databinding.FragmentServiceNotificationsBinding;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.VehicleInfo;
import android.example.vehiclemaintenancetracker.utilities.ServiceNotificationGenerator;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceNotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceNotificationsFragment extends Fragment {

    private List<MileageEntry> mileageEntries;
    private List<MaintenanceEntry> maintenanceEntries;
    private VehicleInfo vehicleInfo;
    private Set<MaintenanceScheduleEntry> maintenanceScheduleEntries;

    FragmentServiceNotificationsBinding binding;
    private String maintenanceScheduleUid;

    public ServiceNotificationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param maintenanceScheduleUid the maintenance uid
     * @return A new instance of fragment ServiceNotificationsFragment.
     */
    public static ServiceNotificationsFragment newInstance(String maintenanceScheduleUid) {
        ServiceNotificationsFragment fragment = new ServiceNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(AppDatabase.MAINTENANCE_SCHEDULE_UID_KEY, maintenanceScheduleUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maintenanceScheduleUid = getArguments().getString(AppDatabase.MAINTENANCE_SCHEDULE_UID_KEY);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentServiceNotificationsBinding.inflate(inflater, container, false);

//        View view = inflater.inflate(R.layout.fragment_service_notifications, container, false);

//        View recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView();
        setupObservers();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.buttonEnterService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MaintenanceActivity.class);
                intent.putExtra(AppDatabase.MAINTENANCE_SCHEDULE_UID_KEY, maintenanceScheduleUid);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        vehicleInfo = AppDatabase.getVehicleInfo(getActivity());

        if (vehicleInfo != null) {
            FirebaseDatabaseUtils.getInstance().getVehicle(vehicleInfo.getVehicleUid(), new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
                @Override
                public void onDataReady(final Vehicle vehicle) {
                    FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
                        @Override
                        public void onDataReady(Set<MaintenanceScheduleEntry> maintenanceScheduleEntries) {
                            ServiceNotificationsFragment.this.maintenanceScheduleEntries = maintenanceScheduleEntries;
                            recalculateServiceNotifications();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    private void setupObservers() {
        // Observe changes to the mileage data.
        LiveData<List<MileageEntry>> mileage = AppDatabase.getInstance(getContext()).getMileageEntryDao().getAllLiveData();

        mileage.observe(getViewLifecycleOwner(), new Observer<List<MileageEntry>>() {
            @Override
            public void onChanged(List<MileageEntry> mileageEntries) {
                ServiceNotificationsFragment.this.mileageEntries = mileageEntries;

                recalculateServiceNotifications();
            }
        });

        // Observe changes to maintenance.
        LiveData<List<MaintenanceEntry>> maintenance = AppDatabase.getInstance(getContext()).getMaintenanceDao().getAllLiveData();

        maintenance.observe(getViewLifecycleOwner(), new Observer<List<MaintenanceEntry>>() {
            @Override
            public void onChanged(List<MaintenanceEntry> maintenanceEntries) {
                ServiceNotificationsFragment.this.maintenanceEntries = maintenanceEntries;

                recalculateServiceNotifications();
            }
        });
    }

    private void recalculateServiceNotifications() {
        Timber.d("Recalculate service notifications");

        // Mandatory fields for calculating service notifications.
        if (maintenanceEntries != null && maintenanceScheduleEntries != null && vehicleInfo != null) {
            MileageEntry mostRecentMileage = null;

            if (mileageEntries != null && mileageEntries.size() > 0) {
                mostRecentMileage = mileageEntries.get(0);
            }

            // If there are no mileage entries yet for the vehicle, use the vehicle starting mileage.
            int currentMileage = mostRecentMileage != null ? mostRecentMileage.getMileage() : vehicleInfo.getStartingMileage();
            long currentDate = System.currentTimeMillis();

            // Link mileage entries to maintenance entries.
            if (linkMileageEntriesToMaintenanceEntries()) {

                Set<MaintenanceEntry> maintenanceEntrySet = new HashSet<>(maintenanceEntries);

                // Get the vehicle UID, start date, and start mileage.
                List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                        currentMileage,
                        currentDate,
                        maintenanceScheduleEntries,
                        maintenanceEntrySet,
                        100,
                        10,
                        vehicleInfo.getStartingMileage(),
                        vehicleInfo.getStartingDateEpochMs()
                );

                binding.recyclerView.setAdapter(new NotificationsRecylerViewAdapter(ServiceNotificationsFragment.this, serviceNotifications));
            } else {
                Timber.d("Not all maintenance entries had linked mileage entries.");
            }
        }
    }

    private boolean linkMileageEntriesToMaintenanceEntries() {
        boolean allLinked = true;

        for (MaintenanceEntry maintenanceEntry : maintenanceEntries) {
            boolean found = false;
            for (MileageEntry mileageEntry : mileageEntries) {
                if (maintenanceEntry.getMileageUid() == mileageEntry.getUid()) {
                    maintenanceEntry.setMileageEntry(mileageEntry);
                    found = true;
                }
            }

            if (!found) {
                allLinked = false;
                break;
            }
        }

        return allLinked;
    }

    public static class NotificationsRecylerViewAdapter
            extends RecyclerView.Adapter<NotificationsRecylerViewAdapter.ViewHolder> {

        private final ServiceNotificationsFragment parent;
        private final List<ServiceNotification> notifications;

        public NotificationsRecylerViewAdapter(ServiceNotificationsFragment parent,
                                               List<ServiceNotification> notifications) {
            this.parent = parent;
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ServiceNotification notification = notifications.get(position);

            holder.serviceTextView.setText(notification.getService());

            if (notification.getMileageDue() != null) {
                holder.mileageTextView.setText(ValueFormatter.formatDistance(notification.getMileageDue()));
            } else {
                holder.mileageTextView.setText("");
            }

            if (notification.getDateDue() != null) {
                holder.dateTextView.setText(DateConverter.convertDateToString(parent.getContext(), notification.getDateDue()));
            } else {
                holder.dateTextView.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView serviceTextView;
            final TextView mileageTextView;
            final TextView dateTextView;

            ViewHolder(View view) {
                super(view);

                serviceTextView = view.findViewById(R.id.textViewService);
                mileageTextView = view.findViewById(R.id.textViewMileage);
                dateTextView = view.findViewById(R.id.textViewDate);
            }
        }
    }
}
