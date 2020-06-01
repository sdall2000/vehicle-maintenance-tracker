package android.example.vehiclemaintenancetracker.data;

import android.content.Context;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
    // This is the format we save in the string field in the database.
    // The string value should sort properly.  We are not concerned about time of day at this point.
    private static DateFormat saveDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @TypeConverter
    public static Date fromString(String value) {
        try {
            return value == null ? null : saveDateFormat.parse(value);
        } catch (ParseException e) {
            // TODO log error
            return null;
        }
    }

    @TypeConverter
    public static String dateToString(Date date) {
        return date == null ? null : saveDateFormat.format(date);
    }

    // This method does not support type converter.  It's a utility method to create a date string
    // using the user's locale.
    public static String convertDateToString(Context context, Date date) {
        // TODO We can probably set the date format once.
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return dateFormat.format(date);
    }
}
