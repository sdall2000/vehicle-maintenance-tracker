package android.example.vehiclemaintenancetracker.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.ActivityMileageBinding;
import android.example.vehiclemaintenancetracker.databinding.ContentMileageBinding;
import android.example.vehiclemaintenancetracker.ui.widget.VehicleMaintenanceTrackerAppWidget;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.example.vehiclemaintenancetracker.utilities.DatePickerHelper;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class MileageActivity extends AppCompatActivity {
    private ContentMileageBinding contentMileageBinding;
    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.example.vehiclemaintenancetracker.databinding.ActivityMileageBinding activityMileageBinding = ActivityMileageBinding.inflate(getLayoutInflater());
        setContentView(activityMileageBinding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        // Put the current date in the date field.
        contentMileageBinding = activityMileageBinding.contentMileage;

        // Use the DatePickerHelper to configure date picker functionality and set initial value.
        DatePickerHelper.configureDateChooser(
                this,
                contentMileageBinding.textFieldDate,
                contentMileageBinding.imageButton,
                dateFormat,
                new Date());

        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final MileageEntry mileageEntry = AppDatabase.getInstance(MileageActivity.this).getMileageEntryDao().getMostRecentMileage();

                AppExecutor.getInstance().getUiExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        populateLastEntry(mileageEntry);
                    }
                });
            }
        });

        contentMileageBinding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValid()) {
                    String dateString = contentMileageBinding.textFieldDate.getText().toString();
                    String mileageString = contentMileageBinding.editTextMileage.getText().toString();

                    try {
                        // Read into a Date object so we can store in the specific format we want.
                        final Date date = dateFormat.parse(dateString);
                        final int mileage = Integer.parseInt(mileageString);

                        // Must be done on a background thread.
                        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                // Insert a new mileage entry
                                MileageEntry mileageEntry = new MileageEntry(mileage, date);
                                AppDatabase.getInstance(MileageActivity.this).getMileageEntryDao().insert(mileageEntry);

                                // Update the app widgets.
                                updateAppWidgets();

                                // Complete the activity
                                finish();
                            }
                        });

                    } catch (ParseException e) {
                        // Should never happen because we always validate the fields before using them.
                        Toast.makeText(MileageActivity.this, R.string.validation_error_unhandled, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void populateLastEntry(MileageEntry mileageEntry) {
        if (mileageEntry != null) {
            contentMileageBinding.textViewLastEntry.setText(
                    getString(
                            R.string.last_mileage_entered_label,
                            ValueFormatter.formatDistance(mileageEntry.getMileage()),
                            DateConverter.convertDateToString(getApplicationContext(), mileageEntry.getDate())));
        } else {
            contentMileageBinding.textViewLastEntry.setText("");
        }
    }

    private boolean inputValid() {
        boolean inputValid = true;

        String dateString = contentMileageBinding.textFieldDate.getText().toString();
        String mileageString = contentMileageBinding.editTextMileage.getText().toString();

        contentMileageBinding.textFieldDate.setError(null);
        contentMileageBinding.editTextMileage.setError(null);

        if (dateString.isEmpty()) {
            contentMileageBinding.textFieldDate.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                contentMileageBinding.textFieldDate.setError(getString(R.string.validation_invalid_date));
                inputValid = false;
            }
        }

        if (mileageString.isEmpty()) {
            contentMileageBinding.editTextMileage.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                Integer.parseInt(mileageString);
            } catch (NumberFormatException e) {
                contentMileageBinding.editTextMileage.setError(getString(R.string.validation_invalid_mileage));
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
