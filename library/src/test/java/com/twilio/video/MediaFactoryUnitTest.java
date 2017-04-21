package com.twilio.video;


import org.junit.Test;

public class MediaFactoryUnitTest {

    @Test(expected = NullPointerException.class)
    public void instance_shouldFailWithNullContext() {
        MediaFactory.instance(null);
    }
}
