package com.cagneymoreau.teletest.ui.contacts.recycle;

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

class ContactsViewHolder extends RecyclerView.ViewHolder {

    View view;
    ImageView avatar_imgView;
    TextView username_tv, lastSeen_tv;
    MainActivity mainActivity;


    public ContactsViewHolder(@NonNull View itemView, MainActivity act) {
        super(itemView);

        mainActivity = act;


        view = itemView;

        avatar_imgView = view.findViewById(R.id.chatlist_card_avatar_imageview);
        avatar_imgView.getLayoutParams().height = 80;


        username_tv = view.findViewById(R.id.chatlist_card_chatTitle_textView);
        lastSeen_tv = view.findViewById(R.id.chatlist_card_preview_textview);


    }


    public void setUser(TdApi.User user)
    {

        Utilities.setUserAvater(user, avatar_imgView, mainActivity);

        if (user.firstName != null){
            username_tv.setText(user.firstName);
        }


    }




}
