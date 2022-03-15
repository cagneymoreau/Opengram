package com.cagneymoreau.teletest.ui.posting.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.R;

public class AdvertisementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    SortedList<Advertisement> advertsList;
    MainActivity mainActivity;

    public AdvertisementAdapter(SortedList<Advertisement> advertsList, MainActivity mainActivity)
    {
        this.advertsList = advertsList;
        this.mainActivity = mainActivity;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.advert_card_layout, parent, false);
        viewHolder = new AdvertViewHolder(v, mainActivity);

        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        AdvertViewHolder viewHolder = (AdvertViewHolder) holder;

        if (position != 0) {
            viewHolder.setAdvert(advertsList.get(position -1));
        }else{
            viewHolder.setAdvert(Advertisement.dummyAdvert());
        }

    }



    @Override
    public int getItemCount() {

     return advertsList.size() + 1;

    }




}
