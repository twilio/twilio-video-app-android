package com.twilio.video.util;

public enum Environment {
    DEV("dev"),
    STAGE("stage"),
    PROD("prod");

    private final String environment;

    Environment(String environment) {
        this.environment = environment;
    }

    public static Environment fromString(String environment) {
        if (environment.equals(DEV.environment)) {
            return DEV;
        } else if (environment.equals(STAGE.environment)) {
            return STAGE;
        } else if (environment.equals(PROD.environment)) {
            return PROD;
        } else {
            throw new RuntimeException("Unsupported environment string -> " + environment);
        }
    }
}
