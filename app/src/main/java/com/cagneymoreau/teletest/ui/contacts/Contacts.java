package com.cagneymoreau.teletest.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;


/**
 * show a recycleview with all the chats available
 */
public class Contacts extends Fragment  implements SearchView.OnQueryTextListener, DialogSender {



    private final static String TAG = "chatlist_fragment";



    View fragment;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ContactsAdapter contactsAdapter;

    Controller controller;
    TelegramController telegramController;

    Paywall paywall;

    Client.ResultHandler handler;

    String currquery;

    ArrayList<TdApi.User> usersList;

    MainActivity mainActivity;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.contacts_fragment, container, false);

        mainActivity = (MainActivity) getActivity();

        mainActivity.getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.contacts_title));

        controller = Controller.getInstance((MainActivity) getActivity());
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        paywall = new Paywall(((MainActivity) getActivity()));

        buildFrag();
        ((MainActivity)getActivity()).setQueryListener(this);

        return fragment;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        telegramController.removeChatAdapter();
    }





    private void buildFrag() {

        recyclerView = fragment.findViewById(R.id.chatlist_recycler);

        contactsAdapter = new ContactsAdapter(((MainActivity) getActivity()));

        Collection<TdApi.User> val = telegramController.getUsers().values();
        usersList = new ArrayList<>(val);

        Collections.sort(usersList, new Utilities.UserComparator());

        contactsAdapter.setList(usersList);
        recyclerView.setAdapter(contactsAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        telegramController.getChatsList();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {

                controller.setDataBindingItem(contactsAdapter.getUser(position));
                Navigation.findNavController(mainActivity, R.id.nav_host_fragment).navigate(R.id.action_global_profile);


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
        contactsAdapter.setSearchReturn(list);

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
