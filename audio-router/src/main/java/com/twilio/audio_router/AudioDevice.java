package com.twilio.audio_router;

import androidx.annotation.NonNull;

public class AudioDevice {
    public final @NonNull String name;
    public final @NonNull Type type;

    public AudioDevice(@NonNull Type type, @NonNull String name) {
        this.type = type;
        this.name = name;
    }

    /** Audio device types */
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
