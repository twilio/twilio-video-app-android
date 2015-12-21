package com.twilio.conversations.quickstart.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.twilio.conversations.quickstart.R;

public class Dialog {

    public static AlertDialog createInviteDialog(String caller, DialogInterface.OnClickListener acceptClickListener, DialogInterface.OnClickListener rejectClickListener, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
        alertDialogBuilder.setTitle("Incoming Call");
        alertDialogBuilder.setMessage(caller + " is calling");
        alertDialogBuilder.setPositiveButton("Accept", acceptClickListener);
        alertDialogBuilder.setNegativeButton("Reject", rejectClickListener);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    public static AlertDialog createCallParticipantsDialog(EditText participantEditText, DialogInterface.OnClickListener callParticipantsClickListener, DialogInterface.OnClickListener cancelClickListener, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
        alertDialogBuilder.setTitle("Call Participant");
        alertDialogBuilder.setPositiveButton("Call", callParticipantsClickListener);
        alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);
        alertDialogBuilder.setCancelable(false);

        setParticipantFieldInDialog(participantEditText, alertDialogBuilder, context);

        return alertDialogBuilder.create();
    }

    private static void setParticipantFieldInDialog(EditText participantEditText, AlertDialog.Builder alertDialogBuilder, Context context) {
        // Add a participant field to the dialog
        participantEditText.setHint("participant name");
        int horizontalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        int verticalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
        alertDialogBuilder.setView(participantEditText, horizontalPadding, verticalPadding, horizontalPadding, 0);
    }

}
