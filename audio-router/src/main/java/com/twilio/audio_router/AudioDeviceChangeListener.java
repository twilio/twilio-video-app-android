package com.twilio.audio_router;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/** A listener that receives the most recently available {@link AudioDevice}s. */
public interface AudioDeviceChangeListener {

    /**
     * Receives a list of the most recently available {@link AudioDevice}s. Also provides the
     * currently selected {@link AudioDevice} from {@link AudioDeviceSelector}.
     *
     * @param audioDevices the list of {@link AudioDevice}s or an empty list if none are available.
     * @param selectedAudioDevice the currently selected device or {@code null} if none have been
     *     selected.
     */
    void onAvailableAudioDevices(
            @NonNull List<AudioDevice> audioDevices, @Nullable AudioDevice selectedAudioDevice);
}
