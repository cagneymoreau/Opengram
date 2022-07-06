package com.cagneymoreau.teletest.ui.chatlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.MainActivity;

import com.cagneymoreau.teletest.Paywall;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.RecyclerTouchListener;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;
import com.cagneymoreau.teletest.ui.chatlist.recycle.ChatListAdapter;
import com.google.android.material.tabs.TabLayout;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;


/**
 * show a recycleview with all the chats available
 */
public class ChatList extends Fragment  implements SearchView.OnQueryTextListener, DialogSender {


    private final static String TAG = "chatlist_fragment";


    View fragment;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ChatListAdapter chatlistAdapter;

    Controller controller;

    DialogSender thisChat = this;

    Paywall paywall;

    Client.ResultHandler handler;

    String currquery;
    long[] found;
    int count;
    ArrayList<TdApi.Chat> globalSearch;

    TabLayout tabLayout;

    int list = 1;

    TelegramController telegramController;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.chatlist_fragment, container, false);

        controller = Controller.getInstance((MainActivity) getActivity());
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        String title = getActivity().getResources().getString(R.string.app_name);
        if (controller.getMessageToForward() != null){
             title = "...forward to?";
        }

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(title);

        paywall = new Paywall(((MainActivity) getActivity()));

        return fragment;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //((MainActivity)getActivity()).onCreateOptionsMenu(menu);
        // TODO: 6/23/2022  
    }

    @Override
    public void onPause() {
        super.onPause();
        (telegramController).removeChatAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        buildTabs();
        buildFrag();
        ((MainActivity)getActivity()).setQueryListener(this);

    }


    private void buildTabs()
    {

        tabLayout = fragment.findViewById(R.id.chatlist_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Channel"), 0);
        tabLayout.addTab(tabLayout.newTab().setText("Group"), 1);
        tabLayout.addTab(tabLayout.newTab().setText("Private"), 2);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()){

                    case 0:
                        //if (list == 1) return;
                        list = 1;
                        chatlistAdapter.setSortedList(telegramController.getChannChatList());
                        break;

                    case 1:
                        //if (list == 2) return;
                        list = 2;
                        chatlistAdapter.setSortedList(telegramController.getGroupChatList());
                        break;

                    case 2:

                        //if (list == 3) return;
                        list = 3;
                        chatlistAdapter.setSortedList((telegramController).getPrivateChatList());
                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabUnselected: ");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected: ");
            }
        });


    }


    private void buildFrag() {

        recyclerView = fragment.findViewById(R.id.chatlist_recycler);

        chatlistAdapter = new ChatListAdapter(((MainActivity) getActivity()), this);
        chatlistAdapter.setSortedList(telegramController.getChannChatList());
        telegramController.setChatAdapter(chatlistAdapter);
        recyclerView.setAdapter(chatlistAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        telegramController.getChatsList();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {

                //((MainActivity)getActivity()).getChatsList();

                //((MainActivity)getActivity()).getChatHistory(chatlistAdapter.getChat(position), 0,5);

                //((MainActivity)getActivity()).printLists();
                Bundle b = new Bundle();
                long id = chatlistAdapter.getChat(position).id;
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
       chatlistAdapter.setSearchReturn(filter(s,telegramController.getEasyChats()));

       currquery = s;
       handler = new Client.ResultHandler() {
           @Override
           public void onResult(TdApi.Object object) {

               if (TdApi.Chats.CONSTRUCTOR == object.getConstructor()){
                   TdApi.Chats chats =(TdApi.Chats) object;
                    count = chats.totalCount;
                   found = chats.chatIds;
                   collectChats();

               }
           }
       };

        telegramController.searchChatsAndUsers(s, handler);

       return true;
    }

    private void collectChats()
    {
        globalSearch = new ArrayList<>();

        Client.ResultHandler resultHandler = new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {

                if (TdApi.Chat.CONSTRUCTOR == object.getConstructor()){
                    globalSearch.add((TdApi.Chat) object);
                    if (globalSearch.size() == count)
                    {
                        ((MainActivity)getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatlistAdapter.extendSearchResults(globalSearch);
                            }
                        });
                    }
                }

            }
        };

        for (int i = 0; i < found.length; i++) {
            telegramController.getChat(found[i], resultHandler);
        }
    }


    private ArrayList<TdApi.Chat> filter(String query, SortedList<TdApi.Chat> chats)
    {
        if (query.isEmpty()) return null;

        query = query.toLowerCase();

        ArrayList<TdApi.Chat> matchingchats = new ArrayList<>();

        for (int i = 0; i < chats.size(); i++) {

            String title = chats.get(i).title.toLowerCase();

            if (title.contains(query)) matchingchats.add(chats.get(i));

        }

        return matchingchats;

    }

    //callback for popup long press pop up dialog


    @Override
    public void setvalue(Object obj, String operation, int pos, int result) {

    }

    //lets list attach to our adapter
    public Controller getMarketController()
    {
        return controller;
    }

}
