package com.cagneymoreau.teletest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Needs to work with telegram api and google play
 *
 * Logic for when to block use, annoy, allow free or paid use
 *
 * Holds methods for sending users to payment options and recognizing payments
 *
 */

public class Paywall {

    private static final String TAG = "Paywall";

    private static final String request = "This is why we cant have nice things!!";




    Subscription subscription;
    private boolean playStore;

    public Paywall(MainActivity activity)
    {

        if (verifyInstallerId(activity.getApplicationContext())){
            //do google paywall shit
            playStore = true;
            subscription = new Subscription(activity);
        }else{
            //do side load paywall shit

        }

    }



    public void userRequestsSubscription()
    {
        if (playStore){
            subscription.beginPlayStorePrompts();
        }else {
            beginTelegramPurchaseFlow();
        }

    }


    public void displayAnnoyingPopUp(Fragment f)
    {
        new Purchase_Dialog(this, request).show(f.getChildFragmentManager(), "purchase dialog");
    }



    boolean verifyInstallerId(Context context) {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }



    private void beginTelegramPurchaseFlow()
    {
        // TODO: 1/27/2022 interact with a telegrambot

        //open simplechat? to bot

    }




    /**
     * Pop Up to annoy user and get them to buy the app
     */
    public static class Purchase_Dialog extends DialogFragment {

        String text;
        Paywall p;

            public Purchase_Dialog(Paywall p, String text) {
            this.p = p;
            this.text = text;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View v;


            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setView(v = inflater.inflate(R.layout.instructions_dialog, null));
            TextView tv = v.findViewById(R.id.instruction_textView);
            tv.setText(text);

            builder.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    p.userRequestsSubscription();
                }
            });

            builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Paywall.Purchase_Dialog.this.getDialog().cancel();
                }
            });

            return builder.create();
        }

    }



}
