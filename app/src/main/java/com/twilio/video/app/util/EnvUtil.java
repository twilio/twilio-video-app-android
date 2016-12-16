package com.twilio.video.app.util;

public class EnvUtil {
    private static final String TWILIO_DEV_ENV = "Development";
    private static final String TWILIO_STAGE_ENV = "Staging";
    private static final String TWILIO_PROD_ENV = "Production";
    public static final String TWILIO_ENV_KEY = "TWILIO_ENVIRONMENT";

    public static String getNativeEnvironmentVariableValue(String environment) {
        if (environment.equals(SimplerSignalingUtils.DEV)) {
            return TWILIO_DEV_ENV;
        } else if (environment.equals(SimplerSignalingUtils.STAGE)) {
            return TWILIO_STAGE_ENV;
        } else {
            return TWILIO_PROD_ENV;
        }
    }

}
