package com.cagneymoreau.teletest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.cagneymoreau.teletest.data.TelegramController;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Perform non playstore updates
 *
 * search channels messages to find most recent apk update
 *
 */

// FIXME: 2/10/2022 will back to back update show need for new download?

public class Update {

    private static final String TAG = "Update";

    private static final long channelID = -1001615064939L;

    private static final long messID = 2097152L;

    /**
     * Find message containing
     */
    public static void findUpdatedApk(TelegramController telegramController)
    {




        Client.ResultHandler  handler = new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {

                if (object.getConstructor() != TdApi.Message.CONSTRUCTOR){
                    Log.d(TAG,"onResult: not message " + object.toString());
                    return;
                }

                TdApi.Message message = (TdApi.Message) object;

                if (TdApi.MessageDocument.CONSTRUCTOR != message.content.getConstructor()){
                    Log.d(TAG, "onResult: not document ");
                    return;
                }

                TdApi.MessageDocument messDoc = (TdApi.MessageDocument) message.content;

                TdApi.FormattedText text = (TdApi.FormattedText) messDoc.caption;

                String val = text.text;

                String[] args = val.split(" ");

                if (!args[0].contains("APP_UPDATE"))
                {
                    Log.e(TAG, "onResult: APK DOWNLOAD DAMAGED", null);
                    return;
                }

                String[] split = args[0].split("_");

                int update = Integer.valueOf(split[3]);

                if (update > BuildConfig.VERSION_CODE){

                    TdApi.Document apk = (TdApi.Document) messDoc.document;
                    telegramController.newApkAvailable(apk, args);

                }
            }
        };

        telegramController.getMessage(
                channelID,
                messID,
                handler);
    }


    public static boolean isDownLoaded(TdApi.Document document, TelegramController telegramController)
    {
        TdApi.File file = document.document;

        if (!file.local.path.isEmpty()){

            File localApk = new File(file.local.path);

            String[] name = localApk.getName().split("\\.");

            if (name.length != 2 || !name[1].equals("apk")) return false;

            telegramController.setApkFile(localApk);

            return true;
        }
        return false;
    }




    public static void downloadApk(TdApi.Document document, TelegramController telegramController)
    {
        TdApi.File file = document.document;


            telegramController.getRemoteFile(file.remote.id, new TdApi.FileTypeUnknown(), new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (TdApi.File.CONSTRUCTOR != object.getConstructor()){
                        Log.e(TAG, "onResult:  apk remote locate failed at unique id", null);

                        return;
                    }

                    TdApi.File actualFile = (TdApi.File) object;
                    
                    telegramController.downloadFile(actualFile.id, 1, new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {
                            
                            if (TdApi.File.CONSTRUCTOR != object.getConstructor()){
                                Log.e(TAG, "onResult: failed apk download", null);

                                return;
                            }
                            
                            TdApi.File localFile = (TdApi.File) object;

                            File localApk = new File(localFile.local.path);

                            String[] name = localApk.getName().split("\\.");

                            if (name.length < 2 || !name[1].equals("apk")) return;

                            telegramController.setApkFile(localApk);


                        }
                    });


                }
            });



        }




    public static boolean openApkInstall(Activity activity, File f) {
        boolean exists = false;
        try {

            if (exists = f.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (Build.VERSION.SDK_INT >= 24) {
                   Uri uri =  FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", f);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
                }
                try {
                    activity.startActivityForResult(intent, 500);
                } catch (Exception e) {
                    Log.e(TAG, "openApkInstall: ", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "openApkInstall: ",e );
        }
        return exists;
    }




}
