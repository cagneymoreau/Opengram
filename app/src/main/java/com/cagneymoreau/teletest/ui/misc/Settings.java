package com.cagneymoreau.teletest.ui.misc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.MarketController;

public class Settings extends Fragment {



    View fragmentView;

    MarketController marketController;

    //remake chan
    Button remakeChanButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.settings, container, false);

        marketController = MarketController.getInstance(((MainActivity) getActivity()));

        remakeChanButton = fragmentView.findViewById(R.id.settings_fixchannel_button);

        buildUI();

        return fragmentView;

    }


    private void buildUI()
    {
        remakeChanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marketController.buildMyChannel();
            }
        });
    }


}
