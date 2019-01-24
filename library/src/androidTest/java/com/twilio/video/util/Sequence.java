package com.twilio.video.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
 * Utility for tests to assert that a sequence occurred during a given amount of time.
 */
public class Sequence {
    private static final String SEQUENCE_TIMEOUT_MESSAGE_TEMPLATE =
            "Timed out waiting for sequence %s";
    private final CountDownLatch latch;
    private final List<String> expectedSequence;
    private final List<String> actualSequence;

    public Sequence(final @NonNull List<String> expectedSequence) {
        this.expectedSequence = expectedSequence;
        this.actualSequence = new ArrayList<>(expectedSequence.size());
        this.latch = new CountDownLatch(expectedSequence.size());
    }

    public void addEvent(String sequenceEvent) {
        actualSequence.add(sequenceEvent);
        latch.countDown();
    }

    public void assertSequenceOccurred(long timeout, TimeUnit unit) throws InterruptedException {
        assertTrue(
                String.format(SEQUENCE_TIMEOUT_MESSAGE_TEMPLATE, expectedSequence),
                latch.await(timeout, unit));
        assertArrayEquals(expectedSequence.toArray(), actualSequence.toArray());
    }
}
