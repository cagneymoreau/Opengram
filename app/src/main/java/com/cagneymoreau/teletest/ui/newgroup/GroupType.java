package com.cagneymoreau.teletest.ui.newgroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
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
import com.cagneymoreau.teletest.ui.newgroup.recycle.NewGroupAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;


/**
 * Landing from main menu to create new type of chat
 */
public class GroupType extends Fragment  implements SearchView.OnQueryTextListener, DialogSender {

    private final static String TAG = "chatlist_fragment";

    View fragment;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    NewGroupAdapter newGroupAdapter;

    Controller controller;
    TelegramController telegramController;

    Paywall paywall;

    Client.ResultHandler handler;

    String currquery;

    ArrayList<TdApi.User> usersList;

    ArrayList<TdApi.User> selected;

    Button createGroupButton, secretChatButton, createChannelButton;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.grouptype_fragment, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.newgroup_title));

        controller = Controller.getInstance((MainActivity) getActivity());
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        paywall = new Paywall(((MainActivity) getActivity()));

        selected = new ArrayList<>();




        buildButtons();

        buildFrag();

        ((MainActivity)getActivity()).setQueryListener(this);

        return fragment;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        telegramController.removeChatAdapter();
    }


    private void buildButtons()
    {
        createGroupButton = fragment.findViewById(R.id.grouptype_group_button);
        secretChatButton = fragment.findViewById(R.id.grouptype_secret_button);
        createChannelButton = fragment.findViewById(R.id.grouptype_channel_button);

        createGroupButton.setOnClickListener(view -> {

            controller.setCreateNewGroupFlag(Utilities.GROUP);

            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_newGroup2);

        });

        secretChatButton.setOnClickListener(view -> {

            Toast.makeText(getContext(), "not implemented yet", Toast.LENGTH_SHORT).show();
            // TODO: 6/27/2022 secret chat

        });

        createChannelButton.setOnClickListener(view->{

            controller.setCreateNewGroupFlag(Utilities.CHANNEL);

            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_nameGroup);

        });


    }



    private void buildFrag() {

        recyclerView = fragment.findViewById(R.id.grouptype_recycler);

        newGroupAdapter = new NewGroupAdapter(((MainActivity) getActivity()));

        ConcurrentMap<Long, TdApi.User> users = telegramController.getUsers();
        Collection<TdApi.User> val = users.values();
        usersList = new ArrayList<>(val);

        Collections.sort(usersList, new Utilities.UserComparator());

        newGroupAdapter.setList(usersList);
        recyclerView.setAdapter(newGroupAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        telegramController.getChatsList();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {

                Bundle b = new Bundle();
                long id = usersList.get(position).id;
                b.putLong("chatId", id);
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_simpleChat, b);

            }

            @Override
            public void onLongClick(View view, int position, float x, float y) {



            }
        }));

    }



    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    //when we search we just paste a new list into the adpater
    @Override
    public boolean onQueryTextChange(String s) {
        ArrayList<TdApi.User> list = Utilities.filter(s,telegramController.getUsers(), true);
       newGroupAdapter.setSearchReturn(list);

       currquery = s;

       return true;
    }


    @Override
    public void setvalue(Object obj, String operation, int pos, int result) {

    }

    //lets list attach to our adapter
    public Controller getMarketController()
    {
        return controller;
    }





}
