package android.example.vehiclemaintenancetracker.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.data.Vehicle;
import android.example.vehiclemaintenancetracker.data.VehicleStartingMileage;
import android.example.vehiclemaintenancetracker.databinding.ActivityVehicleChooserBinding;
import android.example.vehiclemaintenancetracker.ui.widget.VehicleMaintenanceTrackerAppWidget;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.example.vehiclemaintenancetracker.utilities.DatePickerHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class VehicleChooserActivity extends AppCompatActivity {

    private DateFormat dateFormat;

    private ActivityVehicleChooserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        binding.buttonSelectVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValid()) {
                    saveVehicle();
                }
            }
        });

        // Load vehicle data in the background thread since it accesses the db.
        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadVehicle();
            }
        });
    }

    private void loadVehicle() {
        // Defaults if vehicle has not been defined.
        Date startingDate = new Date();
        int startingMileage = 0;

        List<VehicleStartingMileage> vehicles = AppDatabase.getInstance(this).getVehicleDao().getVehicleStartingMileage();

        VehicleStartingMileage vehicle;

        if (vehicles.size() >= 1) {
            vehicle = vehicles.get(0);

            if (vehicle != null) {
                // If the vehicle uid has been set, then the starting mileage/date will be as well.
                startingMileage = vehicle.getStartingMileage();

                // If the starting date is already set, use that date instead.
                startingDate = vehicle.getStartingDate();

                binding.editTextName.setText(vehicle.getName());
                binding.editTextDescription.setText(vehicle.getDescription());
            }
        }
        binding.editTextMileage.setText(String.format("%d", startingMileage));

        // Use the DatePickerHelper to configure date picker functionality and set initial value.
        DatePickerHelper.configureDateChooser(
                this,
                binding.textFieldDate,
                binding.imageButton,
                dateFormat,
                startingDate);
    }

    private void saveVehicle() {
        String dateString = binding.textFieldDate.getText().toString();
        String mileageString = binding.editTextMileage.getText().toString();
        final AppDatabase appDatabase = AppDatabase.getInstance(this);

        try {
            final Date date = dateFormat.parse(dateString);
            final int mileage = Integer.parseInt(mileageString);

            // Launch worker thread for db operations.
            AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    // Clear out any existing data.
                    appDatabase.deleteData();

                    // Create the first mileage entry based on the starting mileage/date entered for the vehicle.
                    MileageEntry mileageEntry = new MileageEntry(mileage, date);
                    long mileageUid = appDatabase.getMileageEntryDao().insert(mileageEntry);

                    String name = binding.editTextName.getText().toString();
                    String description = binding.editTextDescription.getText().toString();

                    // TODO hardcoded until we let the user define/pick a maintenance schedule
                    int maintenanceScheduleUid = 1;

                    Vehicle vehicle = new Vehicle(name, description, maintenanceScheduleUid, (int) mileageUid);
                    appDatabase.getVehicleDao().insert(vehicle);

                    // Update the app widgets.
                    updateAppWidgets();

                    // Complete this activity.
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

        } catch (ParseException e) {
            // Should never happen because we always validate the fields before using them.
            Toast.makeText(VehicleChooserActivity.this, R.string.validation_error_unhandled, Toast.LENGTH_LONG).show();
        }
    }

    private void updateAppWidgets() {
        Intent intent = new Intent(this, VehicleMaintenanceTrackerAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, VehicleMaintenanceTrackerAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private boolean inputValid() {
        boolean inputValid = true;

        String name = binding.editTextName.getText().toString();
        String dateString = binding.textFieldDate.getText().toString();
        String mileageString = binding.editTextMileage.getText().toString();

        binding.editTextName.setError(null);
        binding.textFieldDate.setError(null);
        binding.editTextMileage.setError(null);

        if (name.isEmpty()) {
            binding.editTextName.setError(getString(R.string.validation_required_field));
            inputValid = false;
        }

        if (dateString.isEmpty()) {
            binding.textFieldDate.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                binding.textFieldDate.setError(getString(R.string.validation_invalid_date));
                inputValid = false;
            }
        }

        if (mileageString.isEmpty()) {
            binding.editTextMileage.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                Integer.parseInt(mileageString);
            } catch (NumberFormatException e) {
                binding.editTextMileage.setError(getString(R.string.validation_invalid_mileage));
                inputValid = false;
            }
        }

        return inputValid;
    }
}