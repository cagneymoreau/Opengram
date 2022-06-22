package com.cagneymoreau.teletest.ui.chatlist.recycle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.ui.chatlist.ChatList;

import org.drinkless.td.libcore.telegram.TdApi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

class ChatListViewHolder extends RecyclerView.ViewHolder {

    View view;
    ImageView avatar_imgView, checkImageView;
    TextView title_Tv, preview_Tv, time_Tv, unread_TV;

    long recentCall = 0;

    MainActivity mainActivity;
    ChatList chatList;

    public ChatListViewHolder(@NonNull View itemView, MainActivity act, ChatList chatList) {
        super(itemView);

        mainActivity = act;
        this.chatList = chatList;

        view = itemView;

        avatar_imgView = view.findViewById(R.id.chatlist_card_avatar_imageview);
        avatar_imgView.getLayoutParams().height = 80;

        checkImageView = view.findViewById(R.id.chatlist_card_checkmark_imageview);

        title_Tv = view.findViewById(R.id.chatlist_card_chatTitle_textView);
        preview_Tv = view.findViewById(R.id.chatlist_card_preview_textview);
        time_Tv = view.findViewById(R.id.chatlist_card_time_textview);
        unread_TV = view.findViewById(R.id.chatlist_card_unread_textview);

    }


    public void setChat(TdApi.Chat chat)
    {

        Utilities.setChatAvater(chat, avatar_imgView, mainActivity);

        if (chat.title != null){
            title_Tv.setText(chat.title);
        }

        if (chat.lastMessage != null){

            long time = 1000L * chat.lastMessage.date;
            LocalDateTime last = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();

            String d = "";

            LocalDate today =  Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime midnight = LocalTime.MIDNIGHT;
            LocalDateTime bench = LocalDateTime.of(today, midnight);

            if (last.isBefore(bench)){
                d = last.getDayOfWeek().name();
                d = d.substring(0,3);
            }else{

                int hour = last.getHour();
                if ( hour > 12) hour = hour - 12;

                d = hour + ":" + last.getMinute();
            }

            time_Tv.setText(d);
        }

        String unread = String.valueOf(chat.unreadCount);
        unread_TV.setText(unread);

        if (chat.lastMessage == null){
                /*
            Client.ResultHandler h = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    postPreview(chat.lastMessage);
                }
            };

                 */

        }else{
            postPreview(chat.lastMessage);
        }




    }

    private static int previewLength = 30;

    private void postPreview(TdApi.Message message)
    {
        if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR)
        {
            TdApi.MessageText mess = (TdApi.MessageText) message.content;

            String text = mess.text.text;

            if (text.length() > previewLength) text = text.substring(0, previewLength);
            preview_Tv.setText(text);


        }else{
            preview_Tv.setText("media message");
        }
    }

}
