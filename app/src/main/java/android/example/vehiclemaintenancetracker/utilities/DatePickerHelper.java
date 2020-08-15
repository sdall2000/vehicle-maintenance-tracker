package android.example.vehiclemaintenancetracker.utilities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class to support a date picker.
 */
public class DatePickerHelper {
    /**
     * Configures the views to support a date picker
     * @param context The application context
     * @param calendarTextView The text view that shows the date.  Clicking this field also brings
     *                         up the calendar
     * @param calendarImageButton The button that lets the user bring up the calendar
     * @param dateFormat The date formatter
     * @param initialDate The initial date to set in the calendar and in the text view
     */
    public static void configureDateChooser(
            final Context context,
            final TextView calendarTextView,
            final ImageButton calendarImageButton,
            final DateFormat dateFormat,
            final Date initialDate) {

        // Create the onDateSetListener.  This gets fired when the user clicks ok on the
        // date picker dialog.
        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                calendarTextView.setText(dateFormat.format(calendar.getTime()));
            }
        };

        // Convert date to a calendar.
        final Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTime(initialDate);

        // Create the on click listener to bring up the date picker.
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(
                        context,
                        onDateSetListener,
                        initialCalendar.get(Calendar.YEAR),
                        initialCalendar.get(Calendar.MONTH),
                        initialCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        };

        calendarTextView.setText(dateFormat.format(initialDate));

        // Attach listener to both the text view and the image button.
        calendarTextView.setOnClickListener(onClickListener);
        calendarImageButton.setOnClickListener(onClickListener);
    }
}
