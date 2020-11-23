package android.example.vehiclemaintenancetracker.ui;

import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.data.DateConverter;
import android.widget.DatePicker;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.PickerActions.setDate;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final String VEHICLE_NAME = "Tesla Model 3";
    private static final String VEHICLE_DESCRIPTION = "2018 Tesla Model 3";
    private static final String STARTING_MILEAGE = "123";

    private static final int STARTING_DAY = 1;
    private static final int STARTING_MONTH = 5;
    private static final int STARTING_YEAR = 2018;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testVehicleChooser() {
        // Click on the settings button, which takes us to the vehicle choooser.
        onView(withId(R.id.nav_settings)).perform(click());

        // Verify that the vehicle chooser is displayed.
        onView(withId(R.id.vehicleChooserFragmentLayout)).check(matches(isDisplayed()));

        // Enter name, description, starting mileage, date
        onView(withId(R.id.editTextName)).perform(replaceText(VEHICLE_NAME));
        onView(withId(R.id.editTextDescription)).perform(replaceText(VEHICLE_DESCRIPTION));
        onView(withId(R.id.editTextMileage)).perform(replaceText(STARTING_MILEAGE));

        // Bring up the date picker dialog
        onView(withId(R.id.imageButton)).perform(click());

        // Set the date
        onView(isAssignableFrom(DatePicker.class)).perform(setDate(STARTING_YEAR, STARTING_MONTH, STARTING_DAY));

        // CLick "ok".  The standard DatePickerDialog uses android.R.id.button1 for the "ok" button.
        onView(withId(android.R.id.button1)).perform(click());

        // Submit form
        onView(withId(R.id.buttonSelectVehicle)).perform(click());

        onView(withId(R.id.dashboardFragment)).check(matches(isDisplayed()));

        // Verify correct text fields
        onView(withId(R.id.textViewVehicle)).check(matches(withText(VEHICLE_DESCRIPTION)));

        // There are multiple uses of textViewMileage - have to specify the parent here to get the right one.
        onView(allOf(withId(R.id.textViewMileage), withParent(withId(R.id.dashboardFragment)))).check(matches(withText(STARTING_MILEAGE)));

        Calendar calendar = Calendar.getInstance();
        calendar.set(STARTING_YEAR, STARTING_MONTH - 1, STARTING_DAY);
        Date date = calendar.getTime();

        String dateString = DateConverter.convertDateToString(mainActivityRule.getActivity(), date);
        onView(withId(R.id.textViewMileageReported)).check(matches(withText(dateString)));

        // Verify correct maintenance items
        // TODO is there not another way to verify recycler view items without this 3rd party helper class?
        RecyclerViewMatcher matcher = new RecyclerViewMatcher(R.id.recyclerView);

        onView(matcher.atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.textViewService), withText("Oil Change")))));
        onView(matcher.atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.textViewMileage), withText("3,623")))));
        onView(matcher.atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.textViewDate), withText("7/30/18")))));

        onView(matcher.atPosition(1)).check(matches(hasDescendant(allOf(withId(R.id.textViewService), withText("Replace Wiper Blades")))));
        onView(matcher.atPosition(1)).check(matches(hasDescendant(allOf(withId(R.id.textViewMileage), withText("12,623")))));
        onView(matcher.atPosition(1)).check(matches(hasDescendant(allOf(withId(R.id.textViewDate), withText("10/28/18")))));
    }
}
