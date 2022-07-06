package com.cagneymoreau.teletest.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.cagneymoreau.teletest.MainActivity;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


/**
 * This class is used for short term data persistence across UI
 *
 *
 */

public class Controller {

    private static final String TAG = "Controller";

    public static Controller controller;

    MainActivity mActivity;


    int previousUpdate;

    String createNewGroupFlag;

    ArrayList<Object> dataBinding = new ArrayList<>();
    Bitmap bitmapBind;

    TdApi.Message messageToForward;

    ArrayList<TdApi.User> userList;

    long channIDHolder;


    //region --------------  object management

    public static synchronized Controller getInstance(MainActivity activity)
    {
        if (controller == null){
            controller = new Controller(activity);
        }
        return controller;
    }


    private Controller(MainActivity activity)
    {
        mActivity = activity;
        previousUpdate = ((int) System.currentTimeMillis()/1000) - (90*24*60*60);
    }




    public void setDataBindingItem(Object o)
    {
        dataBinding.add(o);
    }

    public ArrayList<Object> getDataBinding()
    {
        ArrayList<Object> out = dataBinding;
        dataBinding = new ArrayList<>();
        return out;
    }

    public void setBitmapBind(Bitmap b)
    {
        bitmapBind = b;
    }

    public Bitmap getbitmapBind()
    {
        return bitmapBind;
    }


    public TdApi.Message getMessageToForward() {
        return messageToForward;
    }

    public void setMessageToForward(TdApi.Message messageTOForward) {
        this.messageToForward = messageTOForward;
    }

    public String getCreateNewGroupFlag() {
        return createNewGroupFlag;
    }

    public void setCreateNewGroupFlag(String createNewGroupFlag) {
        this.createNewGroupFlag = createNewGroupFlag;
    }

    public ArrayList<TdApi.User> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<TdApi.User> userList) {
        this.userList = userList;
    }


    public long getChannIDHolder() {
        return channIDHolder;
    }

    public void setChannIDHolder(long channIDHolder) {
        this.channIDHolder = channIDHolder;
    }
}
