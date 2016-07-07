package com.tw.conv.testapp.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.tw.conv.testapp.R;
import com.tw.conv.testapp.adapter.IceServerAdapter;
import com.tw.conv.testapp.model.TwilioIceResponse;
import com.tw.conv.testapp.model.TwilioIceServer;
import com.tw.conv.testapp.util.IceOptionsHelper;
import com.tw.conv.testapp.util.SimpleSignalingUtils;
import com.twilio.conversations.IceOptions;
import com.twilio.conversations.IceServer;
import com.twilio.conversations.IceTransportPolicy;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class IceOptionsController {

    private Activity activity;

    private String selectedTwilioIceServersJson;
    private String iceTransportPolicy;
    private String twilioIceServersJson;

    @BindView(R.id.ice_trans_policy_spinner) Spinner iceTransPolicySpinner;
    @BindView(R.id.ice_servers_list_view) ListView twilioIceServersListView;
    @BindView(R.id.ice_options_layout) RelativeLayout iceOptionsLayout;
    @BindView(R.id.enable_ice_checkbox) CheckBox enableIceCheckbox;

    public IceOptionsController(Activity activity) {
        this.activity = activity;
        ButterKnife.bind(this, activity);
        iceOptionsLayout.setVisibility(View.GONE);
        enableIceCheckbox.setChecked(false);
    }

    @OnCheckedChanged(R.id.enable_ice_checkbox)
    public void toggleIceOptions(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            iceOptionsLayout.setVisibility(View.VISIBLE);
        } else {
            iceOptionsLayout.setVisibility(View.GONE);
        }
    }

    public void restoreState(Bundle bundle) {
        selectedTwilioIceServersJson = bundle.getString(TwilioIceResponse.ICE_SELECTED_SERVERS);
        iceTransportPolicy = bundle.getString(TwilioIceResponse.ICE_TRANSPORT_POLICY);
        twilioIceServersJson = bundle.getString(TwilioIceResponse.ICE_SERVERS);
    }

    public void saveState(Bundle bundle) {
        bundle.putString(TwilioIceResponse.ICE_SELECTED_SERVERS, selectedTwilioIceServersJson);
        bundle.putString(TwilioIceResponse.ICE_TRANSPORT_POLICY, iceTransportPolicy);
        bundle.putString(TwilioIceResponse.ICE_SERVERS, twilioIceServersJson);
    }

    public IceOptions retrieveIceOptions() {
        //Transform twilio ice servers from json to Set<IceServer>
        List<TwilioIceServer> selectedIceServers =
                IceOptionsHelper.convertToTwilioIceServerList(selectedTwilioIceServersJson);
        Set<IceServer> iceServers = IceOptionsHelper.convertToIceServersSet(selectedIceServers);
        IceTransportPolicy transPolicy  = IceTransportPolicy.ICE_TRANSPORT_POLICY_ALL;

        if (iceTransportPolicy.equalsIgnoreCase("relay") ) {
            transPolicy = IceTransportPolicy.ICE_TRANSPORT_POLICY_RELAY;
        }
        if (iceServers.size() > 0) {
            return new IceOptions(transPolicy, iceServers);
        }

        return new IceOptions(transPolicy);
    }

    public IceOptions createIceOptions() {
        if (!enableIceCheckbox.isChecked()) {
            return null;
        }
        List<TwilioIceServer> selectedTwIceServers =
                IceOptionsHelper.getSelectedServersFromListView(twilioIceServersListView);
        IceTransportPolicy policy = IceOptionsHelper.convertToIceTransportPolicy(
                iceTransPolicySpinner.getSelectedItem().toString());
        if (selectedTwIceServers.size() > 0) {
            Set<IceServer> iceServers =
                    IceOptionsHelper.convertToIceServersSet(selectedTwIceServers);
            return new IceOptions(policy, iceServers);
        }
        return new IceOptions(policy);
    }

    public void setIceOptionsViews(String realm){
        ArrayAdapter<CharSequence> iceTransPolicyArrayAdapter = ArrayAdapter.createFromResource(
                activity, R.array.ice_trans_policy_array, android.R.layout.simple_spinner_item);
        iceTransPolicyArrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        iceTransPolicySpinner.setAdapter(iceTransPolicyArrayAdapter);
        List<TwilioIceServer> twilioIceServers =
                IceOptionsHelper.convertToTwilioIceServerList(twilioIceServersJson);

        if (twilioIceServers.size() > 0) {
            IceServerAdapter iceServerAdapter =
                    new IceServerAdapter(activity, twilioIceServers);
            twilioIceServersListView.setAdapter(iceServerAdapter);
        } else {
            // We are going to obtain list of servers anyway
            SimpleSignalingUtils.getIceServers(realm, new Callback<TwilioIceResponse>() {
                @Override
                public void success(TwilioIceResponse twilioIceResponse, Response response) {
                    IceServerAdapter iceServerAdapter =
                            new IceServerAdapter(activity,
                                    twilioIceResponse.getIceServers());
                    twilioIceServersListView.setAdapter(iceServerAdapter);
                }

                @Override
                public void failure(RetrofitError error) {
                    Timber.w(error.getMessage());
                }
            });
        }
    }
}
