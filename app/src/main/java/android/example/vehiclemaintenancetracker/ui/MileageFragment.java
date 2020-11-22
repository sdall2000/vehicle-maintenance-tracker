package android.example.vehiclemaintenancetracker.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.AppDatabase;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.example.vehiclemaintenancetracker.data.MileageEntry;
import android.example.vehiclemaintenancetracker.databinding.FragmentMileageBinding;
import android.example.vehiclemaintenancetracker.ui.widget.VehicleMaintenanceTrackerAppWidget;
import android.example.vehiclemaintenancetracker.utilities.AppExecutor;
import android.example.vehiclemaintenancetracker.utilities.DatePickerHelper;
import android.example.vehiclemaintenancetracker.utilities.ValueFormatter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class MileageFragment extends Fragment {
    private FragmentMileageBinding fragmentMileageBinding;
    private DateFormat dateFormat;

    public MileageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getContext();

        // Inflate the layout for this fragment
        fragmentMileageBinding = FragmentMileageBinding.inflate(inflater, container, false);

        dateFormat = android.text.format.DateFormat.getDateFormat(context);

        // Use the DatePickerHelper to configure date picker functionality and set initial value.
        DatePickerHelper.configureDateChooser(
                context,
                fragmentMileageBinding.textFieldDate,
                fragmentMileageBinding.imageButton,
                dateFormat,
                new Date());

        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final MileageEntry mileageEntry = AppDatabase.getInstance(context).getMileageEntryDao().getMostRecentMileage();

                AppExecutor.getInstance().getUiExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        populateLastEntry(mileageEntry);
                    }
                });
            }
        });

        fragmentMileageBinding.buttonSubmit.setOnClickListener(createOnClickListener(context));

        return fragmentMileageBinding.getRoot();
    }

    private void populateLastEntry(MileageEntry mileageEntry) {
        if (mileageEntry != null) {
            fragmentMileageBinding.textViewLastEntry.setText(
                    getString(
                            R.string.last_mileage_entered_label,
                            ValueFormatter.formatDistance(mileageEntry.getMileage()),
                            DateConverter.convertDateToString(getContext(), mileageEntry.getDate())));
        } else {
            fragmentMileageBinding.textViewLastEntry.setText("");
        }
    }

    private View.OnClickListener createOnClickListener(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValid()) {
                    String dateString = fragmentMileageBinding.textFieldDate.getText().toString();
                    String mileageString = fragmentMileageBinding.editTextMileage.getText().toString();

                    try {
                        // Read into a Date object so we can store in the specific format we want.
                        final Date date = dateFormat.parse(dateString);
                        final int mileage = Integer.parseInt(mileageString);

                        // Remove the soft keyboard.
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(fragmentMileageBinding.editTextMileage.getWindowToken(), 0);

                        // Must be done on a background thread.
                        AppExecutor.getInstance().getDbExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                // Insert a new mileage entry
                                MileageEntry mileageEntry = new MileageEntry(mileage, date);
                                AppDatabase.getInstance(context).getMileageEntryDao().insert(mileageEntry);

                                // Update the app widgets.
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

    private boolean inputValid() {
        boolean inputValid = true;

        String dateString = fragmentMileageBinding.textFieldDate.getText().toString();
        String mileageString = fragmentMileageBinding.editTextMileage.getText().toString();

        fragmentMileageBinding.textFieldDate.setError(null);
        fragmentMileageBinding.editTextMileage.setError(null);

        if (dateString.isEmpty()) {
            fragmentMileageBinding.textFieldDate.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                fragmentMileageBinding.textFieldDate.setError(getString(R.string.validation_invalid_date));
                inputValid = false;
            }
        }

        if (mileageString.isEmpty()) {
            fragmentMileageBinding.editTextMileage.setError(getString(R.string.validation_required_field));
            inputValid = false;
        } else {
            try {
                Integer.parseInt(mileageString);
            } catch (NumberFormatException e) {
                fragmentMileageBinding.editTextMileage.setError(getString(R.string.validation_invalid_mileage));
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