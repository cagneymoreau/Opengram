package com.cagneymoreau.teletest.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.R;

import org.drinkless.td.libcore.telegram.TdApi;


public class Delete_Dialog extends DialogFragment {

    String text;
    String option;
    DialogSender sender;
    String operation;
    int pos;
    Object object;
    int result = 0;

    public Delete_Dialog(String text,String option, DialogSender d,  Object obj, String operation, int position) {

        this.text = text;
        this.option = option;
        sender = d;
        this.operation = operation;
        pos = position;
        this.object = obj;
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

        CheckBox cb = v.findViewById(R.id.infoDialog_checkbox);
        if (option.isEmpty()){

            cb.setVisibility(View.INVISIBLE);

        }else{

            cb.setText(option);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b)result = 1;
                    else result = 0;
                }
            });
        }


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sender.setvalue(object, operation, pos, result);
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
