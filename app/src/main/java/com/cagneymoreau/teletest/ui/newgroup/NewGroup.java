package com.cagneymoreau.teletest.ui.newgroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.cagneymoreau.teletest.ui.contacts.recycle.ContactsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;


/**
 * show a recycleview with all the chats available
 */
public class NewGroup extends Fragment  implements SearchView.OnQueryTextListener, DialogSender {

    private final static String TAG = "chatlist_fragment";

    View fragment;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ContactsAdapter contactsAdapter;

    Controller controller;

    Paywall paywall;

    Client.ResultHandler handler;

    String currquery;

    ArrayList<TdApi.User> usersList;

    ArrayList<TdApi.User> selected;

    LinearLayoutCompat selectedLayout;

    FloatingActionButton floatingActionButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.newgroup_fragment, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.contacts_title));

        controller = Controller.getInstance((MainActivity) getActivity());

        paywall = new Paywall(((MainActivity) getActivity()));

        selected = new ArrayList<>();

        buildFrag();

        floatingActionButton = fragment.findViewById(R.id.newgroup_fab);
        selectedLayout = fragment.findViewById(R.id.newgroup_users_chosen_layout);

        ((MainActivity)getActivity()).setQueryListener(this);

        return fragment;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).removeChatAdapter();
    }





    private void buildFrag() {

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                controller.setUsers(selected);

                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_nameGroup);

            }
        });



        recyclerView = fragment.findViewById(R.id.chatlist_recycler);

        contactsAdapter = new ContactsAdapter(((MainActivity) getActivity()));

        Collection<TdApi.User> val = ((MainActivity)getActivity()).getUsers().values();
        usersList = new ArrayList<>(val);

        contactsAdapter.setList(usersList);
        recyclerView.setAdapter(contactsAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        ((MainActivity)getActivity()).getChatsList();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {

                refreshSelected(position);

            }

            @Override
            public void onLongClick(View view, int position, float x, float y) {



            }
        }));

    }

    private void refreshSelected(int p)
    {
        selected.add(usersList.get(p));

        LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView avatar = new ImageView(linearLayout.getContext());
        Utilities.setUserAvater(usersList.get(p), avatar, ((MainActivity) getActivity()));
        linearLayout.addView(avatar);

        TextView name = new TextView(linearLayout.getContext());
        name.setText(usersList.get(p).firstName);
        linearLayout.addView(name);

        selectedLayout.addView(linearLayout);

    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    //when we search we just paste a new list into the adpater
    @Override
    public boolean onQueryTextChange(String s) {
       contactsAdapter.setSearchReturn(filter(s,((MainActivity)getActivity()).getUsers()));

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

    //callback for popup long press pop up dialog
    @Override
    public void setvalue(int i, TdApi.Chat c, int position) {

    }

    //lets list attach to our adapter
    public Controller getMarketController()
    {
        return controller;
    }





}
