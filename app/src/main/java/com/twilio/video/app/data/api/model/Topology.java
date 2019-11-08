/*
 * Copyright (C) 2019 Twilio, Inc.
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

public enum Topology {
    GROUP("group"),
    GROUP_SMALL("group-small"),
    P2P("peer-to-peer");

    private final String topology;

    Topology(String topolgy) {
        this.topology = topolgy;
    }

    public String getString() {
        return topology;
    }

    public static Topology fromString(String topology) {
        if (topology.equals(P2P.topology)) {
            return P2P;
        } else if (topology.equals(GROUP.topology)) {
            return GROUP;
        } else if (topology.equals(GROUP_SMALL.topology)) {
            return GROUP_SMALL;
        } else {
            throw new RuntimeException("Unsupported topology string -> " + topology);
        }
    }
}
