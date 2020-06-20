package android.example.vehiclemaintenancetracker;

import android.content.Intent;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.databinding.ActivityMainBinding;
import android.example.vehiclemaintenancetracker.ui.main.SectionsPagerAdapter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private final static int ACTIVITY_RESULT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new Timber.DebugTree() {
            @Override
            protected void log(int priority, String tag, @NotNull String message, Throwable t) {
                super.log(priority, "*** timber *** " + tag, message, t);
            }
        });

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


        binding.buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VehicleChooserActivity.class);
                String selectedVehicleUid = AppDatabase.getVehicleUid(MainActivity.this);

                if (selectedVehicleUid != null) {
                    intent.putExtra(AppDatabase.SELECTED_VEHICLE_UID_KEY, selectedVehicleUid);
                }

                startActivityForResult(intent, ACTIVITY_RESULT_REQUEST_CODE);
            }
        });

        // TODO don't think we are going to use FAB
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String vehicleUidKey = "";

        if (data != null && data.hasExtra(AppDatabase.SELECTED_VEHICLE_UID_KEY)) {
            vehicleUidKey = data.getStringExtra(AppDatabase.SELECTED_VEHICLE_UID_KEY);
            AppDatabase.setVehicleUid(this, vehicleUidKey);
        }

        Timber.d("onActivityResult requestCode: %d, resultCode: %d, vehicle key: %s",
                requestCode, resultCode, vehicleUidKey);
    }
}