package com.cagneymoreau.teletest.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.R;

import org.drinkless.td.libcore.telegram.TdApi;


public class Delete_Dialog extends DialogFragment {

    String text;
    DialogSender sender;
    int operation;
    int pos;

    public Delete_Dialog(String text, DialogSender d, int operation, int position) {

        this.text = text;

        sender = d;
        this.operation = operation;
        pos = position;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v;


        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v = inflater.inflate(R.layout.info_dialog, null));
        TextView tv = v.findViewById(R.id.infoDialog_TV);
        tv.setText(text);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sender.setvalue(operation, null, pos);
                Delete_Dialog.this.getDialog().cancel();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Delete_Dialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

}
