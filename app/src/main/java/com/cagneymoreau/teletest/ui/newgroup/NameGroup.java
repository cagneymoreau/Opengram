package com.cagneymoreau.teletest.ui.newgroup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.Paywall;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.RecyclerTouchListener;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;
import com.cagneymoreau.teletest.ui.contacts.recycle.ContactsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import kotlin.Suppress;


/**
 * shows when making a group or channel to insert name, image and description
 *
 */
public class NameGroup extends Fragment {

    private final static String TAG = "namegroup_fragment";


    View fragment;
    Controller controller;
    TelegramController telegramController;

    ImageView imgview;
    EditText nameET, desET;

    FloatingActionButton floatingActionButton;

    String flag;

    ArrayList<TdApi.User> groupmembers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.namegroup_layout, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.namegroup_title));

        controller = Controller.getInstance((MainActivity) getActivity());
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        groupmembers = controller.getUserList();

        flag = controller.getCreateNewGroupFlag();

        imgview = fragment.findViewById(R.id.namegroup_imageview);

        nameET = fragment.findViewById(R.id.namegroup_name_edittext);
        desET = fragment.findViewById(R.id.namegroup_description_edittext);




        floatingActionButton = fragment.findViewById(R.id.namegroup_fab);

        buildFrag();

        return fragment;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        telegramController.removeChatAdapter();
    }





    private void buildFrag() {


        imgview.setOnClickListener(view -> {

            // TODO: 6/27/2022 open gallery 

        });

        if (flag == Utilities.GROUP){
            desET.setVisibility(View.INVISIBLE);
        }

       floatingActionButton.setOnClickListener(view -> {

           //create group and navigate to temp landing page while group is formed
           if (flag.equals(Utilities.GROUP))
           {

               String groupName = nameET.getText().toString();

               long[] memids = new long[groupmembers.size()];

               for (int i = 0; i < groupmembers.size(); i++) {

                   memids[i] = groupmembers.get(i).id;
               }


               Client.ResultHandler handler = object -> {

                   if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR)
                   {
                       TdApi.Chat c = (TdApi.Chat) object;

                       Bundle b = new Bundle();
                       long id = c.id;
                       b.putLong("chatId", id);
                       getActivity().runOnUiThread(() -> {
                           Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_simpleChat, b);
                       });

                   }

               };

               telegramController.createGroup(memids, groupName, handler);

           }
           else if(flag.equals(Utilities.CHANNEL))
           {

               Client.ResultHandler handler = object -> {

                   if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR)
                   {
                       TdApi.Chat c = (TdApi.Chat) object;

                       Bundle b = new Bundle();
                       long id = c.id;
                       b.putLong("chatId", id);
                       getActivity().runOnUiThread(() ->{
                           Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_visibility, b);
                       });
                   }
               };

               String name = nameET.getText().toString();
               String desc = desET.getText().toString();

               telegramController.createChannel(name, desc, handler);
           }

       });




    }








}
