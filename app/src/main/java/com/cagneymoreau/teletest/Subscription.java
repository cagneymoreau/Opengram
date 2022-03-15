package com.cagneymoreau.teletest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

public class Subscription {


    private final static String TAG = "Subscription";

    private final static String key_firstLaunchKey = "firstLaunch";
    private final static String key_purchasedBoolean = "purchbool";
    private final static String key_lifetimeFreemium = "freeRide";
    private final static String key_rateThisApp = "ratingsGhost";

    private final static String sku_subscription = "annualfitlogpass";

    Activity activity;
    Context context;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    long milli;
    long trialPeriod = 30L*24L*60L*60L*1000L; //30 days
    boolean purchased = false;
    boolean trial = false;
    boolean freemium = false;

    BillingClient billingClient;
    List<SkuDetails> skuDetails;



    public Subscription(Activity a)
    {
        activity = a;
        context = a.getApplicationContext();

        keyValueFetch();

        init();




    }


    //record subscription status as key pair
    private void keyValueFetch()
    {
        sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        //trial period
        milli = sharedpreferences.getLong(key_firstLaunchKey, -1);
        //first time opening app
        if (milli == -1){
            milli =  System.currentTimeMillis();
            editor.putLong(key_firstLaunchKey,milli);
            editor.commit();
        }


        if ((milli + trialPeriod) > System.currentTimeMillis()){
            trial = true;
        }

        //purchased
        purchased = sharedpreferences.getBoolean(key_purchasedBoolean, false);
        freemium = sharedpreferences.getBoolean(key_lifetimeFreemium, false);

    }


    /**
     * initiate billing client to check subscription status and callbacks
     * listerner for new purchase
     */

    private void init()
    {

        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        setPurchased(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else {
                    // Handle any other error codes.
                }
            }
        };

        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();


        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    checkPurchase();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }


    /**
     * Here we startup and catch that the app has become inactive
     *
     */
    public void checkPurchase()
    {
        PurchasesResponseListener responseListener = new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK){
                    return;
                }

                if (list.size() == 0){

                    changeActivate(false);
                }
                for (Purchase purchase : list) {
                    setPurchased(purchase);
                }

            }
        };

        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, responseListener);
    }

    public void activateFreemium()
    {
        boolean newval = sharedpreferences.getBoolean(key_lifetimeFreemium, false);
        editor.putBoolean(key_lifetimeFreemium, !newval);
        editor.commit();
        freemium = !newval;
    }

    /**
     * called on startup or after attempted transaction
     * will activate but wont deactive expired subscription
     * @param purchase
     */
    private void setPurchased(Purchase purchase)
    {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                // who cares?
            }
        };

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }


            ArrayList<String> skus = purchase.getSkus();

            for (String sku: skus){
                if (sku.equals(sku_subscription)) {
                        changeActivate(true);
                }
            }

        }


    }

    private void changeActivate(boolean b)
    {
        purchased = b;
        editor.putBoolean(key_purchasedBoolean, purchased);
        editor.commit();
    }



    //paywall
    public boolean grantAccess()
    {
        //if (false){   //debugging purpses
        if (purchased || trial || freemium){

            return true;
        }


        List<String> skuList = new ArrayList<>();
        skuList.add(sku_subscription);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        skuDetails = skuDetailsList;
                    }
                });


        return false;
    }

    public void beginPlayStorePrompts()
    {
        beginPurchaseFlow();

    }



    private void beginPurchaseFlow()
    {
        if (skuDetails == null || skuDetails.size() == 0){
            FirebaseCrashlytics.getInstance().log("no skus!!");
            FirebaseCrashlytics.getInstance().recordException(new Exception());
            return;
        }

        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails.get(0))
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();

            // Handle the result.


    }



    //simple popup to begin purchase
    public static class Purchase_Dialog extends DialogFragment {

        String text;
        Subscription subscription;

        public Purchase_Dialog(Subscription s, String text) {

            subscription = s;
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
                   subscription.beginPurchaseFlow();
                }
            });

            builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Purchase_Dialog.this.getDialog().cancel();
                }
            });

            return builder.create();
        }

    }

    /**
     * Ask for rating at intervals
     * @return true if rating due
     */
    public boolean showRating()
    {

       long nextRate = sharedpreferences.getLong(key_rateThisApp, 0);

        if (nextRate == 0){

            nextRate = System.currentTimeMillis() + ((trialPeriod/4) * 3);
            editor.putLong(key_rateThisApp, nextRate);
            editor.commit();
            return false;
        }

        if (nextRate < System.currentTimeMillis()){

           nextRate = System.currentTimeMillis() + (trialPeriod/2);
           editor.putLong(key_rateThisApp, nextRate);
           editor.commit();
           return true;
       }
       return false;



    }

    /**
     * delay rating for 20 years
     */
    public void cancelRating() {
        long nextRate = System.currentTimeMillis() + (trialPeriod * 240);
        editor.putLong(key_rateThisApp, nextRate);
        editor.commit();

    }

}
