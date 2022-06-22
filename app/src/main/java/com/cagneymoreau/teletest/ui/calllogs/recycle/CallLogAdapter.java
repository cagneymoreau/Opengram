package com.cagneymoreau.teletest.ui.calllogs.recycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class CallLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {




    ArrayList<TdApi.User> userList;

    MainActivity mainActivity;


    public CallLogAdapter(MainActivity activity)
    {
        mainActivity = activity;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.contacts_card, parent, false);
        viewHolder = new CallLogViewHolder(v, mainActivity);

        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        CallLogViewHolder checkList_viewHolder = (CallLogViewHolder) holder;

        TdApi.User user = userList.get(position);

        checkList_viewHolder.setUser(user);

    }



    @Override
    public int getItemCount() {

        return userList.size();

    }

    public void setList(ArrayList<TdApi.User> currList)
    {
        userList = currList;
        notifyDataSetChanged();
    }


    public TdApi.User getUser(int i)
    {
            return userList.get(i);
    }


    public void setSearchReturn(ArrayList<TdApi.User> list)
    {
        userList = list;
        notifyDataSetChanged();
    }

    public void extendSearchResults(ArrayList<TdApi.User> list){

        userList.addAll(list);
        notifyDataSetChanged();

    }


}
