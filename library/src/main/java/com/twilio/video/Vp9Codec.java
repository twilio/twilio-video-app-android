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
 * Traditional block-based transform coding format similar to MPEG's High Efficiency Video Coding
 * (HEVC/H.265).
 *
 * @see <a href="https://en.wikipedia.org/wiki/VP9">VP9</a>
 */
public class Vp9Codec extends VideoCodec {
  public static final String NAME = "VP9";

  public Vp9Codec() {
    super(NAME);
  }
}
