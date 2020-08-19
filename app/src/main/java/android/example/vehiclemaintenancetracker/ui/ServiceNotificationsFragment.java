package android.example.vehiclemaintenancetracker.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntryJoined;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.VehicleStartingMileage;
import android.example.vehiclemaintenancetracker.databinding.FragmentServiceNotificationsBinding;
import android.example.vehiclemaintenancetracker.databinding.NotificationListContentBinding;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.Status;
import android.example.vehiclemaintenancetracker.utilities.ServiceNotificationGenerator;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import org.jetbrains.annotations.NotNull;

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
    private List<MaintenanceEntryJoined> maintenanceEntries;
    private Set<MaintenanceScheduleEntry> maintenanceScheduleEntries;

    FragmentServiceNotificationsBinding binding;
    private String maintenanceScheduleUid;

    private VehicleStartingMileage vehicle;

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

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupObservers();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.buttonEnterService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add explode transition.
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();

                Intent intent = new Intent(getActivity(), MaintenanceActivity.class);
                intent.putExtra(AppDatabase.MAINTENANCE_SCHEDULE_UID_KEY, maintenanceScheduleUid);
                startActivity(intent, bundle);
            }
        });
    }

    private void setupObservers() {
        final AppDatabase appDatabase = AppDatabase.getInstance(getContext());
        // Observe changes to the vehicle.

        LiveData<List<VehicleStartingMileage>> vehicles = appDatabase.getVehicleDao().getVehicleStartingMileageLive();
        vehicles.observe(getViewLifecycleOwner(), new Observer<List<VehicleStartingMileage>>() {
            @Override
            public void onChanged(List<VehicleStartingMileage> vehicles) {
                // TODO Just track one vehicle for now.
                if (vehicles.size() > 0) {
                    vehicle = vehicles.get(0);

                    FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
                        @Override
                        public void onDataReady(Set<MaintenanceScheduleEntry> data) {
                            maintenanceScheduleEntries = data;

                            recalculateServiceNotifications();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Timber.e("Error getting maintenance schedule %s", databaseError);
                        }
                    });
                } else {
                    // It's possible all vehicles were deleted, in which case we need to refresh
                    // the notifications.
                    recalculateServiceNotifications();
                }

                Timber.d("Vehicle observer fired in ServiceNotificationsFragment");
            }
        });

        // Observe changes to the mileage data.  We need to listen to this separately from the joined
        // query below, because mileage entries may be made without corresponding maintenance entries.
        LiveData<List<MileageEntry>> mileage = appDatabase.getMileageEntryDao().getAllLiveData();

        mileage.observe(getViewLifecycleOwner(), new Observer<List<MileageEntry>>() {
            @Override
            public void onChanged(List<MileageEntry> mileageEntries) {
                ServiceNotificationsFragment.this.mileageEntries = mileageEntries;

                Timber.d("Mileage observer fired in ServiceNotificationsFragment");

                recalculateServiceNotifications();
            }
        });

        // Observe changes to joined maintenance/mileage.
        LiveData<List<MaintenanceEntryJoined>> maintenance = appDatabase.getMaintenanceDao().getAllJoinedLiveData();

        maintenance.observe(getViewLifecycleOwner(), new Observer<List<MaintenanceEntryJoined>>() {
            @Override
            public void onChanged(List<MaintenanceEntryJoined> maintenanceEntries) {
                ServiceNotificationsFragment.this.maintenanceEntries = maintenanceEntries;

                Timber.d("Maintenance observer fired in ServiceNotificationsFragment");

                recalculateServiceNotifications();
            }
        });
    }

    private void recalculateServiceNotifications() {
        Timber.d("Recalculate service notifications");

        // Mandatory fields for calculating service notifications.
        if (maintenanceEntries != null && maintenanceScheduleEntries != null && vehicle != null) {
            MileageEntry mostRecentMileage = null;

            if (mileageEntries != null && mileageEntries.size() > 0) {
                mostRecentMileage = mileageEntries.get(0);
            }

            // If there are no mileage entries yet for the vehicle, use the vehicle starting mileage.
            int currentMileage = mostRecentMileage != null ? mostRecentMileage.getMileage() : vehicle.getStartingMileage();
            long currentDate = System.currentTimeMillis();

            // Get the vehicle UID, start date, and start mileage.
            List<ServiceNotification> serviceNotifications = ServiceNotificationGenerator.generateServiceNotifications(
                    currentMileage,
                    currentDate,
                    maintenanceScheduleEntries,
                    maintenanceEntries,
                    AppDatabase.getMileageWarningThreshold(getContext()),
                    AppDatabase.getDayWarningThreshold(getContext()),
                    vehicle.getStartingMileage(),
                    vehicle.getStartingDate().getTime());

            Timber.d("There are %d service notifications", serviceNotifications.size());

            // If there are no service notifications, we want to show the text view indicating maintenance is up to date.
            binding.textViewMaintenanceUpToDate.setVisibility(serviceNotifications.size() == 0 ? View.VISIBLE : View.INVISIBLE);

            binding.recyclerView.setAdapter(new NotificationsRecylerViewAdapter(ServiceNotificationsFragment.this, serviceNotifications));
        } else {
            Timber.d("A field is null %s %s %s",
                    maintenanceEntries == null ? "maintenanceEntries" : "",
                    maintenanceScheduleEntries == null ? "maintenanceScheduleEntries" : "",
                    vehicle == null ? "vehicle" : "");
        }
    }

    private class NotificationsRecylerViewAdapter
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
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            NotificationListContentBinding binding = NotificationListContentBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindData(notifications.get(position));
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final NotificationListContentBinding binding;

            ViewHolder(NotificationListContentBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bindData(ServiceNotification serviceNotification) {
                binding.textViewService.setText(serviceNotification.getService());

                if (serviceNotification.getMileageDue() != null) {
                    binding.textViewMileage.setText(ValueFormatter.formatDistance(serviceNotification.getMileageDue()));
                } else {
                    binding.textViewMileage.setText("");
                }

                if (serviceNotification.getDateDue() != null) {
                    binding.textViewDate.setText(DateConverter.convertDateToString(parent.getContext(), serviceNotification.getDateDue()));
                } else {
                    binding.textViewDate.setText("");
                }

                if (serviceNotification.getOverallStatus() != Status.Good) {
                    Styler.styleImageViewStatus(getContext(), binding.imageView, serviceNotification.getOverallStatus());
                }
            }
        }
    }
}
