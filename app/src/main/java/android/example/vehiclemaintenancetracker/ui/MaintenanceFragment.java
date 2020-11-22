package android.example.vehiclemaintenancetracker.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.MaintenanceEntry;
import android.example.vehiclemaintenancetracker.data.MaintenanceScheduleDetailJoined;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.FragmentMaintenanceBinding;
import android.example.vehiclemaintenancetracker.ui.widget.VehicleMaintenanceTrackerAppWidget;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.example.vehiclemaintenancetracker.utilities.DatePickerHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MaintenanceFragment extends Fragment {

    private FragmentMaintenanceBinding binding;
    private DateFormat dateFormat;

    public MaintenanceFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        dateFormat = android.text.format.DateFormat.getDateFormat(context);

        binding = FragmentMaintenanceBinding.inflate(inflater, container, false);

        // Use the DatePickerHelper to configure date picker functionality and set initial value.
        DatePickerHelper.configureDateChooser(
                context,
                binding.textViewDatePerformed,
                binding.imageButton,
                dateFormat,
                new Date());

        binding.buttonSubmitMaintenance.setOnClickListener(createOnClickListener(context));

        int maintenanceScheduleUid = MaintenanceFragmentArgs.fromBundle(getArguments()).getMaintenanceScheduleUid();

        populateSpinner(maintenanceScheduleUid);

        return binding.getRoot();
    }

    private View.OnClickListener createOnClickListener(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValid()) {
                    String dateString = binding.textViewDatePerformed.getText().toString();
                    String mileageString = binding.editTextMileage.getText().toString();

                    try {
                        // Read into a Date object so we can store in the specific format we want.
                        final Date date = dateFormat.parse(dateString);
                        final int mileage = Integer.parseInt(mileageString);

                        // Remove the soft keyboard.
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.maintenaceFragmentLayout.getWindowToken(), 0);

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
                                AppDatabase appDatabase = AppDatabase.getInstance(context);

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

                                AppExecutor.getInstance().getUiExecutor().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Go to previous fragment
                                        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                                        navController.popBackStack();
                                    }
                                });
                            }
                        });

                    } catch (ParseException e) {
                        // Should never happen because we always validate the fields before using them.
                        Toast.makeText(context, R.string.validation_error_unhandled, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    private void populateSpinner(final int maintenanceScheduleUid) {
        final Context context = getContext();

        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final List<MaintenanceScheduleDetailJoined> maintenanceScheduleDetailList =
                        AppDatabase
                                .getInstance(context)
                                .getMaintenanceScheduleDetailDao().
                                getMaintenanceScheduleDetailJoined(maintenanceScheduleUid);

                AppExecutor.getInstance().getUiExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<MaintenanceScheduleDetailJoined> adapter = new ArrayAdapter<>(
                                context,
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
        Context context = getContext();

        Intent intent = new Intent(context, VehicleMaintenanceTrackerAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, VehicleMaintenanceTrackerAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
