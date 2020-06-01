package android.example.vehiclemaintenancetracker.data;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
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
        MileageEntry mileageEntry = new MileageEntry(30_000, "1967/10/17");
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
        MileageEntry mileageEntry1 = new MileageEntry(30_000, "1967/10/17");
        MileageEntry mileageEntry2 = new MileageEntry(30_002, "2020/10/17");
        MileageEntry mileageEntry3 = new MileageEntry(30_001, "1968/10/17");

        mileageEntryDao.insert(mileageEntry1);
        mileageEntryDao.insert(mileageEntry2);
        mileageEntryDao.insert(mileageEntry3);

        List<MileageEntry> mileageEntries = mileageEntryDao.getAll();

        assertEquals(3, mileageEntries.size());

        MileageEntry latestMileageEntry = mileageEntryDao.getMostRecentMileage();

        assertEquals(mileageEntry2.getDate(), latestMileageEntry.getDate());
        assertEquals(mileageEntry2.getMileage(), latestMileageEntry.getMileage());

        mileageEntryDao.delete(mileageEntry1);
        mileageEntryDao.delete(mileageEntry2);
        mileageEntryDao.delete(mileageEntry3);
    }

    @Test
    public void testNoData() {
        assertNull(mileageEntryDao.getMostRecentMileage());
        assertEquals(0, mileageEntryDao.getAll().size());
    }
}
