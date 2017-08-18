/*
 * Copyright (C) 2017 Twilio, inc.
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

/**
 * Definition of supported audio codecs.
 */
public enum AudioCodec {
    /**
     * Internet speech audio codec.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Internet_Speech_Audio_Codec">iSAC</a>
     */
    ISAC,

    /**
     * Lossy audio coding format.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Opus_(audio_format)">Opus</a>
     */
    OPUS,

    /**
     * ITU-T standard for audio companding.
     *
     * @see <a href="https://en.wikipedia.org/wiki/G.711">PCMA</a>
     */
    PCMA,

    /**
     * ITU-T standard for audio companding.
     *
     * @see <a href="https://en.wikipedia.org/wiki/G.711">PCMU</a>
     */
    PCMU,

    /**
     * ITU-T standard 7 kHz Wideband audio codec.
     *
     * @see <a href="https://en.wikipedia.org/wiki/G.722">G.722</a>
     */
    G722
}
