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

package com.twilio.video.app.util;

import android.util.SparseBooleanArray;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.twilio.video.IceServer;
import com.twilio.video.IceTransportPolicy;
import com.twilio.video.app.model.TwilioIceServer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IceOptionsHelper {

    public static List<TwilioIceServer> getSelectedServersFromListView(
            ListView iceServersListView) {
        List<TwilioIceServer> selectedServers = new ArrayList<>();
        if (iceServersListView != null) {
            int len = iceServersListView.getCount();
            SparseBooleanArray checkedItems = iceServersListView.getCheckedItemPositions();
            for (int i = 0; i < len; i++) {
                if (checkedItems.get(i)) {
                    selectedServers.add((TwilioIceServer) iceServersListView.getItemAtPosition(i));
                }
            }
        }
        return selectedServers;
    }
}
