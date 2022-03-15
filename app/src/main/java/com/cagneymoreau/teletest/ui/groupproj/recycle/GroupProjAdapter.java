package com.cagneymoreau.teletest.ui.groupproj.recycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.Advertisement;

import java.util.ArrayList;

public class GroupProjAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    SortedList<Advertisement> forSaleSortedList;
    ArrayList<Advertisement> queryList;

    MainActivity mainActivity;

    public GroupProjAdapter(SortedList<Advertisement> forSaleSortedList, MainActivity mainActivity)
    {
        this.forSaleSortedList = forSaleSortedList;
        this.mainActivity = mainActivity;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.advert_card_layout, parent, false);
        viewHolder = new ProjViewHolder(v, mainActivity);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        Advertisement advertisement;

        if (queryList == null){
            advertisement = forSaleSortedList.get(position);
        }else{
            advertisement = queryList.get(position);
        }

        ProjViewHolder viewHolder = (ProjViewHolder) holder;

        viewHolder.setItemFound(advertisement);
    }



    @Override
    public int getItemCount() {

        if (queryList == null){
            return forSaleSortedList.size();
        }else{
            return queryList.size();
        }

    }


    public void setSearchReturn(ArrayList<Advertisement> list)
    {
        queryList = list;
        notifyDataSetChanged();
    }


}
