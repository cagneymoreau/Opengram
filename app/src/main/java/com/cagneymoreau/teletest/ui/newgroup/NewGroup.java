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
import com.cagneymoreau.teletest.ui.contacts.recycle.ContactsAdapter;
import com.cagneymoreau.teletest.ui.newgroup.recycle.NewGroupAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;


/**
 * show a recycleview with all the chats available so we can add members to group
 */
public class NewGroup extends Fragment  implements SearchView.OnQueryTextListener, DialogSender {

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

    LinearLayoutCompat selectedLayout;

    FloatingActionButton floatingActionButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.newgroup_fragment, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.newgroup_title));

        controller = Controller.getInstance((MainActivity) getActivity());
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        paywall = new Paywall(((MainActivity) getActivity()));

        selected = new ArrayList<>();

        floatingActionButton = fragment.findViewById(R.id.newgroup_fab);
        selectedLayout = fragment.findViewById(R.id.newgroup_users_chosen_layout);

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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selected.size() == 0)
                {
                    Toast.makeText(getContext(), "You must add some contacts", Toast.LENGTH_SHORT).show();
                    return;
                }

                controller.setUserList(selected);

                ((MainActivity)getActivity()).closeSearchView();

                controller.setCreateNewGroupFlag(Utilities.GROUP);
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_nameGroup);

            }
        });


        recyclerView = fragment.findViewById(R.id.newgroup_recycler);

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

                refreshSelected(position);

            }

            @Override
            public void onLongClick(View view, int position, float x, float y) {



            }
        }));

    }

    private void refreshSelected(int p)
    {
        TdApi.User user = newGroupAdapter.getUser(p);

        selected.add(user);

        LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView avatar = new ImageView(linearLayout.getContext());
        Utilities.setUserAvater(user, avatar, ((MainActivity) getActivity()));
        linearLayout.addView(avatar);

        TextView name = new TextView(linearLayout.getContext());
        name.setText(user.firstName);
        linearLayout.addView(name);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.remove(user);
                selectedLayout.removeView(linearLayout);
            }
        });

        selectedLayout.addView(linearLayout);

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
