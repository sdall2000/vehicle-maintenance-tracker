package android.example.vehiclemaintenancetracker.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.FirebaseDatabaseUtils;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntry;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.ActivityMaintenanceBinding;
import android.example.vehiclemaintenancetracker.model.MaintenanceScheduleEntry;
import android.example.vehiclemaintenancetracker.ui.widget.VehicleMaintenanceTrackerAppWidget;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class MaintenanceActivity extends AppCompatActivity {
    private ActivityMaintenanceBinding binding;
    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        binding = ActivityMaintenanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonSubmitMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValid()) {
                    String dateString = binding.editTextDatePerformed.getText().toString();
                    String mileageString = binding.editTextMileage.getText().toString();

                    try {
                        // Read into a Date object so we can store in the specific format we want.
                        final Date date = dateFormat.parse(dateString);
                        final int mileage = Integer.parseInt(mileageString);

                        MaintenanceScheduleEntry entry = (MaintenanceScheduleEntry) binding.spinnerMaintenance.getSelectedItem();
                        final String maintenanceItemUid = entry.getMaintenanceItemId();

                        final String provider = binding.editTextTextProvider.getText().toString();
                        final Double cost = Double.parseDouble(binding.editTextCost.getText().toString());

                        // Must be done on a background thread.
                        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase appDatabase = AppDatabase.getInstance(MaintenanceActivity.this);

                                // Create a mileage entry
                                MileageEntry mileageEntry = new MileageEntry(mileage, date);
                                long mileageUid = appDatabase.getMileageEntryDao().insert(mileageEntry);

                                // Create a maintenance entry
                                MaintenanceEntry maintenanceEntry = new MaintenanceEntry(
                                        mileageUid,
                                        maintenanceItemUid,
                                        provider,
                                        cost);

                                appDatabase.getMaintenanceDao().insert(maintenanceEntry);

                                // Update app widgets.
                                updateAppWidgets();

                                // Complete the activity.
                                finish();
                            }
                        });

                    } catch (ParseException e) {
                        // Should never happen because we always validate the fields before using them.
                        Toast.makeText(MaintenanceActivity.this, R.string.validation_error_unhandled, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        String maintenanceUid = null;

        // See if a vehicle has already been selected.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString(AppDatabase.MAINTENANCE_SCHEDULE_UID_KEY);
            if (value != null) {
                maintenanceUid = value;
            }
        }

        populateSpinner(maintenanceUid);
    }

    private void populateSpinner(String maintenanceScheduleUid) {
        FirebaseDatabaseUtils.getInstance().getMaintenanceSchedule(maintenanceScheduleUid, new FirebaseDatabaseUtils.HelperListener<Set<MaintenanceScheduleEntry>>() {
            @Override
            public void onDataReady(Set<MaintenanceScheduleEntry> data) {
                List<MaintenanceScheduleEntry> list = new ArrayList<>(data);
                ArrayAdapter<MaintenanceScheduleEntry> adapter = new ArrayAdapter<>(
                        MaintenanceActivity.this,
                        android.R.layout.simple_spinner_item,
                        list);
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                binding.spinnerMaintenance.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e("Error populating maintenance spinner: %s", databaseError.getMessage());
            }
        });
    }

    private boolean inputValid() {
        boolean inputValid = true;

        String dateString = binding.editTextDatePerformed.getText().toString();
        String mileageString = binding.editTextMileage.getText().toString();

        binding.editTextDatePerformed.setError(null);
        binding.editTextMileage.setError(null);

        if (dateString.isEmpty()) {
            binding.editTextDatePerformed.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                binding.editTextDatePerformed.setError(getString(R.string.validation_invalid_date));
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

    private void updateAppWidgets() {
        Intent intent = new Intent(this, VehicleMaintenanceTrackerAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, VehicleMaintenanceTrackerAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}