package com.cagneymoreau.teletest.ui.misc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cagneymoreau.teletest.R;

/**
 * A loading fragment  for startup and maybe app updates
 */


public class LoadFragment extends Fragment {


    View fragment ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragment = inflater.inflate(R.layout.load_fragment, container, false);

        ImageView im = fragment.findViewById(R.id.loading_imageview);
        im.setImageResource(R.drawable.telemarket_main_load);

        return fragment;
    }
}
