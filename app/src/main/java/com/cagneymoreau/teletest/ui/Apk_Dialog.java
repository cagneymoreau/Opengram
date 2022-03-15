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
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;

import org.drinkless.td.libcore.telegram.TdApi;


public class Apk_Dialog extends DialogFragment {



    public Apk_Dialog() {


    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {



        View v;


        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v = inflater.inflate(R.layout.info_dialog, null));
        TextView tv = v.findViewById(R.id.infoDialog_TV);
        tv.setText("do something");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Apk_Dialog.this.getDialog().cancel();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Apk_Dialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

}
