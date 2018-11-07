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
package com.twilio.video.app.data.api.model;

import android.support.annotation.NonNull;

public class RoomProperties {
    @NonNull private String name;
    @NonNull private Topology topology;
    private boolean recordParticipantsOnConnect;

    RoomProperties(
            @NonNull final String name,
            @NonNull final Topology topology,
            final boolean recordParticipantsOnConnect) {
        this.name = name;
        this.topology = topology;
        this.recordParticipantsOnConnect = recordParticipantsOnConnect;
    }

    public Topology getTopology() {
        return topology;
    }

    public boolean isRecordParticipantsOnConnect() {
        return recordParticipantsOnConnect;
    }

    public String getName() {
        return name;
    }

    public static class Builder {
        private String name;
        private Topology topology;
        private boolean recordParticipantsOnConnect;

        public Builder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        public Builder setTopology(@NonNull Topology topology) {
            this.topology = topology;
            return this;
        }

        public Builder setRecordParticipantsOnConnect(boolean recordParticipantsOnConnect) {
            this.recordParticipantsOnConnect = recordParticipantsOnConnect;
            return this;
        }

        @NonNull
        public RoomProperties createRoomProperties() {
            return new RoomProperties(name, topology, recordParticipantsOnConnect);
        }
    }
}
