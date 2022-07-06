package com.cagneymoreau.teletest.data

import com.cagneymoreau.teletest.MainActivity
import org.drinkless.td.libcore.telegram.TdApi

/**
 * This class acts as an api to control users data within a telegram channel
 *
 * create a private channel to save data
 *
 * save and retrieve data
 *
 *
 *
 *
 */

class RemoteData (mainActivity: MainActivity) {

    var telegramController: TelegramController
    lateinit var dataChannel: TdApi.Supergroup

    init {

        telegramController = TelegramController.getInstance(mainActivity)

        findChannel()

    }

    private fun findChannel()
    {
        //if channel found set datachannel

        //else call make channel
    }


    //create channel
    private fun createChannel()
    {

    }

    //open textfile
    fun openTextFile(): String
    {

    }

    //replace textfile
    private fun replaceTextFile(title: String, data:String): Boolean
    {

    }

    //save new textfile
    fun saveTextFile(title: String, data: String): Boolean
    {
       //if exists replace



    }




}