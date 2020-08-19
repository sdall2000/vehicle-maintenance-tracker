package android.example.vehiclemaintenancetracker.ui;

import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntryJoined;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.databinding.FragmentHistoryBinding;
import android.example.vehiclemaintenancetracker.databinding.HistoryListContentBinding;
import android.example.vehiclemaintenancetracker.model.History;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class HistoryFragment extends Fragment {

    FragmentHistoryBinding binding;
    private Vehicle vehicle;
    private Set<MaintenanceScheduleEntry> maintenanceScheduleEntries;
    private List<MaintenanceEntryJoined> maintenanceEntries;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        processSelectedVehicle();
        setupObservers();

        return binding.getRoot();
    }

    private void setupObservers() {
        AppDatabase appDatabase = AppDatabase.getInstance(getContext());

        LiveData<List<Vehicle>> vehicles = appDatabase.getVehicleDao().getVehiclesLive();
        vehicles.observe(getViewLifecycleOwner(), new Observer<List<Vehicle>>() {
            @Override
            public void onChanged(List<Vehicle> vehicles) {
                if (vehicles.size() > 0) {
                    vehicle = vehicles.get(0);
                } else {
                    vehicle = null;
                }

                processSelectedVehicle();
                renderServiceHistory();
            }
        });

        LiveData<List<MaintenanceEntryJoined>> maintenanceEntries = AppDatabase.getInstance(getContext()).getMaintenanceDao().getAllJoinedLiveData();
        maintenanceEntries.observe(getViewLifecycleOwner(), new Observer<List<MaintenanceEntryJoined>>() {
            @Override
            public void onChanged(List<MaintenanceEntryJoined> maintenanceEntries) {
                HistoryFragment.this.maintenanceEntries = maintenanceEntries;
                renderServiceHistory();
            }
        });
    }

    private void processSelectedVehicle() {
        // Make sure a vehicle is selected.
        if (vehicle != null) {
            binding.textViewVehicle.setText(vehicle.getName());
        } else {
            binding.textViewVehicle.setText(R.string.no_vehicle_selected);
        }

        populateRecyclerView();
    }

    private void populateRecyclerView() {
        if (vehicle != null) {
            FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
                @Override
                public void onDataReady(final Set<MaintenanceScheduleEntry> data) {
                    HistoryFragment.this.maintenanceScheduleEntries = data;

                    renderServiceHistory();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("Error retrieving vehicle details %s", databaseError.getMessage());
                }
            });
        }
    }

    private void renderServiceHistory() {
        if (maintenanceScheduleEntries != null && maintenanceEntries != null) {
            List<History> historyList = new ArrayList<>();

            double totalCost = 0.0;

            // Create the list of history.  This includes the maintenance text.
            for (MaintenanceEntryJoined entry : maintenanceEntries) {
                MaintenanceScheduleEntry scheduleEntry = getMaintenanceScheduleEntry(entry.getMaintenanceItemUid());

                if (scheduleEntry != null) {
                    double cost = entry.getCost() == null ? 0.0 : entry.getCost();
                    totalCost += cost;
                    historyList.add(new History(entry.getDate(), entry.getMileage(), scheduleEntry.getMaintenance(), cost, entry.getProvider()));
                } else {
                    Timber.e("Could not find maintenance schedule entry with UID %s", entry.getMaintenanceItemUid());
                }
            }

            binding.recyclerView.setAdapter(new HistoryRecyclerViewAdapter(historyList));
            binding.textViewTotal.setText(ValueFormatter.formatCost(totalCost));
        }
    }

    private MaintenanceScheduleEntry getMaintenanceScheduleEntry(String uid) {
        for (MaintenanceScheduleEntry entry : maintenanceScheduleEntries) {
            if (uid.equals(entry.getMaintenanceItemId())) {
                return entry;
            }
        }

        return null;
    }

    private class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {
        private final List<History> historyList;

        public HistoryRecyclerViewAdapter(List<History> historyList) {
            this.historyList = historyList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            HistoryListContentBinding binding = HistoryListContentBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindData(historyList.get(position));
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final HistoryListContentBinding binding;

            public ViewHolder(HistoryListContentBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bindData(History history) {
                binding.textViewDate.setText(DateConverter.convertDateToString(getContext(), history.getDate()));
                binding.textViewMileage.setText(ValueFormatter.formatDistance(history.getMileage()));
                binding.textViewService.setText(history.getService());
                binding.textViewCost.setText(ValueFormatter.formatCost(history.getCost()));
            }
        }
    }
}
