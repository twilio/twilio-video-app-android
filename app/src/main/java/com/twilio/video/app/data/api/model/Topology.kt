/*
 * Copyright (C) 2020 Twilio, Inc.
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
package com.twilio.video.app.data.api.model

import com.google.gson.annotations.SerializedName

private const val GROUP_ROOM_NAME = "group"
private const val GROUP_SMALL_ROOM_NAME = "group-small"
private const val PEER_TO_PEER_ROOM_NAME = "peer-to-peer"
private const val GO_ROOM_NAME = "go"

enum class Topology(val value: String) {
    @SerializedName(GROUP_ROOM_NAME) GROUP(GROUP_ROOM_NAME),
    @SerializedName(GROUP_SMALL_ROOM_NAME) GROUP_SMALL(GROUP_SMALL_ROOM_NAME),
    @SerializedName(PEER_TO_PEER_ROOM_NAME) PEER_TO_PEER(PEER_TO_PEER_ROOM_NAME),
    @SerializedName(GO_ROOM_NAME) GO(GO_ROOM_NAME);

    override fun toString() = value
}
