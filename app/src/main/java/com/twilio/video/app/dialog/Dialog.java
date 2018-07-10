/*
 * Copyright (C) 2017 Twilio, Inc.
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

package com.twilio.video.app.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.twilio.video.app.R;
import com.twilio.video.app.model.TwilioIceServer;

import java.util.List;

public class Dialog {
    public static IceServersDialogFragment createIceServersDialog(List<TwilioIceServer> iceServers,
                                                                  IceServersDialogFragment.Listener listener) {
        IceServersDialogFragment dialog = new IceServersDialogFragment();
        dialog.setIceServers(iceServers);
        dialog.setListener(listener);
        return dialog;
    }

    public static AlertDialog createConnectDialog(EditText roomEditText,
                                                  DialogInterface.OnClickListener connectClickListener,
                                                  DialogInterface.OnClickListener cancelClickListener,
                                                  Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
        alertDialogBuilder.setTitle("Connect to Room");
        alertDialogBuilder.setPositiveButton("Connect", connectClickListener);
        alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);
        alertDialogBuilder.setCancelable(false);

        setRoomtFieldInDialog(roomEditText, alertDialogBuilder, context);

        return alertDialogBuilder.create();
    }

    private static void setRoomtFieldInDialog(EditText roomEditText,
                                              AlertDialog.Builder alertDialogBuilder,
                                              Context context) {
        roomEditText.setHint("Room Name");

        int horizontalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        int verticalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
        roomEditText.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        alertDialogBuilder.setView(roomEditText);
    }

}
