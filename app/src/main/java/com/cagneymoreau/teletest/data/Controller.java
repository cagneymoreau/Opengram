package com.cagneymoreau.teletest.data;

import android.content.Context;
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
 * Interacts with api to controll advertisment objects representing our and other postings
 * This class gets/sets posts from/to the various chats
 * data persistence through simplesave
 *
 * // FIXME: 2/17/2022 adding new channel needs single search
 * // FIXME: 2/17/2022 remove channels that arent a market
 */

public class Controller {

    private static final String TAG = "MarketController";

    public static Controller controller;



    MainActivity mActivity;
    Context context;



    //which channels i have selected to be a market


    long myChannelId;

    //searching for advert
    Client.ResultHandler messageRecieiver;
    ArrayList<TdApi.Message> messageList = new ArrayList<>();
    long offsetChatId = 0;
    long offsetMessId = 0;
    int previousUpdate;
    int maxDate;
    //prevent running the gather algo simultaneously. if new channel is added then it will be added to end and performed anyways
    boolean gathering = false;


    int countToPost = 0;


    //for posting an advert

    ArrayList<Pair<String, Integer>> attempted;
    ArrayList<TdApi.Message> returned;
   int dateFortimeout;

    private int cleanCounter = 0;


    ArrayList<TdApi.User> userHolder;


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
        this.context = activity.getApplicationContext();



        previousUpdate = ((int) System.currentTimeMillis()/1000) - (90*24*60*60);
        int maxupdate = 0;






        //When app start you must wait on callbacks to verify if adds still exist
        //this value tracks how many callbacks are waiting so you can call cleanup once they have all returned
        cleanCounter += 1000;

        cleanCounter -= 1000;




    }

    //when an alert comes in live or a log in happens and a group changed we need to make sure that the new supergroup id is added as well
    public void checkForUpgrade(long currentBasic, long basicNew)
    {
    }

    public void onPause()
    {

    }


    public void setUsers(ArrayList<TdApi.User> users)
    {
        this.userHolder = users;
    }

    public ArrayList<TdApi.User> getUsers()
    {
        return userHolder;
    }

    //endregion


    //region ---------------- new messagehandling

    /**
     * Method determines if this message is from this app and need processing
     */
    public void unkownLastMessageSorter(TdApi.Message message)
    {

    }






    //endregion







}
