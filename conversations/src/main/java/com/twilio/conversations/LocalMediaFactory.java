package com.twilio.conversations;

import com.twilio.conversations.impl.LocalMediaImpl;


/**
 * A factory for creating an instance of {@link LocalMedia}
 *
 */
public class LocalMediaFactory {
    /**
     * Creates a new instance of the {@link LocalMedia}
     *
     * <p>Calling thread will be used to invoke all events on {@link LocalMediaListener}. If the
     * calling thread doesn't have Looper associated with it, SDK will attempt to use main thread
     * Handler instead.</p>
     *
     * @return instance of local media
     */
    public static LocalMedia createLocalMedia(LocalMediaListener localMediaListener) {
        return new LocalMediaImpl(localMediaListener);
    }
}
