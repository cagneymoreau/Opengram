package com.cagneymoreau.teletest.ui.calllogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.Paywall;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;
import com.cagneymoreau.teletest.ui.calllogs.recycle.CallLogAdapter;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;


/**
 * show a recycleview with all the chats available
 */
public class CallLog extends Fragment  implements SearchView.OnQueryTextListener, DialogSender {



    private final static String TAG = "calllog_fragment";



    View fragment;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CallLogAdapter contactsAdapter;

    Controller controller;
    TelegramController telegramController;

    Paywall paywall;

    Client.ResultHandler handler;

    String currquery;
    long[] found;
    int count;
    ArrayList<TdApi.Chat> globalSearch;

    ArrayList<TdApi.User> usersList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.chatlist_fragment, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.contacts_title));

        controller = Controller.getInstance((MainActivity) getActivity());

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


    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    //when we search we just paste a new list into the adpater
    @Override
    public boolean onQueryTextChange(String s) {
       contactsAdapter.setSearchReturn(filter(s,telegramController.getUsers()));

       currquery = s;

       return true;
    }




    private ArrayList<TdApi.User> filter(String query, ConcurrentMap<Long, TdApi.User> users)
    {
        if (query.isEmpty()) return null;

        query = query.toLowerCase();

        Collection<TdApi.User> val = users.values();
        usersList = new ArrayList<>(val);

        ArrayList<TdApi.User> matchingchats = new ArrayList<>();

        for (int i = 0; i < usersList.size(); i++) {

            String title = usersList.get(i).firstName.toLowerCase();

            if (title.contains(query)) matchingchats.add(usersList.get(i));

        }

        return matchingchats;

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
