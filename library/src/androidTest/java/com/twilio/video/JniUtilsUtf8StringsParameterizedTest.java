/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.support.test.InstrumentationRegistry;

import com.getkeepsafe.relinker.ReLinker;
import com.twilio.video.base.BaseVideoTest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class JniUtilsUtf8StringsParameterizedTest extends BaseVideoTest {
    private static final int NUM_RANDOM_STRINGS = 100;
    private static final int RANDOM_STRING_LENGTH = 100;

    @BeforeClass
    public static void classSetup() {
        ReLinker.loadLibrary(InstrumentationRegistry.getContext(), "jingle_peerconnection_so");
    }

    @Parameterized.Parameters
    public static List<String> data() {
        List<String> randomStrings = new ArrayList<>(NUM_RANDOM_STRINGS);

        for (int i = 0 ; i < NUM_RANDOM_STRINGS ; i++) {
            randomStrings.add(i, random(RANDOM_STRING_LENGTH));
        }

        return randomStrings;
    }

    private final String randomString;

    public JniUtilsUtf8StringsParameterizedTest(String randomString) {
        this.randomString = randomString;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
    }

    @Test
    public void shouldConvertJavaString() {
        assertEquals(randomString, JniUtils.javaUtf16StringToStdString(randomString));
    }
}
