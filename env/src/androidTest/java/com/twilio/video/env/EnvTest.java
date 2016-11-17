package com.twilio.video.env;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class EnvTest {
    String DUMMY_ENV_KEY = "DUMMY_ENV";
    String DUMMY_ENV_VALUE1 = "DUMMY_ENV_VALUE1";
    String DUMMY_ENV_VALUE2 = "DUMMY_ENV_VALUE2";

    @Test
    public void shouldSetEnvVariable() {
        Env.set(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY, DUMMY_ENV_VALUE1, true);
        assertEquals(DUMMY_ENV_VALUE1, Env.get(InstrumentationRegistry.getContext(),  DUMMY_ENV_KEY));
    }

    @Test
    public void shouldOverwriteEnvValue() {
        Env.set(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY, DUMMY_ENV_VALUE1, true);
        assertEquals(DUMMY_ENV_VALUE1, Env.get(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY));
        Env.set(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY, DUMMY_ENV_VALUE2, true);
        assertEquals(DUMMY_ENV_VALUE2, Env.get(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY));
    }

    @Test
    public void shouldNotOverwriteEnvValue() {
        Env.set(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY, DUMMY_ENV_VALUE1, true);
        assertEquals(DUMMY_ENV_VALUE1, Env.get(InstrumentationRegistry.getContext(),  DUMMY_ENV_KEY));
        Env.set(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY, DUMMY_ENV_VALUE2, false);
        assertEquals(DUMMY_ENV_VALUE1, Env.get(InstrumentationRegistry.getContext(), DUMMY_ENV_KEY));
    }
}
