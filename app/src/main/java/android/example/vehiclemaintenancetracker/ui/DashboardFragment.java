package android.example.vehiclemaintenancetracker.ui;

import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.databinding.FragmentDashboardBinding;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DashboardFragment extends Fragment {

    // 86,400,000 milliseconds per day.
    private static final double MS_PER_YEAR = 86_400_000.0 * 365.0;

    private Integer maintenanceScheduleUid;

    FragmentDashboardBinding binding;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupObservers();

        binding.buttonReportMileage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO reintroduce transitions.  Maybe can do from the nav graph editor.
                NavDirections action = DashboardFragmentDirections.actionNavDashboardToNavMileage();
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.buttonEnterService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maintenanceScheduleUid != null) {
                    NavDirections action = DashboardFragmentDirections.actionNavDashboardToNavMaintenance(maintenanceScheduleUid);
                    Navigation.findNavController(view).navigate(action);
                } else {
                    Toast.makeText(getContext(), R.string.no_vehicle_defined, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = DashboardFragmentDirections.actionNavDashboardToNavHistory();
                Navigation.findNavController(view).navigate(action);
            }
        });
    }

    private void setupObservers() {
        LiveData<List<MileageEntry>> mileage = AppDatabase.getInstance(getContext()).getMileageEntryDao().getAllLiveData();

        mileage.observe(getViewLifecycleOwner(), new Observer<List<MileageEntry>>() {
            @Override
            public void onChanged(List<MileageEntry> mileageEntries) {
                if (mileageEntries.size() != 0) {
                    // Mileage entries are sorted by date descending.

                    // Get the most recent one.
                    MileageEntry mostRecent = mileageEntries.get(0);

                    binding.textViewMileage.setText(ValueFormatter.formatDistance(mostRecent.getMileage()));
                    binding.textViewMileageReported.setText(DateConverter.convertDateToString(getContext(), mostRecent.getDate()));

                    // We need at least two mileage entries to calculate the average.
                    if (mileageEntries.size() >= 2) {
                        binding.textViewAverageLabel.setVisibility(View.VISIBLE);
                        binding.textViewMileageAverageLabel.setVisibility(View.VISIBLE);

                        // Get the last one in the list, which would be the oldest entry.
                        MileageEntry oldest = mileageEntries.get(mileageEntries.size() - 1);

                        long timeDeltaMs = mostRecent.getDate().getTime() - oldest.getDate().getTime();

                        // Make sure there is some delta.
                        if (timeDeltaMs > 0) {
                            // Calculate average miles per year.
                            int milesTravelled = mostRecent.getMileage() - oldest.getMileage();

                            // We are calculating average miles per year.  Convert the time delta to years.
                            double timeDeltaYears = timeDeltaMs / MS_PER_YEAR;

                            // If we drove 7500 miles in .75 years (9 months), that would mean
                            // we would be averaging 10,000 miles in a year.
                            // We just divide miles travelled by the years delta.
                            int averagePerYear = (int) Math.round(milesTravelled / timeDeltaYears);

                            binding.textViewMileageAverage.setText(ValueFormatter.formatDistance(averagePerYear));
                        }
                    } else {
                        // Not at least two mileage entries - can't calculate average miles.
                        binding.textViewMileageAverage.setText("");
                        binding.textViewAverageLabel.setVisibility(View.INVISIBLE);
                        binding.textViewMileageAverageLabel.setVisibility(View.INVISIBLE);
                    }
                } else {
                    binding.textViewMileage.setText("");
                    binding.textViewMileageReported.setText("");
                    binding.textViewMileageAverage.setText("");

                    // Not at least two mileage entries - can't calculate average miles.
                    binding.textViewAverageLabel.setVisibility(View.INVISIBLE);
                    binding.textViewMileageAverageLabel.setVisibility(View.INVISIBLE);
                }
            }
        });

        LiveData<List<Vehicle>> vehicles = AppDatabase.getInstance(getContext()).getVehicleDao().getVehiclesLive();
        vehicles.observe(getViewLifecycleOwner(), new Observer<List<Vehicle>>() {
            @Override
            public void onChanged(List<Vehicle> vehicles) {
                if (vehicles.size() > 0) {
                    // TODO Multiple vehicle support
                    Vehicle vehicle = vehicles.get(0);

                    binding.textViewMileageLabel.setVisibility(View.VISIBLE);
                    binding.textViewReportedLabel.setVisibility(View.VISIBLE);
                    binding.buttonReportMileage.setVisibility(View.VISIBLE);

                    binding.textViewVehicle.setText(vehicle.getDescription());

                    maintenanceScheduleUid = vehicle.getMaintenanceScheduleUid();

                    loadServiceNotificationsFragment();
                } else {
                    binding.textViewVehicle.setText(getString(R.string.no_vehicle_selected));
                    binding.textViewMileageLabel.setVisibility(View.INVISIBLE);
                    binding.textViewReportedLabel.setVisibility(View.INVISIBLE);
                    binding.textViewAverageLabel.setVisibility(View.INVISIBLE);
                    binding.textViewMileageAverageLabel.setVisibility(View.INVISIBLE);
                    binding.buttonReportMileage.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void loadServiceNotificationsFragment() {

        // TODO handle case where the vehicle uid has changed.
        // Insert service notifications fragment
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ServiceNotificationsFragment serviceNotificationsFragment = ServiceNotificationsFragment.newInstance();
        ft.replace(R.id.service_notifications_placeholder, serviceNotificationsFragment);
        ft.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
