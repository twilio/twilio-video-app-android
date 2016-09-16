package com.tw.video.testapp.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.tw.video.testapp.R;
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

    public static AlertDialog createJoinRoomDialog(EditText roomEditText,
                                                   DialogInterface.OnClickListener joinRoomClickListener,
                                                   DialogInterface.OnClickListener cancelClickListener,
                                                   Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
        alertDialogBuilder.setTitle("Join Room");
        alertDialogBuilder.setPositiveButton("Join", joinRoomClickListener);
        alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);
        alertDialogBuilder.setCancelable(false);

        setRoomtFieldInDialog(roomEditText, alertDialogBuilder, context);

        return alertDialogBuilder.create();
    }

    private static void setRoomtFieldInDialog(EditText roomEditText,
                                              AlertDialog.Builder alertDialogBuilder,
                                              Context context) {
        // Add a room field to the dialog
        roomEditText.setHint("Room Name");
        int horizontalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        int verticalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
        alertDialogBuilder.setView(roomEditText, horizontalPadding, verticalPadding,
                horizontalPadding, 0);
    }

}
