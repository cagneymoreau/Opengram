package com.cagneymoreau.teletest.ui.posting.recycler;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.Image;

class AdvertViewHolder extends RecyclerView.ViewHolder {

    private final static String TAG = "AdvertViewHolder";

    View view;
    ImageView avatar_imgView;
    TextView title_Tv;

    MainActivity mainActivity;


    public AdvertViewHolder(@NonNull View itemView, MainActivity mainActivity) {
        super(itemView);

        this.mainActivity = mainActivity;

        view = itemView;
        avatar_imgView = view.findViewById(R.id.advert_card_imageview);
        avatar_imgView.getLayoutParams().height = 150;
        avatar_imgView.getLayoutParams().width = 150;

        title_Tv = view.findViewById(R.id.advert_card_textView);

    }


    public void setAdvert(Advertisement advert)
    {
        title_Tv.setText(advert.getTitle());

        Image im = advert.getImage(0);

            if (advert.getUser() == 1234L ){
                avatar_imgView.setColorFilter(null);
            }
            else if (advert.expired()) {
                avatar_imgView.setColorFilter(Utilities.GRAY_TINT, PorterDuff.Mode.MULTIPLY);
            }
            else if(advert.missing()){
                avatar_imgView.setColorFilter(Utilities.YELLOW_TINT, PorterDuff.Mode.MULTIPLY);
            }
            else if(advert.deleted()){
                avatar_imgView.setColorFilter(Utilities.RED_TINT, PorterDuff.Mode.MULTIPLY);            }
            else {
                avatar_imgView.setColorFilter(null);
            }


        if (im != null){
            im.displayImage(avatar_imgView, mainActivity);
        }else {
            Image.displayDefault(avatar_imgView, advert.getMycategory());
        }



    }





}
