package android.example.vehiclemaintenancetracker;

import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.ActivityMileageBinding;
import android.example.vehiclemaintenancetracker.databinding.ContentMileageBinding;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MileageActivity extends AppCompatActivity {
    private ActivityMileageBinding activityMileageBinding;
    private ContentMileageBinding contentMileageBinding;
    private DateFormat dateFormat;

    private EditText editTextDate;
    private EditText editTextMileage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMileageBinding = ActivityMileageBinding.inflate(getLayoutInflater());
        setContentView(activityMileageBinding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        // Put the current date in the date field.
        contentMileageBinding = activityMileageBinding.contentMileage;
        editTextDate = contentMileageBinding.editTextDate;
        editTextMileage = contentMileageBinding.editTextMileage;

        editTextDate.setText(dateFormat.format(new Date()));

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
                    String dateString = editTextDate.getText().toString();
                    String mileageString = editTextMileage.getText().toString();

                    try {
                        // Read into a Date object so we can store in the specific format we want.
                        final Date date = dateFormat.parse(dateString);
                        final int mileage = Integer.parseInt(mileageString);

                        // Must be done on a background thread.
                        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                MileageEntry mileageEntry = new MileageEntry(mileage, date);
                                AppDatabase.getInstance(MileageActivity.this).getMileageEntryDao().insert(mileageEntry);
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
                            mileageEntry.getMileage(),
                            DateConverter.convertDateToString(getApplicationContext(), mileageEntry.getDate())));
        } else {
            contentMileageBinding.textViewLastEntry.setText("");
        }
    }

    private boolean inputValid() {
        boolean inputValid = true;

        String dateString = editTextDate.getText().toString();
        String mileageString = editTextMileage.getText().toString();

        editTextDate.setError(null);
        editTextMileage.setError(null);

        if (dateString.isEmpty()) {
            editTextDate.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                editTextDate.setError(getString(R.string.validation_invalid_date));
                inputValid = false;
            }
        }

        if (mileageString.isEmpty()) {
            editTextMileage.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                Integer.parseInt(mileageString);
            } catch (NumberFormatException e)
            {
                editTextMileage.setError(getString(R.string.validation_invalid_mileage));
                inputValid = false;
            }
        }

        return inputValid;
    }
}
