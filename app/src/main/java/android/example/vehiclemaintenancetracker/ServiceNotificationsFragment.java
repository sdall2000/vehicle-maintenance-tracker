package android.example.vehiclemaintenancetracker;

import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.model.ServiceNotification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ServiceNotificationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ServiceNotificationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ServiceNotificationsFragment newInstance(String param1, String param2) {
        ServiceNotificationsFragment fragment = new ServiceNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_service_notifications, container, false);
        View recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView((RecyclerView) recyclerView);

        return view;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        List<ServiceNotification> notifications = new ArrayList<>();
        notifications.add(new ServiceNotification("Change Oil", 35_000, new Date()));
        notifications.add(new ServiceNotification("Check Tires", null, new Date()));
        notifications.add(new ServiceNotification("Check Engine", 60_000, null));
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
