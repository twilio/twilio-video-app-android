package com.twilio.audioswitch;

import androidx.annotation.NonNull;

/**
 * This class represents a single audio device that has been retrieved by the {@link
 * AudioDeviceSelector}. It contains information about the audio device.
 */
public class AudioDevice {
    /** The name of the audio device. */
    public final @NonNull String name;

    /** The type of audio device defined in {@link Type}. */
    public final @NonNull Type type;

    AudioDevice(@NonNull Type type, @NonNull String name) {
        this.type = type;
        this.name = name;
    }

    /** A type of audio device. */
    public enum Type {
        SPEAKERPHONE,
        WIRED_HEADSET,
        EARPIECE,
        BLUETOOTH
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioDevice that = (AudioDevice) o;

        if (!name.equals(that.name)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
