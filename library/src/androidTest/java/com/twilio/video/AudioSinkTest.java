/*
 * Copyright (C) 2018 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import static android.content.Context.AUDIO_SERVICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.util.Log;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.util.Topology;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@LargeTest
public class AudioSinkTest extends BaseParticipantTest {
    private static final int COUNTDOWN_COUNT = 5;
    private static final int NUMBER_OF_SINKS = 2;
    private final Topology topology;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{Topology.P2P}, {Topology.GROUP}});
    }

    private boolean didWriteWavHeader = false;
    private AtomicBoolean isAudioSinkAttached = new AtomicBoolean(false);
    private CountDownLatch audioFramesReceivedSink1 = new CountDownLatch(COUNTDOWN_COUNT);
    private CountDownLatch audioFramesReceivedSink2 = new CountDownLatch(COUNTDOWN_COUNT);
    private CountDownLatch invalidAudioFramesReceived = new CountDownLatch(1);
    private CountDownLatch duplicateFramesReceivedLatch = new CountDownLatch(COUNTDOWN_COUNT);

    private AudioManager audioManager;

    private final AudioSink audioSink1 =
            (audioSample, encoding, sampleRate, channels) -> {
                if (!isAudioSinkAttached.get()) {
                    invalidAudioFramesReceived.countDown();
                }
                assertTrue(audioSample.capacity() > 0);
                assertEquals(AudioFormat.ENCODING_PCM_16BIT, encoding);
                assertTrue(sampleRate > 0);
                assertTrue(channels > 0);
                audioFramesReceivedSink1.countDown();
            };

    private final AudioSink audioSink2 =
            (audioSample, encoding, sampleRate, channels) -> {
                if (!isAudioSinkAttached.get()) {
                    invalidAudioFramesReceived.countDown();
                }
                assertTrue(audioSample.capacity() > 0);
                assertEquals(AudioFormat.ENCODING_PCM_16BIT, encoding);
                assertTrue(sampleRate > 0);
                assertTrue(channels > 0);
                audioFramesReceivedSink2.countDown();
            };

    public AudioSinkTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    @Override
    public void setup() throws InterruptedException {
        super.setup();
        audioManager =
                (AudioManager)
                        InstrumentationRegistry.getContext()
                                .getSystemService(Context.AUDIO_SERVICE);
        setAudioFocus();
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canAddAudioSinkToLocalAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));

        bobLocalAudioTrack.removeSink(audioSink1);
        isAudioSinkAttached.set(false);
    }

    @Test
    public void canRemoveAudioSinkFromLocalAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));

        bobLocalAudioTrack.removeSink(audioSink1);
        isAudioSinkAttached.set(false);

        assertEquals(0, audioFramesReceivedSink1.getCount());
        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void canAddAudioSinkFromRemoteAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));
        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .removeSink(audioSink1);
        isAudioSinkAttached.set(false);
    }

    @Test
    public void canRemoveAudioSinkFromRemoteAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();
        isAudioSinkAttached.set(true);
        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));

        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .removeSink(audioSink1);
        isAudioSinkAttached.set(false);

        assertEquals(0, audioFramesReceivedSink1.getCount());
        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void canAddMultipleSinksToLocalAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);
        bobLocalAudioTrack.addSink(audioSink2);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));
        assertTrue(audioFramesReceivedSink2.await(10, TimeUnit.SECONDS));

        bobLocalAudioTrack.removeSink(audioSink1);
        bobLocalAudioTrack.removeSink(audioSink2);
        isAudioSinkAttached.set(false);

        assertEquals(0, audioFramesReceivedSink1.getCount());
        assertEquals(0, audioFramesReceivedSink2.getCount());
        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void canAddMultipleSinksToRemoteAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .addSink(audioSink1);
        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .addSink(audioSink2);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));
        assertTrue(audioFramesReceivedSink2.await(10, TimeUnit.SECONDS));

        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .removeSink(audioSink1);
        bobRemoteParticipant
                .getRemoteAudioTracks()
                .get(0)
                .getRemoteAudioTrack()
                .removeSink(audioSink2);
        isAudioSinkAttached.set(false);

        assertEquals(0, audioFramesReceivedSink1.getCount());
        assertEquals(0, audioFramesReceivedSink2.getCount());
        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void cannotReceiveMultipleCallbacksFromSameSink() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();
        HashSet<AudioSinkFrame> frames = new HashSet<>();

        final AudioSink duplicateSink =
                (audioSample, encoding, sampleRate, channels) -> {
                    assertTrue(frames.add(new AudioSinkFrame(audioSample.asReadOnlyBuffer())));
                    duplicateFramesReceivedLatch.countDown();
                };

        bobLocalAudioTrack.addSink(duplicateSink);
        bobLocalAudioTrack.addSink(duplicateSink);

        assertTrue(duplicateFramesReceivedLatch.await(10, TimeUnit.SECONDS));
        bobLocalAudioTrack.removeSink(duplicateSink);
    }

    @Test
    public void canMuteTrackWithSink() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();
        AudioManager audioManager =
                (AudioManager) InstrumentationRegistry.getContext().getSystemService(AUDIO_SERVICE);

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));
        audioManager.setMicrophoneMute(true);
        bobLocalAudioTrack.enable(false);

        bobLocalAudioTrack.removeSink(audioSink1);
        isAudioSinkAttached.set(false);
    }

    @Test
    public void removingAudioSinkIsIdempotent() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));

        bobLocalAudioTrack.removeSink(audioSink1);
        bobLocalAudioTrack.removeSink(audioSink1);
        isAudioSinkAttached.set(false);
        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void sinkMapCountShouldBeZeroIfAllSinksAreRemoved() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        List<AudioSink> sinks = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_SINKS; i++) {
            final int id = i;
            AudioSink audioSink =
                    (audioSample, encoding, sampleRate, channels) -> {
                        Log.d("SINK", String.valueOf(id));
                    };
            bobLocalAudioTrack.addSink(audioSink);
            sinks.add(audioSink);
        }

        assertEquals(NUMBER_OF_SINKS, bobLocalAudioTrack.audioSinks.size());

        for (AudioSink sink : sinks) {
            bobLocalAudioTrack.removeSink(sink);
        }
        assertEquals(0, bobLocalAudioTrack.audioSinks.size());

        isAudioSinkAttached.set(false);
        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void sinkMapCountShouldBeZeroIfAllDuplicateSinksAreRemoved()
            throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);
        bobLocalAudioTrack.addSink(audioSink1);
        assertEquals(1, bobLocalAudioTrack.audioSinks.size());

        bobLocalAudioTrack.removeSink(audioSink1);

        assertEquals(0, bobLocalAudioTrack.audioSinks.size());
        isAudioSinkAttached.set(false);

        assertFalse(invalidAudioFramesReceived.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void audioSinkProxyIsTheSameWhenCallingAddSinkWithSameAudioSink()
            throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();

        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);

        AudioSinkProxy proxy1 = bobLocalAudioTrack.audioSinks.get(audioSink1);
        bobLocalAudioTrack.addSink(audioSink1);
        AudioSinkProxy proxy2 = bobLocalAudioTrack.audioSinks.get(audioSink1);
        assertEquals(proxy1, proxy2);

        bobLocalAudioTrack.removeSink(audioSink1);
        assertEquals(0, bobLocalAudioTrack.audioSinks.size());
    }

    @Test
    public void canUseAudioSinkAfterReleasingAndCreatingAudioTrack() throws InterruptedException {
        super.baseSetup(topology);
        publishAudioTrack();
        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));
        bobLocalAudioTrack.removeSink(audioSink1);
        isAudioSinkAttached.set(false);

        unpublishAudioTrack();
        bobLocalAudioTrack.release();
        audioFramesReceivedSink1 = new CountDownLatch(COUNTDOWN_COUNT);
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true, bobAudioTrackName);
        isAudioSinkAttached.set(true);
        bobLocalAudioTrack.addSink(audioSink1);
        publishAudioTrack();

        assertTrue(audioFramesReceivedSink1.await(10, TimeUnit.SECONDS));
        bobLocalAudioTrack.removeSink(audioSink1);
        isAudioSinkAttached.set(false);
    }

    @Test
    public void audioSinkFramesCanBeWrittenToFileAndPlayedBack()
            throws InterruptedException, IOException {
        super.baseSetup(topology);
        publishAudioTrack();

        assertTrue(bobRoom.getRemoteParticipants().size() > 0);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        isAudioSinkAttached.set(true);
        final String fileName = "/audio_sink.wav";
        final String fullPath = mediaTestActivity.getFilesDir().getPath() + fileName;
        final File outputFile = new File(fullPath);
        if (outputFile.exists()) {
            assertTrue(outputFile.delete());
        }
        assertTrue(outputFile.createNewFile());
        final FileOutputStream output = new FileOutputStream(outputFile, true);

        AudioSink fileProxy =
                (audioSample, encoding, sampleRate, channels) -> {
                    Log.v("AudioSinkTest", Arrays.toString(audioSample.array()));
                    try {
                        if (!didWriteWavHeader) {
                            writeWavHeader(output, getChannelMask(channels), sampleRate, encoding);
                            didWriteWavHeader = true;
                        }
                        output.write(audioSample.array());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        bobLocalAudioTrack.addSink(fileProxy);
        Thread.sleep(5000);
        bobLocalAudioTrack.removeSink(fileProxy);

        try {
            output.close();
            assertTrue(outputFile.setReadable(true));
            updateWavHeader(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(fullPath);
        player.prepare();
        player.start();

        assertTrue(player.isPlaying());
        boolean didGetSound = false;

        while (player.isPlaying()) {
            int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volumeLevel > 0) {
                didGetSound = true;
                player.stop();
            }
        }
        assertTrue(didGetSound);
        player.release();
    }

    /*
     * The following wav header helper functions are from this gist: https://gist.github.com/kmark/d8b1b01fb0d2febf5770
     */

    /**
     * Writes the proper 44-byte RIFF/WAVE header to/for the given stream Two size fields are left
     * empty/null since we do not yet know the final stream size
     *
     * @param out The stream to write the header to
     * @param channelMask An AudioFormat.CHANNEL_* mask
     * @param sampleRate The sample rate in hertz
     * @param encoding An AudioFormat.ENCODING_PCM_* value
     * @throws IOException
     */
    private static void writeWavHeader(
            OutputStream out, int channelMask, int sampleRate, int encoding) throws IOException {
        short channels;
        switch (channelMask) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable channel mask");
        }

        short bitDepth;
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bitDepth = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitDepth = 16;
                break;
            case AudioFormat.ENCODING_PCM_FLOAT:
                bitDepth = 32;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable encoding");
        }

        writeWavHeader(out, channels, sampleRate, bitDepth);
    }

    /**
     * Writes the proper 44-byte RIFF/WAVE header to/for the given stream Two size fields are left
     * empty/null since we do not yet know the final stream size
     *
     * @param out The stream to write the header to
     * @param channels The number of channels
     * @param sampleRate The sample rate in hertz
     * @param bitDepth The bit depth
     * @throws IOException
     */
    private static void writeWavHeader(
            OutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the
        // spec
        byte[] littleBytes =
                ByteBuffer.allocate(14)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putShort(channels)
                        .putInt(sampleRate)
                        .putInt(sampleRate * channels * (bitDepth / 8))
                        .putShort((short) (channels * (bitDepth / 8)))
                        .putShort(bitDepth)
                        .array();

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(
                new byte[] {
                    // RIFF header
                    'R',
                    'I',
                    'F',
                    'F', // ChunkID
                    0,
                    0,
                    0,
                    0, // ChunkSize (must be updated later)
                    'W',
                    'A',
                    'V',
                    'E', // Format
                    // fmt subchunk
                    'f',
                    'm',
                    't',
                    ' ', // Subchunk1ID
                    16,
                    0,
                    0,
                    0, // Subchunk1Size
                    1,
                    0, // AudioFormat
                    littleBytes[0],
                    littleBytes[1], // NumChannels
                    littleBytes[2],
                    littleBytes[3],
                    littleBytes[4],
                    littleBytes[5], // SampleRate
                    littleBytes[6],
                    littleBytes[7],
                    littleBytes[8],
                    littleBytes[9], // ByteRate
                    littleBytes[10],
                    littleBytes[11], // BlockAlign
                    littleBytes[12],
                    littleBytes[13], // BitsPerSample
                    // data subchunk
                    'd',
                    'a',
                    't',
                    'a', // Subchunk2ID
                    0,
                    0,
                    0,
                    0, // Subchunk2Size (must be updated later)
                });
    }

    /**
     * Updates the given wav file's header to include the final chunk sizes
     *
     * @param wav The wav file to update
     * @throws IOException
     */
    private static void updateWavHeader(File wav) throws IOException {
        byte[] sizes =
                ByteBuffer.allocate(8)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        // There are probably a bunch of different/better ways to calculate
                        // these two given your circumstances. Cast should be safe since if the WAV
                        // is
                        // > 4 GB we've already made a terrible mistake.
                        .putInt((int) (wav.length() - 8)) // ChunkSize
                        .putInt((int) (wav.length() - 44)) // Subchunk2Size
                        .array();

        RandomAccessFile accessWave = null;
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // Subchunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } catch (IOException ex) {
            // Rethrow but we still close accessWave in our finally
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }

    private int getChannelMask(int channels) {
        switch (channels) {
            case 1:
                return AudioFormat.CHANNEL_IN_MONO;
            case 2:
                return AudioFormat.CHANNEL_IN_STEREO;
        }
        return AudioFormat.CHANNEL_IN_STEREO;
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build();
            AudioFocusRequest focusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(false)
                            .setOnAudioFocusChangeListener(i -> {})
                            .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(
                    null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    private void setAudioFocus() {
        audioManager.setMicrophoneMute(false);
        audioManager.setSpeakerphoneOn(false);
        // Request audio focus before making any device switch.
        requestAudioFocus();
        /*
         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
         * required to be in this mode when playout and/or recording starts for
         * best possible VoIP performance.
         * Some devices have difficulties with speaker mode if this is not set.
         */
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    class AudioSinkFrame {

        private ByteBuffer frame;

        AudioSinkFrame(ByteBuffer frame) {
            this.frame = frame;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof AudioSinkFrame)) {
                return false;
            }
            return frame == ((AudioSinkFrame) obj).frame;
        }
    }
}
