package android.example.vehiclemaintenancetracker.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntry;
import android.example.vehiclemaintenancetracker.data.MaintenanceScheduleDetailJoined;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.ActivityMaintenanceBinding;
import android.example.vehiclemaintenancetracker.ui.widget.VehicleMaintenanceTrackerAppWidget;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.example.vehiclemaintenancetracker.utilities.DatePickerHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MaintenanceActivity extends AppCompatActivity {
    private ActivityMaintenanceBinding binding;
    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        binding = ActivityMaintenanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Use the DatePickerHelper to configure date picker functionality and set initial value.
        DatePickerHelper.configureDateChooser(
                this,
                binding.textViewDatePerformed,
                binding.imageButton,
                dateFormat,
                new Date());

        binding.buttonSubmitMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValid()) {
                    String dateString = binding.textViewDatePerformed.getText().toString();
                    String mileageString = binding.editTextMileage.getText().toString();

                    try {
                        // Read into a Date object so we can store in the specific format we want.
                        final Date date = dateFormat.parse(dateString);
                        final int mileage = Integer.parseInt(mileageString);

                        MaintenanceScheduleDetailJoined entry = (MaintenanceScheduleDetailJoined) binding.spinnerMaintenance.getSelectedItem();
                        final int maintenanceItemUid = entry.getMaintenanceUid();

                        final String provider = binding.editTextTextProvider.getText().toString();

                        // Cost is optional.
                        final AtomicReference<Double> cost = new AtomicReference<>(null);

                        if (!TextUtils.isEmpty(binding.editTextCost.getText())) {
                            cost.set(Double.parseDouble(binding.editTextCost.getText().toString()));
                        }

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
                                        cost.get());

                                appDatabase.getMaintenanceEntryDao().insert(maintenanceEntry);

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

        // See if the maintenance schedule is defined.  Eventually we will be passed a vehicle instead.
        // TODO pass the vehicle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt(AppDatabase.MAINTENANCE_SCHEDULE_UID_KEY);
            if (value != 0) {
                populateSpinner(value);
            }
        }
    }

    private void populateSpinner(final int maintenanceScheduleUid) {
        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final List<MaintenanceScheduleDetailJoined> maintenanceScheduleDetailList =
                        AppDatabase
                                .getInstance(MaintenanceActivity.this)
                                .getMaintenanceScheduleDetailDao().
                                getMaintenanceScheduleDetailJoined(maintenanceScheduleUid);

                AppExecutor.getInstance().getUiExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<MaintenanceScheduleDetailJoined> adapter = new ArrayAdapter<>(
                                MaintenanceActivity.this,
                                android.R.layout.simple_spinner_item,
                                maintenanceScheduleDetailList);
                        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                        binding.spinnerMaintenance.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private boolean inputValid() {
        boolean inputValid = true;

        String dateString = binding.textViewDatePerformed.getText().toString();
        String mileageString = binding.editTextMileage.getText().toString();

        binding.textViewDatePerformed.setError(null);
        binding.editTextMileage.setError(null);

        if (dateString.isEmpty()) {
            binding.textViewDatePerformed.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                binding.textViewDatePerformed.setError(getString(R.string.validation_invalid_date));
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