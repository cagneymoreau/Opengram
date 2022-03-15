package com.cagneymoreau.teletest;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

class Outgoing {

    long chat;
    long thread;
    TdApi.MessageSendOptions options;
    TdApi.ReplyMarkup replyMarkup;
    TdApi.InputMessageContent content;
    Client.ResultHandler handler;

    public Outgoing(long chat, long thread, TdApi.MessageSendOptions options, TdApi.ReplyMarkup replyMarkup, TdApi.InputMessageContent content, Client.ResultHandler handler)
    {
        this.chat = chat;
        this.thread = thread;
        this.options = options;
        this.replyMarkup = replyMarkup;
        this.content = content;
        this.handler = handler;


    }





}
