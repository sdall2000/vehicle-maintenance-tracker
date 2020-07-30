package android.example.vehiclemaintenancetracker;

import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.databinding.FragmentDashboardBinding;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.database.DatabaseError;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // 86,400,000 milliseconds per day.
    private static final double MS_PER_YEAR = 86_400_000.0 * 365.0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentDashboardBinding binding;

    // Cache the data so we don't have to refetch.
    private String vehicleUid;
    private Vehicle vehicle;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        processSelectedVehicle();

        LiveData<List<MileageEntry>> mileage = AppDatabase.getInstance(getContext()).getMileageEntryDao().getAllLiveData();

        mileage.observe(getViewLifecycleOwner(), new Observer<List<MileageEntry>>() {
            @Override
            public void onChanged(List<MileageEntry> mileageEntries) {
                if (mileageEntries.size() != 0) {
                    // Mileage entries are sorted by date descending.

                    // Get the most recent one.
                    MileageEntry mostRecent = mileageEntries.get(0);

                    binding.textViewMileage.setText(Integer.toString(mostRecent.getMileage()));
                    binding.textViewMileageReported.setText(DateConverter.convertDateToString(getContext(), mostRecent.getDate()));

                    // We need at least two mileage entries to calculate the average.
                    if (mileageEntries.size() >= 2) {

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
                            long averagePerYear = Math.round(milesTravelled / timeDeltaYears);

                            binding.textViewMileageAverage.setText(Long.toString(averagePerYear));
                        }
                    } else {
                        binding.textViewMileageAverage.setText("");
                    }
                } else {
                    binding.textViewMileage.setText("");
                    binding.textViewMileageReported.setText("");
                    binding.textViewMileageAverage.setText("");
                }
            }
        });

        binding.buttonReportMileage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MileageActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        processSelectedVehicle();
        Timber.d("DashboardFragment onResume");
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
                        vehicle = data;

                        String vehicleText = String.format("%d %s %s", data.getYear(), data.getMake(), data.getModel());
                        binding.textViewVehicle.setText(vehicleText);

                        loadServiceNotificationsFragment(vehicle.getMaintenanceScheduleUid());
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
    }

    private void loadServiceNotificationsFragment(String maintenanceScheduleUid) {

        // TODO handle case where the vehicle uid has changed.
        // Insert service notifications fragment
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ServiceNotificationsFragment serviceNotificationsFragment = ServiceNotificationsFragment.newInstance(maintenanceScheduleUid);
        ft.replace(R.id.service_notifications_placeholder, serviceNotificationsFragment);
        ft.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
