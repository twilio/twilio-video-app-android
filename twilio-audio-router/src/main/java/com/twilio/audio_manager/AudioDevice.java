package com.twilio.audio_manager;

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
        if (o == this) {
            return true;
        } else if (!(o instanceof AudioDevice)) {
            return false;
        } else {
            return name.equals(((AudioDevice) o).name) && type.equals(((AudioDevice) o).type);
        }
    }
}
