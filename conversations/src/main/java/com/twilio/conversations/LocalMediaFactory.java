package com.twilio.conversations;

/**
 * A factory for creating an instance of {@link LocalMedia}
 *
 */
public class LocalMediaFactory {
    /**
     * Creates a new instance of the {@link LocalMedia}
     *
     * <p>The {@link LocalMedia.Listener} is invoked on the thread that provides the
     * LocalMediaListener instance.</p>
     *
     * @return instance of local media
     */
    public static LocalMedia createLocalMedia(LocalMedia.Listener localMediaListener) {
        return new LocalMedia(localMediaListener);
    }
}
