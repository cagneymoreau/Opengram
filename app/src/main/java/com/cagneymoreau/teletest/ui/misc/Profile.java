package com.cagneymoreau.teletest.ui.misc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cagneymoreau.teletest.BuildConfig;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

/**
 * View another users user profile *
 * View a channel profile
 *
 * If viewing self profile you should see settings and not this fragment
 * 
 * 
 * // TODO: 6/23/2022 show shared files or access messagees etc 
 *
 */

public class Profile extends Fragment {


    View fragmentView;

    AppCompatImageView avatarImgView;
    TextView nameTv, lastSeenTv;

    TextView mobileTv, usernameTv, notificationTv;

    Controller controller;
    TelegramController telegramController;

    FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.user_profile, container, false);

        controller = Controller.getInstance(((MainActivity) getActivity()));
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        avatarImgView = fragmentView.findViewById(R.id.contacts_card_avatar_imageview);
        nameTv = fragmentView.findViewById(R.id.contacts_card_name_textView);
        lastSeenTv = fragmentView.findViewById(R.id.contacts_card_lastseen_textview);

        mobileTv = fragmentView.findViewById(R.id.profile_phone);
        usernameTv = fragmentView.findViewById(R.id.profile_username);
        notificationTv = fragmentView.findViewById(R.id.profile_notifications);

        floatingActionButton = fragmentView.findViewById(R.id.profile_fab);

        loadData();

        return fragmentView;

    }



    private void loadData()
    {
        ArrayList<Object> data = controller.getDataBinding();

        TdApi.User user = (TdApi.User) data.get(0);
        TdApi.UserFullInfo info = telegramController.getUserFullInfo(user.id);

        Utilities.setUserAvater(user, avatarImgView, ((MainActivity) getActivity()));

        nameTv.setText(user.firstName);

        if (user.status != null)
        {
            lastSeenTv.setText(Utilities.getStatusHumanReadable(user.status));
        }

        if (user.phoneNumber != null) {
            mobileTv.setText(user.phoneNumber);
        }

        usernameTv.setText(user.username);


        floatingActionButton.setOnClickListener(view -> {

            Bundle b = new Bundle();
            long id = user.id;
            b.putLong("chatId", id);
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_simpleChat, b);

        });
    }


    }




