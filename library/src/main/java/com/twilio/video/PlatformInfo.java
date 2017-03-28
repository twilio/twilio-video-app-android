package com.twilio.video;

final class PlatformInfo {
    private static final String PLATFORM_NAME = "Android";
    private static long nativeHandle = 0;

    private PlatformInfo(){}

    static synchronized long getNativeHandle() {
        if (nativeHandle == 0) {
            nativeHandle = nativeCreate(
                PLATFORM_NAME,
                android.os.Build.VERSION.RELEASE,
                android.os.Build.MANUFACTURER,
                android.os.Build.MODEL,
                Video.getVersion(),
                System.getProperty("os.arch"));
        }
        return nativeHandle;
    }

    static synchronized void release() {
        if (nativeHandle != 0) {
            nativeRelease(nativeHandle);
            nativeHandle = 0;
        }
    }

    private static native long nativeCreate(String platformName,
                                            String platformVersion,
                                            String hwDeviceManufacturer,
                                            String hwDeviceModel,
                                            String sdkVersion,
                                            String hwDeviceArch);

    private static native void nativeRelease(long nativeHandle);
}
