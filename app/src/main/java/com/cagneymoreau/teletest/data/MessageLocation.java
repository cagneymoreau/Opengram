package com.cagneymoreau.teletest.data;

/**
 * Thee objects store a message location
 * uses: index of the last message searched
 * storing the location of either a buyer or seller message
 */

public class MessageLocation {

    long chat_id;
    long lastMessageIndexedId;
    long old_chatId;




    public String toString()
    {
        return chat_id + "_" + lastMessageIndexedId;
    }


    public MessageLocation(String s)
    {
        String[] v = s.split("_");

        chat_id = Long.valueOf(v[0]);
        lastMessageIndexedId = Long.valueOf(v[1]);
    }


    public MessageLocation(long chat)
    {
        chat_id = chat;
        lastMessageIndexedId = -1;
    }

    public MessageLocation(long chat, long mess)
    {
        chat_id = chat;
        lastMessageIndexedId = mess;
    }

    public long getChatId()
    {
        return chat_id;
    }

    public long getLastMessageIndexedId()
    {
        return lastMessageIndexedId;
    }

}
