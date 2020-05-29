package android.example.vehiclemaintenancetracker;

import android.example.vehiclemaintenancetracker.databinding.ActivityMileageBinding;
import android.example.vehiclemaintenancetracker.databinding.ContentMileageBinding;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.util.Date;

public class MileageActivity extends AppCompatActivity {
    private ActivityMileageBinding activityMileageBinding;
    private ContentMileageBinding binding;
    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMileageBinding = ActivityMileageBinding.inflate(getLayoutInflater());
        setContentView(activityMileageBinding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        // Put the current date in the date field.
        activityMileageBinding.contentMileage.editTextDate.setText(dateFormat.format(new Date()));

        activityMileageBinding.contentMileage.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MileageActivity.this, "Clicked", Toast.LENGTH_LONG).show();
            }
        });
    }
}
