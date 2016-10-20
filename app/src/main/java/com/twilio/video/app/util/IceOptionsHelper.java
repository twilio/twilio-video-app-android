package com.twilio.video.app.util;


import android.util.SparseBooleanArray;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.twilio.video.app.model.TwilioIceServer;
import com.twilio.video.IceServer;
import com.twilio.video.IceTransportPolicy;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IceOptionsHelper {

    private static Type listType = new TypeToken<List<TwilioIceServer>>() {}.getType();

    public static List<TwilioIceServer> convertToTwilioIceServerList(String json) {
        if (json == null || json.equals("")) {
            return new ArrayList<>();
        }
        Gson gson = new GsonBuilder().create();
        List<TwilioIceServer> result =
                gson.fromJson(json, listType);
        return result;
    }

    public static String convertToJson(List<TwilioIceServer> twilioIceServers) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(twilioIceServers, listType);
    }

    public static Set<IceServer> convertToIceServersSet(
            List<TwilioIceServer> twilioIceServers) {
        Set<IceServer> result = new HashSet<>();
        if (twilioIceServers != null && twilioIceServers.size() > 0) {
            for (TwilioIceServer twilioIceServer : twilioIceServers) {
                IceServer iceServer = new IceServer(
                        twilioIceServer.getUrl(),
                        twilioIceServer.getUsername(),
                        twilioIceServer.getCredential());
                result.add(iceServer);
            }
        }
        return result;
    }

    public static List<TwilioIceServer> getSelectedServersFromListView(ListView iceServersListView) {
        List<TwilioIceServer> selectedServers = new ArrayList<>();
        if (iceServersListView != null) {
            int len = iceServersListView.getCount();
            SparseBooleanArray checkedItems = iceServersListView.getCheckedItemPositions();
            for (int i=0; i<len; i++) {
                if (checkedItems.get(i)) {
                    selectedServers.add(
                            (TwilioIceServer)iceServersListView.getItemAtPosition(i));
                }
            }
        }
        return selectedServers;
    }

    public static IceTransportPolicy convertToIceTransportPolicy(String iceTransportStr) {
        if (iceTransportStr.equalsIgnoreCase("relay")) {
            return IceTransportPolicy.RELAY;
        }
        return IceTransportPolicy.ALL;
    }
}
