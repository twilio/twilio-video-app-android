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

import android.support.annotation.NonNull;

/**
 * Internet speech audio codec.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Internet_Speech_Audio_Codec">iSAC</a>
 */
public class IsacCodec extends AudioCodec {
    @NonNull public static final String NAME = "ISAC";

    public IsacCodec() {
        super(NAME);
    }
}
