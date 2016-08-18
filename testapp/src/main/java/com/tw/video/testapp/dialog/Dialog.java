package com.tw.video.testapp.dialog;

import com.tw.video.testapp.model.TwilioIceServer;

import java.util.List;

public class Dialog {
    public static IceServersDialogFragment createIceServersDialog(List<TwilioIceServer> iceServers,
                                                                  IceServersDialogFragment.Listener listener) {
        IceServersDialogFragment dialog = new IceServersDialogFragment();
        dialog.setIceServers(iceServers);
        dialog.setListener(listener);
        return dialog;
    }

}
