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

/**
 * Traditional block-based transform coding format similar to H264.
 *
 * @see <a href="https://en.wikipedia.org/wiki/VP8">VP8</a>
 */
public class Vp8Codec extends VideoCodec {
  public static final String NAME = "VP8";

  /**
   * Enabling simulcast causes the encoder to generate multiple spatial and temporal layers for the
   * video that is published. This feature should only be enabled in a Group Room.
   */
  public final boolean simulcast;

  public Vp8Codec() {
    this(false);
  }

  public Vp8Codec(boolean simulcast) {
    super(NAME);
    this.simulcast = simulcast;
  }
}
