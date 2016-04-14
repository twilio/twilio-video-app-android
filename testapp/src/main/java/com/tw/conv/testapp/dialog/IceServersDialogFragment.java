package com.tw.conv.testapp.dialog;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.tw.conv.testapp.R;
import com.tw.conv.testapp.adapter.IceServerAdapter;
import com.tw.conv.testapp.provider.TwilioIceServer;
import com.tw.conv.testapp.provider.TwilioIceServers;
import com.twilio.conversations.IceTransportPolicy;

import java.util.ArrayList;
import java.util.List;

public class IceServersDialogFragment extends AppCompatDialogFragment{

    public interface IceServersDialogListener {
        void onIceOptionsSelected(String iceTransportPolicy, List<TwilioIceServer> selectedServers);
        void onIceOptionsCancel();
    }

    private List<TwilioIceServer> iceServers;
    private Spinner iceTransPolicySpinner;
    private ListView iceServersListView;
    private List<TwilioIceServer> selectedServers = new ArrayList<>();
    private SparseBooleanArray checkedItems;
    private IceServersDialogListener listener;

    public void setIceServers(List<TwilioIceServer> iceServers) {
       this.iceServers = iceServers;
    }

    public void setListener(IceServersDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View iceOptionsView = inflater.inflate(R.layout.ice_options_layout, null);

        iceServersListView =
                (ListView)iceOptionsView.findViewById(R.id.ice_servers_list_view);
        IceServerAdapter iceServerAdapter =
                new IceServerAdapter(getActivity(), iceServers);
        iceServersListView.setAdapter(iceServerAdapter);


        iceTransPolicySpinner = (Spinner)iceOptionsView.findViewById(R.id.ice_trans_policy_spinner);
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
                selectedServers = new ArrayList<>();
                if (iceServersListView != null) {
                    int len = iceServersListView.getCount();
                    checkedItems = iceServersListView.getCheckedItemPositions();
                    for (int i=0; i<len; i++) {
                        if (checkedItems.get(i)) {
                            selectedServers.add(
                                    (TwilioIceServer)iceServersListView.getItemAtPosition(i));
                        }
                    }
                }
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
