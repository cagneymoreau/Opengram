package com.cagneymoreau.teletest;

import org.drinkless.td.libcore.telegram.TdApi;

public interface MessageListCallback {



    void messageAction(TdApi.Message message, String action);


}
