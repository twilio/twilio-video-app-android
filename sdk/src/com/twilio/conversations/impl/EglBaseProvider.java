package com.twilio.conversations;

import org.webrtc.EglBase;

public final class EglBaseProvider {
    private static EglBase rootEglBase;

    public static EglBase provideEglBase() {
        if (rootEglBase == null) {
            rootEglBase = new EglBase();
        }

        return rootEglBase;
    }

    public static void releaseEglBase() {
        if (rootEglBase != null) {
            rootEglBase.release();
            rootEglBase = null;
        }
    }

    private EglBaseProvider() {
        // No instances
    }
}
