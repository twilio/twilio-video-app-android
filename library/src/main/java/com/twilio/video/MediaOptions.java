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

import android.support.annotation.VisibleForTesting;

/*
 * Provides options for creating a test MediaFactory instance. Useful for simulating media scenarios
 * on a device.
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class MediaOptions {
  /*
   * Read from native media factory layer.
   */
  @SuppressWarnings("unused")
  private final boolean enableH264;

  private MediaOptions(Builder builder) {
    this.enableH264 = builder.enableH264;
  }

  static class Builder {
    private boolean enableH264;

    Builder enableH264(boolean enableH264) {
      this.enableH264 = enableH264;
      return this;
    }

    MediaOptions build() {
      return new MediaOptions(this);
    }
  }
}
