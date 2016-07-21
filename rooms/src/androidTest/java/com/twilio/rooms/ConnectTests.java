package com.twilio.rooms;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.AccessManager;
import com.twilio.rooms.activity.RoomsTestActivity;
import com.twilio.rooms.helper.AccessTokenHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConnectTests {
    private final static String TEST_USER = "TEST_USER";


    private Context context;

    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule = new ActivityTestRule<>(
            RoomsTestActivity.class);

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        RoomsClient.setLogLevel(LogLevel.DEBUG);
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenRoomListenerIsNull() {
        RoomsClient roomsClient = new RoomsClient(context, accessManager(), clientListener());
        roomsClient.connect(null);
    }

    @Test
    public void connect_shouldConnectToANewRoom() throws InterruptedException {
        final CountDownLatch connectedCountdownLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        RoomsClient roomsClient = new RoomsClient(context, accessManager, new RoomsClient.Listener() {
            @Override
            public void onConnected(Room room) {
                assertNotNull(room.getSid());

                connectedCountdownLatch.countDown();
            }

            @Override
            public void onConnectFailure(RoomsException error) {
                fail("Unexpected onConnectFailure callback");
            }

            @Override
            public void onDisconnected(Room room, RoomsException error) {
                fail("Unexpected onDisconnected callback");

            }
        });

        roomsClient.connect(roomListener());

        assertTrue(connectedCountdownLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldDisconnectFromANewRoom() throws InterruptedException {
        final CountDownLatch connectedCountdownLatch = new CountDownLatch(1);
        final CountDownLatch disconnectedCountdownLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        final RoomsClient roomsClient = new RoomsClient(context, accessManager, new RoomsClient.Listener() {
            @Override
            public void onConnected(Room room) {
                connectedCountdownLatch.countDown();

                room.disconnect();
            }

            @Override
            public void onConnectFailure(RoomsException error) {
                fail("Unexpected onConnectFailure callback");
            }

            @Override
            public void onDisconnected(Room room, RoomsException error) {
                disconnectedCountdownLatch.countDown();
            }
        });

        roomsClient.connect(roomListener());

        assertTrue(connectedCountdownLatch.await(20, TimeUnit.SECONDS));

        assertTrue(disconnectedCountdownLatch.await(5, TimeUnit.SECONDS));

    }

    private AccessManager accessManager() {
        return new AccessManager(context, null, null);
    }

    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onParticipantConnected(Room room, Participant participant) {

            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {

            }
        };
    }

    private RoomsClient.Listener clientListener() {
        return new RoomsClient.Listener() {
            @Override
            public void onConnected(Room room) {

            }

            @Override
            public void onConnectFailure(RoomsException error) {

            }

            @Override
            public void onDisconnected(Room room, RoomsException error) {

            }
        };
    }
}

