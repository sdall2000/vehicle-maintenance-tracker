package android.example.vehiclemaintenancetracker.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class FirebaseDatabaseTest {
    private static final String TAG = "FirebaseDatabaseTest";

    @Test
    public void testFirebase() throws InterruptedException {
        // TODO we wouldn't normally want a unit test to hit the real server.
        // TODO but this is just to test fetching and packaging the data.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("vehicleModels");

        final CountDownLatch latch = new CountDownLatch(1);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, ds.getKey() + ", " + ds.getChildren().iterator().next().getValue());
                }

                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                fail("Could not get years");
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testHelperGetVehicles() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        FirebaseDatabaseUtils.getInstance().getVehicles(new FirebaseDatabaseUtils.HelperListener<Map<String, Vehicle>>() {
            @Override
            public void onDataReady(Map<String, Vehicle> data) {
                Log.i(TAG, "Got data: " + data.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fail("Could not get vehicles");
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
    }
    @Test

    public void testHelperGetVehicle() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        FirebaseDatabaseUtils.getInstance().getVehicle("1", new FirebaseDatabaseUtils.HelperListener<Vehicle>() {
            @Override
            public void onDataReady(Vehicle data) {
                assertEquals("Toyota", data.getMake());
                assertEquals("RAV4", data.getModel());
                assertEquals(2016, data.getYear());
                assertEquals("1", data.getMaintenanceScheduleUid());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fail("Could not get vehicle");
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testHelperGetYears() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        FirebaseDatabaseUtils.getInstance().getYears(new FirebaseDatabaseUtils.HelperListener<Set<Integer>>() {
            @Override
            public void onDataReady(Set<Integer> data) {
                Iterator<Integer> it = data.iterator();
                assertEquals((int) it.next(), 2009);
                assertEquals((int) it.next(), 2016);
                assertEquals((int) it.next(), 2018);

                assertFalse(it.hasNext());

                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fail("Could not get years");
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
    }
}
