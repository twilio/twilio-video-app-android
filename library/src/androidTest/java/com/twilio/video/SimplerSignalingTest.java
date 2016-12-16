package com.twilio.video;

import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.util.SimplerSignalingUtils;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SimplerSignalingTest {
    private final static String P2P = "P2P";
    private final static String SFU = "SFU";
    private final static String SFU_RECORDING = "SFU Recording";

    @Test
    public void configuration_shouldReturnConfigurationProfileSids() {
        String configurationProfileSid = SimplerSignalingUtils
                .getConfigurationProfileSid(BuildConfig.ENVIRONMENT, P2P);
        Assert.assertTrue(configurationProfileSid.startsWith("VS"));
        configurationProfileSid = SimplerSignalingUtils
                .getConfigurationProfileSid(BuildConfig.ENVIRONMENT, SFU);
        Assert.assertTrue(configurationProfileSid.startsWith("VS"));
        configurationProfileSid = SimplerSignalingUtils
                .getConfigurationProfileSid(BuildConfig.ENVIRONMENT, SFU_RECORDING);
        Assert.assertTrue(configurationProfileSid.startsWith("VS"));
        configurationProfileSid = SimplerSignalingUtils
                .getConfigurationProfileSid(BuildConfig.ENVIRONMENT, null);
        Assert.assertNull(configurationProfileSid);
        configurationProfileSid = SimplerSignalingUtils
                .getConfigurationProfileSid(BuildConfig.ENVIRONMENT, "DUMMY");
        Assert.assertNull(configurationProfileSid);
    }

    @Test
    public void configuration_shouldReturnUniqueConfigurationProfileSids() {
        String devSid = SimplerSignalingUtils
                .getConfigurationProfileSid(SimplerSignalingUtils.DEV, P2P);
        String stageSid = SimplerSignalingUtils
                .getConfigurationProfileSid(SimplerSignalingUtils.STAGE, P2P);
        String prodSid = SimplerSignalingUtils
                .getConfigurationProfileSid(SimplerSignalingUtils.PROD, P2P);

        Assert.assertNotNull(devSid);
        Assert.assertNotNull(stageSid);
        Assert.assertNotNull(prodSid);

        Assert.assertNotSame(devSid, stageSid);
        Assert.assertNotSame(stageSid, prodSid);
    }

}
