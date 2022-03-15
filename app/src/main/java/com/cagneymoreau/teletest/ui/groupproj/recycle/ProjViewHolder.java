package com.cagneymoreau.teletest.ui.groupproj.recycle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.data.Image;

class ProjViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "itemviewholder";

    View view;
    ImageView avatar_imgView;
    TextView title_Tv;

    MainActivity mainActivity;

    public ProjViewHolder(@NonNull View itemView, MainActivity mainActivity  ) {
        super(itemView);

        this.mainActivity = mainActivity;
        view = itemView;
        avatar_imgView = view.findViewById(R.id.advert_card_imageview);
        //avatar_imgView.setMaxHeight(100);

        title_Tv = view.findViewById(R.id.advert_card_textView);
    }


    public void setItemFound(Advertisement ad) {

        title_Tv.setText(ad.getTitle());
        avatar_imgView.getLayoutParams().height = 150;
        avatar_imgView.getLayoutParams().width = 150;

        Image im = ad.getImage(0);
        if (im != null){
            //im.displayImage(avatar_imgView, mainActivity);
            im.seekImageData(mainActivity, avatar_imgView);
        }



    }







}
