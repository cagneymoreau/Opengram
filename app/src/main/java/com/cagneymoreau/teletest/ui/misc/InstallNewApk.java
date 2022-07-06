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

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Update;
import com.cagneymoreau.teletest.data.TelegramController;

import org.drinkless.td.libcore.telegram.TdApi;

public class InstallNewApk extends Fragment implements DialogSender {


    View fragmentView;

    Button button, deleteButton;

    TextView walloftextview;

    TelegramController telegramController;

    //0 = idle, 1 = downloading, 2 = success, -1 = failed;
    int statusflag = 0;

    DialogSender d = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.about_layout, container, false);
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        deleteButton = fragmentView.findViewById(R.id.about_privacy_button);
        deleteButton.setVisibility(View.INVISIBLE);
        
        button = fragmentView.findViewById(R.id.about_intent_button);
        

        walloftextview = fragmentView.findViewById(R.id.about_textwall_textview);
        walloftextview.setText("Please download and install the apk for latest bug fixes and feature updates");

        buildUI();

        return fragmentView;

    }


    private void buildUI()
    {
        if (!Update.isDownLoaded(telegramController.getApk(), telegramController)){
            
            button.setText("Download");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (statusflag == 1) return;

                   setvalue(null,"",0,1);
                    Update.downloadApk(telegramController.getApk(), telegramController);
                    getActivity().onBackPressed();
                }
            });
            
        }else {
            button.setText("Install");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Update.openApkInstall(getActivity(), telegramController.getApkFile());
                }
            });
        }
        
        
    }


    @Override
    public void setvalue(Object obj, String operation, int pos, int result) {
        statusflag = result;
        switch (statusflag){

            case 0:
                //nothing
                break;

            case 1:
                walloftextview.setText("downloading...");
                break;

            case 2:

                walloftextview.setText("download complete");
                buildUI();
                break;

            case -1:
                walloftextview.setText("failed");
                break;

        }
    }
}
