package android.example.vehiclemaintenancetracker.data;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {
    private AppDatabase appDatabase;
    private MileageEntryDao mileageEntryDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();

        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mileageEntryDao = appDatabase.getMileageEntryDao();
    }

    @After
    public void closeDb() {
        appDatabase.close();
    }

    @Test
    public void writeThenRead() {
        MileageEntry mileageEntry = new MileageEntry(30_000, createDate(2020, 10, 10));
        mileageEntryDao.insert(mileageEntry);

        List<MileageEntry> mileageEntries = mileageEntryDao.getAll();

        assertEquals(1, mileageEntries.size());

        MileageEntry loadedMileage = mileageEntries.get(0);

        assertEquals(mileageEntry.getDate(), loadedMileage.getDate());
        assertEquals(mileageEntry.getMileage(), loadedMileage.getMileage());

        mileageEntryDao.delete(mileageEntry);
    }

    @Test
    public void testGetLatestMileage() {
        MileageEntry mileageEntry1 = new MileageEntry(30_000, createDate(2010, 10, 5));
        MileageEntry latestMileageEntry = new MileageEntry(30_002, createDate(2019, 9, 4));
        MileageEntry mileageEntry3 = new MileageEntry(30_001, createDate(2012, 8, 3));

        mileageEntryDao.insert(mileageEntry1);
        mileageEntryDao.insert(latestMileageEntry);
        mileageEntryDao.insert(mileageEntry3);

        List<MileageEntry> mileageEntries = mileageEntryDao.getAll();

        assertEquals(3, mileageEntries.size());

        MileageEntry latestMileageEntryCompare = mileageEntryDao.getMostRecentMileage();

        assertEquals(latestMileageEntry.getDate(), latestMileageEntryCompare.getDate());
        assertEquals(latestMileageEntry.getMileage(), latestMileageEntryCompare.getMileage());

        mileageEntryDao.delete(mileageEntry1);
        mileageEntryDao.delete(latestMileageEntry);
        mileageEntryDao.delete(mileageEntry3);
    }

    // Helper method to create a Date without the time component.
    private Date createDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    @Test
    public void testNoData() {
        assertNull(mileageEntryDao.getMostRecentMileage());
        assertEquals(0, mileageEntryDao.getAll().size());
    }
}
