package android.example.vehiclemaintenancetracker;

import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.databinding.FragmentServiceNotificationsBinding;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.example.vehiclemaintenancetracker.model.Status;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceNotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceNotificationsFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentServiceNotificationsBinding.inflate(inflater, container, false);

//        View view = inflater.inflate(R.layout.fragment_service_notifications, container, false);

//        View recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView(binding.recyclerView);

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

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        List<ServiceNotification> notifications = new ArrayList<>();
        notifications.add(new ServiceNotification("Change Oil", 35_000, new Date(), Status.Overdue));
        notifications.add(new ServiceNotification("Check Tires", null, new Date(), Status.Upcoming));
        notifications.add(new ServiceNotification("Check Engine", 60_000, null, Status.Upcoming));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new NotificationsRecylerViewAdapter(this, notifications));
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
                holder.mileageTextView.setText(Integer.toString(notification.getMileageDue()));
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

        class ViewHolder extends RecyclerView.ViewHolder {
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
