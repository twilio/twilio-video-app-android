package com.twilio.conversations.helper;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class TwilioConversationsTestsBase {
    /**
     * Intentionally calling destroy in the test suite setup as a cautionary measure to ensure
     * we start the test suite with the sdk completely torn down
     */
    @BeforeClass
    public static void suiteSetup() {
        TwilioConversationsHelper.destroy();
    }

    /**
     * Intentionally calling destroy in the test suite teardown as a cautionary measure to ensure
     * we start the next test suite with the sdk completely torn down
     */
    @AfterClass
    public static void suiteTeardown() {
        TwilioConversationsHelper.destroy();
    }
}
