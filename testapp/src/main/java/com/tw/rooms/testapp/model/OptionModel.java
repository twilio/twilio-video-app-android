package com.tw.rooms.testapp.model;

import android.os.Bundle;

import com.tw.rooms.testapp.util.IceOptionsHelper;
import com.tw.rooms.testapp.util.SimpleSignalingUtils;
import com.twilio.rooms.IceOptions;
import com.twilio.rooms.IceServer;
import com.twilio.rooms.IceTransportPolicy;
import com.twilio.rooms.internal.ClientOptionsInternal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class OptionModel {

    public static final String OPTION_PREFER_H264_KEY = "enable-h264";
    private static final String OPTION_DEV_REGISTRAR = "endpoint.dev.twilio.com";
    private static final String OPTION_REGISTRAR_KEY = "registrar";
    private static final String OPTION_STATS_KEY = "stats-server-url";
    private static final String OPTION_DEV_STATS_URL = "https://eventgw.dev.twilio.com";
    private static final String OPTION_STAGE_REGISTRAR = "endpoint.stage.twilio.com";
    private static final String OPTION_STAGE_STATS_URL = "https://eventgw.stage.twilio.com";

    private Map<String, String> options = new HashMap<>();
    private String realm;
    private boolean preferH264;
    private String selectedTwilioIceServersJson;
    private String iceTransportPolicy;
    private String twilioIceServersJson;

    public interface Listener {

        void onIceServers(List<TwilioIceServer> twilioIceServers);
    }

    public OptionModel(Bundle bundle) {
        restoreState(bundle);
    }

    public void saveState(Bundle bundle) {
        bundle.putString(TwilioIceResponse.ICE_SELECTED_SERVERS, selectedTwilioIceServersJson);
        bundle.putString(TwilioIceResponse.ICE_TRANSPORT_POLICY, iceTransportPolicy);
        bundle.putString(TwilioIceResponse.ICE_SERVERS, twilioIceServersJson);
    }

    public ClientOptionsInternal createClientOptionsInternal() {
        IceOptions iceOptions = createIceOptions();
        return new ClientOptionsInternal(createIceOptions(), createPrivateOptions(realm));
    }

    public boolean isPreferH264() {
        return preferH264;
    }

    public void setPreferH264(boolean preferH264) {
        this.preferH264 = preferH264;
    }

    public void getTwilioIceServerList(final Listener listener) {
        final List<TwilioIceServer> twilioIceServers =
                IceOptionsHelper.convertToTwilioIceServerList(twilioIceServersJson);
        if (twilioIceServers.size() > 0) {
            listener.onIceServers(twilioIceServers);
        } else {
            // We are going to obtain list of servers anyway
            SimpleSignalingUtils.getIceServers(realm, new Callback<TwilioIceResponse>() {
                @Override
                public void success(TwilioIceResponse twilioIceResponse, Response response) {
                    selectedTwilioIceServersJson =
                            IceOptionsHelper.convertToJson(twilioIceResponse.getIceServers());
                    listener.onIceServers(twilioIceResponse.getIceServers());
                }

                @Override
                public void failure(RetrofitError error) {
                    Timber.w(error.getMessage());
                    listener.onIceServers(twilioIceServers);
                }
            });
        }
    }

    public IceOptions createIceOptions(List<TwilioIceServer> selectedTwIceServers,
                                       IceTransportPolicy selectedPolicy) {
        if (selectedTwIceServers.size() > 0) {
            Set<IceServer> iceServers =
                    IceOptionsHelper.convertToIceServersSet(selectedTwIceServers);
            return new IceOptions(selectedPolicy, iceServers);
        }
        return new IceOptions(selectedPolicy);
    }

    private IceOptions createIceOptions() {
        //Transform twilio ice servers from json to Set<IceServer>
        List<TwilioIceServer> selectedIceServers =
                IceOptionsHelper.convertToTwilioIceServerList(selectedTwilioIceServersJson);
        Set<IceServer> iceServers = IceOptionsHelper.convertToIceServersSet(selectedIceServers);
        IceTransportPolicy transPolicy  =
                IceOptionsHelper.convertToIceTransportPolicy(iceTransportPolicy);
        return createIceOptions(selectedIceServers, transPolicy);
    }

    private void restoreState(Bundle bundle) {
        preferH264 = bundle.getBoolean(OPTION_PREFER_H264_KEY);
        realm = bundle.getString(SimpleSignalingUtils.REALM);
        selectedTwilioIceServersJson = bundle.getString(TwilioIceResponse.ICE_SELECTED_SERVERS);
        iceTransportPolicy = bundle.getString(TwilioIceResponse.ICE_TRANSPORT_POLICY);
        twilioIceServersJson = bundle.getString(TwilioIceResponse.ICE_SERVERS);
    }


    private Map<String, String> createPrivateOptions(String realm) {
        options.clear();
        if (realm.equalsIgnoreCase("dev")) {
            options.put(OPTION_REGISTRAR_KEY, OPTION_DEV_REGISTRAR);
            options.put(OPTION_STATS_KEY, OPTION_DEV_STATS_URL);
        } else if (realm.equalsIgnoreCase("stage")) {
            options.put(OPTION_REGISTRAR_KEY, OPTION_STAGE_REGISTRAR);
            options.put(OPTION_STATS_KEY, OPTION_STAGE_STATS_URL);
        }
        options.put(OPTION_PREFER_H264_KEY, preferH264 ? "true" : "false");
        return options;
    }



}
