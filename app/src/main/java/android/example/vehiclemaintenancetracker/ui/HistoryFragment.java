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
import android.example.vehiclemaintenancetracker.model.VehicleInfo;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.text.TextUtils;
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
    private Set<MaintenanceScheduleEntry> maintenanceScheduleEntries;
    private List<MaintenanceEntryJoined> maintenanceEntries;
    private String vehicleUid;

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
        LiveData<List<MaintenanceEntryJoined>> maintenanceEntries = AppDatabase.getInstance(getContext()).getMaintenanceDao().getAllJoinedLiveData();

        maintenanceEntries.observe(getViewLifecycleOwner(), new Observer<List<MaintenanceEntryJoined>>() {
            @Override
            public void onChanged(List<MaintenanceEntryJoined> maintenanceEntries) {
                HistoryFragment.this.maintenanceEntries = maintenanceEntries;
                renderServiceHistory();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        processSelectedVehicle();
    }

    private void processSelectedVehicle() {
        final String selectedVehicleUid = AppDatabase.getVehicleUid(getActivity());

        // Make sure a vehicle is selected.
        if (!TextUtils.isEmpty(selectedVehicleUid)) {

            // See if the vehicle uid has not already been set, or, if it doesn't equal the selected vehicle uid.
            // In those cases, we need to query for the vehicle.
            if (TextUtils.isEmpty(vehicleUid) || !vehicleUid.equals(selectedVehicleUid)) {

                FirebaseDatabaseUtils.getInstance().getVehicle(selectedVehicleUid, new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
                    @Override
                    public void onDataReady(Vehicle data) {
                        vehicleUid = selectedVehicleUid;

                        String vehicleText = String.format("%d %s %s", data.getYear(), data.getMake(), data.getModel());
                        binding.textViewVehicle.setText(vehicleText);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        binding.textViewVehicle.setText(getString(R.string.no_vehicle_selected));
                    }
                });
            }
        } else {
            binding.textViewVehicle.setText(getString(R.string.no_vehicle_selected));
        }

        populateRecyclerView();
    }

    private void populateRecyclerView() {
        VehicleInfo vehicleInfo = AppDatabase.getVehicleInfo(getActivity());

        if (vehicleInfo != null) {
            FirebaseDatabaseUtils.getInstance().getVehicle(vehicleInfo.getVehicleUid(), new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
                @Override
                public void onDataReady(final Vehicle vehicle) {
                    FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(vehicle.getMaintenanceScheduleUid(), new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
                        @Override
                        public void onDataReady(Set<MaintenanceScheduleEntry> maintenanceScheduleEntries) {
                            HistoryFragment.this.maintenanceScheduleEntries = maintenanceScheduleEntries;

                            renderServiceHistory();
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
