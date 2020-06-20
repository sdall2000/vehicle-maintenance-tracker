package android.example.vehiclemaintenancetracker;

import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.databinding.ActivityVehicleChooserBinding;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class VehicleChooserActivity extends AppCompatActivity {

    private ActivityVehicleChooserBinding binding;

    // These are used as the selected item in the spinner, if they are set.
    private Integer vehicleYear;
    private String vehicleMake;
    private String vehicleModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("Year item clicked: " + position + ", " + id);
                vehicleYear = (int) parent.getAdapter().getItem(position);
                populateMakeSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Timber.d("No year selected");
            }
        });

        binding.spinnerMake.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("Year item clicked: " + position + ", " + id);
                vehicleMake = (String) binding.spinnerMake.getAdapter().getItem(position);
                populateModelSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Timber.d("No make selected");
            }
        });

        binding.spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehicleModel = (String) binding.spinnerModel.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Timber.d("No model selected");
            }
        });

        binding.buttonSelectVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("onClick");
                FirebaseDatabaseUtils.getInstance().getVehicleUid(vehicleYear, vehicleMake, vehicleModel, new FirebaseDatabaseUtils.HelperListener<String>() {
                    @Override
                    public void onDataReady(String data) {
                        Timber.d("Got vehicle id %s", data);
                        Intent intent = new Intent();
                        intent.putExtra(AppDatabase.SELECTED_VEHICLE_UID_KEY, data);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.e("Failed to get vehicle uid: %s", databaseError.getMessage());
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
            }
        });

        String selectedVehicleUid = null;

        // See if a vehicle has already been selected.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString(AppDatabase.SELECTED_VEHICLE_UID_KEY);
            if (value != null) {
                selectedVehicleUid = value;
            }
        }

        if (selectedVehicleUid != null) {
            populateSpinnersWithVehicle(selectedVehicleUid);
        } else {
            populateYearSpinner();
        }
    }

    private void populateSpinnersWithVehicle(String selectedVehicleUid) {
        FirebaseDatabaseUtils.getInstance().getVehicle(selectedVehicleUid, new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
            @Override
            public void onDataReady(Vehicle data) {
                vehicleYear = data.getYear();
                vehicleMake = data.getMake();
                vehicleModel = data.getModel();

                // Kick off populating spinners by starting with year.
                populateYearSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO log an error.
                populateYearSpinner();
            }
        });
    }

    private void populateYearSpinner() {
        FirebaseDatabaseUtils.getInstance().getYears(new FirebaseDatabaseUtils.HelperListener<Set<Integer>>() {
            @Override
            public void onDataReady(Set<Integer> data) {
                List<Integer> list = new ArrayList<>(data);
                ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                        VehicleChooserActivity.this,
                        android.R.layout.simple_spinner_item,
                        list);

                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                binding.spinnerYear.setAdapter(adapter);

                if (vehicleYear != null) {
                    // The year is set.  Use that position.
                    binding.spinnerYear.setSelection(adapter.getPosition(vehicleYear));
                } else {
                    // The year is not set.  Use the last item in the set.
                    binding.spinnerYear.setSelection(data.size() - 1);
                    vehicleYear = list.get(list.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateMakeSpinner() {
        FirebaseDatabaseUtils.getInstance().getMakesForYear(vehicleYear, new FirebaseDatabaseUtils.HelperListener<Set<String>>() {
            @Override
            public void onDataReady(Set<String> data) {
                List<String> list = new ArrayList<>(data);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        VehicleChooserActivity.this,
                        android.R.layout.simple_spinner_item,
                        list);

                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                binding.spinnerMake.setAdapter(adapter);

                // If there is a vehicle make defined, then set that, otherwise the first make will be selected.
                if (vehicleMake != null) {
                    binding.spinnerMake.setSelection(adapter.getPosition(vehicleMake));
                } else {
                    vehicleMake = list.get(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateModelSpinner() {
        FirebaseDatabaseUtils.getInstance().getModelsForMakeYear(vehicleYear, vehicleMake, new FirebaseDatabaseUtils.HelperListener<Set<String>>() {
            @Override
            public void onDataReady(Set<String> data) {
                List<String> list = new ArrayList<>(data);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        VehicleChooserActivity.this,
                        android.R.layout.simple_spinner_item,
                        list);

                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                binding.spinnerModel.setAdapter(adapter);

                // If there is a vehicle model defined, then set that, otherwise the first model will be selected.
                if (vehicleModel != null) {
                    binding.spinnerModel.setSelection(adapter.getPosition(vehicleModel));
                } else {
                    vehicleModel = list.get(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}