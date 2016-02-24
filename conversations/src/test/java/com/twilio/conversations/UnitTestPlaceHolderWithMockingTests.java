package com.twilio.conversations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import android.content.Context;

@RunWith(MockitoJUnitRunner.class)
public class UnitTestPlaceHolderWithMockingTests {
    private static final String FAKE_PACAKGE = "some.fake.package";

    @Mock Context mockContext;

    @Before
    public void setup() {
        // Given a mocked Context injected into the object under test...
        when(mockContext.getPackageName())
                .thenReturn(FAKE_PACAKGE);
    }

    @After
    public void teardown() {
        // TODO
    }

    @Test
    public void shouldNotThrowStubException() {
        assertEquals(FAKE_PACAKGE, mockContext.getPackageName());
    }
}
