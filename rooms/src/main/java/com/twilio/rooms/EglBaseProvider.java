package com.twilio.rooms;

import org.webrtc.EglBase;

final class EglBaseProvider {
    private static EglBase rootEglBase;

    public static EglBase provideEglBase() {
        if (rootEglBase == null) {
            rootEglBase = EglBase.create();
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
