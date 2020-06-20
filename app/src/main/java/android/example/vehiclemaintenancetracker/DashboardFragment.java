package android.example.vehiclemaintenancetracker;

import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.FragmentDashboardBinding;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final long MS_PER_YEAR = 1_000 * 86_400 * 365;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentDashboardBinding binding;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO pull from database.
        binding.textViewVehicle.setText("2012 Honda Civic");

        LiveData<List<MileageEntry>> mileage = AppDatabase.getInstance(getContext()).getMileageEntryDao().getAllLiveData();

        mileage.observe(getViewLifecycleOwner(), new Observer<List<MileageEntry>>() {
            @Override
            public void onChanged(List<MileageEntry> mileageEntries) {
                if (mileageEntries.size() != 0) {
                    MileageEntry mostRecent = mileageEntries.get(0);

                    binding.textViewMileage.setText(Integer.toString(mostRecent.getMileage()));
                    binding.textViewMileageReported.setText(DateConverter.convertDateToString(getContext(), mostRecent.getDate()));

                    // We need at least two mileage entries to calculate the average.
                    if (mileageEntries.size() >= 2) {
                        MileageEntry oldest = mileageEntries.get(mileageEntries.size() - 1);

                        long deltaMs = mostRecent.getDate().getTime() - oldest.getDate().getTime();

                        if (deltaMs > 0) {
                            // Calculate average miles per year.
                            int milesTravelled = mostRecent.getMileage() - oldest.getMileage();

                            long averagePerYear = Math.round((double) deltaMs / MS_PER_YEAR * milesTravelled);

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

        // Insert service notifications fragment
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.service_notifications_placeholder, new ServiceNotificationsFragment());
        ft.commit();

        queryFirebase();
    }

    private void queryFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("vehicleModels");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, ds.getKey() + ", " + ds.getChildren().iterator().next().getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value");
            }
        });

        DatabaseReference myRefYears = database.getReference("vehicles");

        myRefYears.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, ds.getKey() + ", " + ds.getChildren().iterator().next().getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
