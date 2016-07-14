package com.tw.rooms.testapp.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.tw.rooms.testapp.R;
import com.tw.rooms.testapp.adapter.IceServerAdapter;
import com.tw.rooms.testapp.model.OptionModel;
import com.tw.rooms.testapp.model.TwilioIceServer;
import com.tw.rooms.testapp.util.IceOptionsHelper;
import com.twilio.rooms.IceTransportPolicy;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class IceOptionsUI {

    private Context context;

    @BindView(R.id.ice_trans_policy_spinner) Spinner iceTransPolicySpinner;
    @BindView(R.id.ice_servers_list_view) ListView twilioIceServersListView;
    @BindView(R.id.ice_options_layout) RelativeLayout iceOptionsLayout;
    @BindView(R.id.enable_ice_checkbox) CheckBox enableIceCheckbox;

    private OptionModel optionModel;

    public IceOptionsUI(Activity activity, OptionModel optionModel) {
        this.context = activity;
        ButterKnife.bind(this, activity);
        iceOptionsLayout.setVisibility(View.GONE);
        enableIceCheckbox.setChecked(false);
        this.optionModel = optionModel;
        updateViews();
    }

    @OnCheckedChanged(R.id.enable_ice_checkbox)
    public void toggleIceOptions(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            iceOptionsLayout.setVisibility(View.VISIBLE);
        } else {
            iceOptionsLayout.setVisibility(View.GONE);
        }
    }

    public boolean isEnabled() {
        return enableIceCheckbox.isChecked();
    }

    public List<TwilioIceServer> getSelectedIceServers() {
        return IceOptionsHelper.getSelectedServersFromListView(twilioIceServersListView);
    }

    public IceTransportPolicy getSelectedTransportPolicy() {
        return IceOptionsHelper.convertToIceTransportPolicy(
                iceTransPolicySpinner.getSelectedItem().toString());
    }

    public void updateViews(){
        ArrayAdapter<CharSequence> iceTransPolicyArrayAdapter = ArrayAdapter.createFromResource(
                context, R.array.ice_trans_policy_array, android.R.layout.simple_spinner_item);
        iceTransPolicyArrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        iceTransPolicySpinner.setAdapter(iceTransPolicyArrayAdapter);
        optionModel.getTwilioIceServerList(new OptionModel.Listener() {
            @Override
            public void onIceServers(List<TwilioIceServer> twilioIceServers) {
                IceServerAdapter iceServerAdapter =
                        new IceServerAdapter(context, twilioIceServers);
                twilioIceServersListView.setAdapter(iceServerAdapter);
            }
        });
    }
}
