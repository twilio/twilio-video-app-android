package com.twilio.video.app.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.twilio.video.app.R;
import com.twilio.video.app.adapter.IceServerAdapter;
import com.twilio.video.app.model.TwilioIceServer;
import com.twilio.video.app.util.IceOptionsHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IceServersDialogFragment extends AppCompatDialogFragment{

    public interface Listener {
        void onIceOptionsSelected(String iceTransportPolicy, List<TwilioIceServer> selectedServers);
        void onIceOptionsCancel();
    }

    private List<TwilioIceServer> iceServers;
    @BindView(R.id.ice_trans_policy_spinner) Spinner iceTransPolicySpinner;
    @BindView(R.id.ice_servers_list_view) ListView iceServersListView;
    private List<TwilioIceServer> selectedServers = new ArrayList<>();
    private SparseBooleanArray checkedItems;
    private Listener listener;
    private Unbinder unbinder;


    public void setIceServers(List<TwilioIceServer> iceServers) {
       this.iceServers = iceServers;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View iceOptionsView = inflater.inflate(R.layout.ice_options_layout, null);
        unbinder = ButterKnife.bind(this, iceOptionsView);

        IceServerAdapter iceServerAdapter =
                new IceServerAdapter(getActivity(), iceServers);
        iceServersListView.setAdapter(iceServerAdapter);


        ArrayAdapter<CharSequence> iceTransPolicyArrayAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.ice_trans_policy_array, android.R.layout.simple_spinner_item);
        iceTransPolicyArrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        iceTransPolicySpinner.setAdapter(iceTransPolicyArrayAdapter);
        setCheckedItems();

        builder.setView(iceOptionsView)
                .setTitle(R.string.ice_servers_dialog_title)
                .setPositiveButton(android.R.string.ok, dialogPositiveButtonClickListener())
                .setNegativeButton(android.R.string.cancel, dialogNegativeButtonClickListener());
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public List<TwilioIceServer> getSelectedServers() {
        return selectedServers;
    }

    private void setCheckedItems() {
        if (iceServersListView != null && checkedItems != null) {
            int len = iceServersListView.getCount();
            for (int i=0; i < len; i++) {
                iceServersListView.setItemChecked(i, checkedItems.get(i));
            }
        }
    }


    private DialogInterface.OnClickListener dialogPositiveButtonClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedServers =
                        IceOptionsHelper.getSelectedServersFromListView(iceServersListView);
                checkedItems = iceServersListView.getCheckedItemPositions();
                if (listener != null) {
                    String transportPolicy = "";
                    if (iceTransPolicySpinner != null) {
                        transportPolicy =
                                iceTransPolicySpinner.getSelectedItem().toString().toLowerCase();
                    }
                    listener.onIceOptionsSelected(
                            transportPolicy, new ArrayList<TwilioIceServer>(selectedServers));
                }
            }
        };
    }

    private DialogInterface.OnClickListener dialogNegativeButtonClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onIceOptionsCancel();
                }
            }
        };
    }


}
