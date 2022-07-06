package com.cagneymoreau.teletest.ui.misc;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;


import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

/**
 *
 * App intent, privacy policy
 *
 */

public class ImageFull extends Fragment {



    View fragmentView;

    ImageView imageView;

    Controller controller;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.image_full, container, false);
        controller = Controller.getInstance(((MainActivity) getActivity()));

        ArrayList<Object> list = controller.getDataBinding();

        TdApi.MessagePhoto mPhoto = (TdApi.MessagePhoto) list.get(0);

        imageView = fragmentView.findViewById(R.id.image_full_imageview);

        Utilities.getMessagePhoto(mPhoto, imageView, ((MainActivity)getActivity()));

        return fragmentView;

    }







}
