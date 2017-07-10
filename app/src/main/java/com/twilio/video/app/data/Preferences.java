/*
 * Copyright (C) 2017 Twilio, Inc.
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

package com.twilio.video.app.data;

import com.twilio.video.app.BuildConfig;

public class Preferences {
    public static final String IDENTITY = "pref_identity";
    public static final String EMAIL = "pref_email";
    public static final String DISPLAY_NAME = "pref_display_name";
    public static final String ENVIRONMENT = "pref_environment";
    public static final String ENVIRONMENT_DEFAULT = BuildConfig.ENVIRONMENT_DEFAULT;
    public static final String TOPOLOGY = "pref_topology";
    public static final String TOPOLOGY_DEFAULT = BuildConfig.TOPOLOGY_DEFAULT;
    public static final String MIN_FPS = "pref_min_fps";
    public static final String MAX_FPS = "pref_max_fps";
    public static final String MIN_VIDEO_DIMENSIONS = "pref_min_video_dim";
    public static final String MAX_VIDEO_DIMENSIONS = "pref_max_video_dim";
    public static final String ASPECT_RATIO = "pref_aspect_ratio";
    public static final String VERSION = "pref_version";
    public static final String VIDEO_LIBRARY_VERSION = "pref_video_library_version";
    public static final String LOGOUT = "pref_logout";
    public static final String ENABLE_STATS = "pref_enable_stats";
    public static final String ENABLE_INSIGHTS = "pref_enable_insights";
    public static final boolean ENABLE_INSIGHTS_DEFAULT = true;
}