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

import com.cagneymoreau.teletest.BuildConfig;
import com.cagneymoreau.teletest.R;

/**
 *
 * App intent, privacy policy
 *
 */

public class About extends Fragment {



    View fragmentView;

    Button intentButton, privacyButton;

    TextView walloftextview;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.about_layout, container, false);

        intentButton = fragmentView.findViewById(R.id.about_intent_button);
        intentButton.setText("Intent");
        privacyButton = fragmentView.findViewById(R.id.about_privacy_button);
        privacyButton.setText("Privacy");

        walloftextview = fragmentView.findViewById(R.id.about_textwall_textview);
        walloftextview.setText(R.string.app_intent);

        intentButton.setOnClickListener(view -> {
            walloftextview.setText(R.string.app_intent);
        });

        privacyButton.setOnClickListener(view -> {
            walloftextview.setText(R.string.privacy_policy);
        });

        TextView tv = fragmentView.findViewById(R.id.about_version_text);
        String s =  "version " + BuildConfig.VERSION_NAME;
        tv.setText(s);

        return fragmentView;

    }







}
