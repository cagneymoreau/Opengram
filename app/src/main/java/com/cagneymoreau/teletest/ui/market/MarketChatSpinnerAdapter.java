package com.cagneymoreau.teletest.ui.market;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.MessageLocation;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class MarketChatSpinnerAdapter extends ArrayAdapter<MessageLocation> {

    ArrayList<MessageLocation> mList;
    MainActivity mainActivity;

    public MarketChatSpinnerAdapter(MainActivity mainActivity, ArrayList<MessageLocation> chats)
    {
        super(mainActivity.getApplicationContext(), 0, chats);
        this.mainActivity = mainActivity;
        mList = chats;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    private View initView(int position, View convertView, ViewGroup parent){


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.market_spinner_item, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.market_spinner_item_textview);
        ImageView imageView = convertView.findViewById(R.id.market_spinner_item_imageview);
        imageView.getLayoutParams().width = 64;
        imageView.getLayoutParams().height = 64;

        if (mList.get(position).getChatId() == 0){

            String t = "All";
            textViewName.setText(t);

            Bitmap b = Utilities.getInitials(t, mainActivity);
            imageView.setImageBitmap(b);
            return convertView;

        }

        TdApi.Chat chat = mainActivity.getSpecificChat(mList.get(position).getChatId());

        String title = chat.title;
        textViewName.setText(title);

        Utilities.setChatAvater(chat, imageView, mainActivity );


        return convertView;

    }

}
