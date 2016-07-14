package com.twilio.rooms;

class CoreError {
    private String errorDomain;
    private int errorCode;
    private String errorMesage;

    public CoreError(String errorDomain, int errorCode, String errorMessage) {
        this.errorDomain = errorDomain;
        this.errorCode = errorCode;
        this.errorMesage = errorMessage;
    }

    public int getCode() {
        return errorCode;
    }

    public String getDomain() {
        return errorDomain;
    }

    public String getMessage() {
        return errorMesage;
    }
}
