package com.cagneymoreau.teletest.ui.chatlist.recycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.ui.chatlist.ChatList;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    public SortedList<TdApi.Chat> sortedList;
    ArrayList<TdApi.Chat> queryList;

    MainActivity mainActivity;
    ChatList chatList;

    public ChatListAdapter( MainActivity activity, ChatList chatList)
    {
        mainActivity = activity;
        this.chatList = chatList;
        //sortedList = activity.getEasyChats();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.chatlist_card, parent, false);
        viewHolder = new ChatListViewHolder(v, mainActivity, chatList);

        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatListViewHolder checkList_viewHolder = (ChatListViewHolder) holder;

        TdApi.Chat chat;

        if (queryList == null){
            chat = sortedList.get(position);
        }else{
            chat = queryList.get(position);
        }


        checkList_viewHolder.setChat(chat);


    }



    @Override
    public int getItemCount() {

        if (queryList == null){
            return sortedList.size();
        }else{
            return queryList.size();
        }

    }

    public void setSortedList(SortedList<TdApi.Chat> currList)
    {
        sortedList = currList;
        notifyDataSetChanged();
    }


    public TdApi.Chat getChat(int i)
    {
        if (queryList == null){
            return sortedList.get(i);
        }else{
            return queryList.get(i);
        }
    }


    public void setSearchReturn(ArrayList<TdApi.Chat> list)
    {
        queryList = list;
        notifyDataSetChanged();
    }

    public void extendSearchResults(ArrayList<TdApi.Chat> list){

        queryList.addAll(list);
        notifyDataSetChanged();

    }



}
