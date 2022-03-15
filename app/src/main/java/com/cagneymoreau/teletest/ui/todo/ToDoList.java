package com.cagneymoreau.teletest.ui.todo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.MessageListCallback;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.RecyclerTouchListener;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.MarketController;
import com.cagneymoreau.teletest.data.MessageLocation;
import com.cagneymoreau.teletest.ui.chat.recycle.MessageListAdapter;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;


/**
 * Hold a list of messages that require you to do something
 * Posting here will add to saved messages and then auto add to todolist
 */
public class ToDoList extends Fragment implements MessageListCallback {


    TdApi.SearchChatMessages h;
    TdApi.SearchMessagesFilterPhoto p;

    private final static String TAG = "todolist_fragment";

    View fragment;

    long chatId;

    Client.ResultHandler messageRecieiver;
    ArrayList<TdApi.Message> messageList = new ArrayList<>();

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    MessageListAdapter adapter;

    long prevId;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;


    EditText messageInput;
    ImageButton stickerButton, sendAttachButton;
    ImageView imageViewBackground;

    boolean messageReadyState = false;

    //copied from simplechat new items below
    //------------------------------

    MarketController marketController;

    ArrayList<MessageLocation> todoItems;

    int pos = 0;
    int buffer = 32;
    int waitCount = 0;
    boolean requesting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.simple_chat, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("ToDo List");

        chatId = ((MainActivity)getActivity()).getMyId();

        marketController = MarketController.getInstance(((MainActivity) getActivity()));

        todoItems = marketController.getToDoList();


        buildCanPostUI();

        buildRecycle();

        buildMessageReceiver();

        getMessages();

        return fragment;

    }


    /**
     *
     */
    private void buildCanPostUI()
    {
        messageInput = fragment.findViewById(R.id.simplechat_messageInput_edittext);
        View.OnClickListener sendListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TdApi.MessageSendOptions options = new TdApi.MessageSendOptions(true, false, new TdApi.MessageSchedulingStateSendAtDate(1) );

                TdApi.InputMessageText textMess = new TdApi.InputMessageText();

                TdApi.FormattedText textC = new TdApi.FormattedText();
                textC.text = messageInput.getText().toString();
                textMess.text = textC;
                messageInput.setText("");
                
                ((MainActivity)getActivity()).sendPosting(chatId, 0, options, new TdApi.ReplyMarkupRemoveKeyboard(), textMess, new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        if (object.getConstructor() == TdApi.Error.CONSTRUCTOR){
                            Toast.makeText(getContext(), "failed to create new todo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        View.OnClickListener attachListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 1/26/2022
                Toast.makeText(getContext(), "We need to attach something", Toast.LENGTH_SHORT).show();
                
            }
        };

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && !messageReadyState){
                    messageReadyState = true;
                    sendAttachButton.setImageResource(R.drawable.ic_baseline_send_24);
                    sendAttachButton.setOnClickListener(sendListener);
                }else if (charSequence.length() == 0 && messageReadyState){
                    messageReadyState = false;
                    sendAttachButton.setImageResource(R.drawable.ic_baseline_attach_file_24);
                    sendAttachButton.setOnClickListener(attachListener);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        stickerButton = fragment.findViewById(R.id.simpleChat_sticker_button);
        stickerButton.setImageResource(R.drawable.ic_baseline_sticky_note_2_24);
        stickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 1/26/2022 this could be similiar to attach menu
                Toast.makeText(getContext(), "show sticker menu", Toast.LENGTH_SHORT).show();
            }
        });

        sendAttachButton = fragment.findViewById(R.id.simpleChat_sendattach_button);
        sendAttachButton.setImageResource(R.drawable.ic_baseline_attach_file_24);
        sendAttachButton.setOnClickListener(attachListener);


    }


    private void buildRecycle()
    {
        recyclerView = fragment.findViewById(R.id.simpleChatRecycleView);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        adapter = new MessageListAdapter((MainActivity) getActivity(), this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            getMessages();
                        }
                    }
                }
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {

                // TODO: 2/23/2022 remove item

            }

            @Override
            public void onLongClick(View view, int position, float x, float y) {

                // TODO: 2/23/2022 show menu containing
                //  move up down
                //  edit

            }
        }));

    }


    private void getMessages()
    {
        if (pos == todoItems.size()) return;

        requesting = true;

        int end = pos + buffer;

        if (end > todoItems.size()) end = todoItems.size();

        for (int i = pos; i < end ; i++) {

            ((MainActivity)getActivity()).getMessage(todoItems.get(i).getChatId(), todoItems.get(i).getLastMessageIndexedId(), messageRecieiver);
            waitCount++;
        }

        pos = end;

        requesting = false;

        if (waitCount == 0) postMessagesToUI();

    }

    private void buildMessageReceiver()
    {
        if (messageRecieiver == null) {

            messageRecieiver = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.Message.CONSTRUCTOR){
                        Log.d(TAG, "onResult: " + object.toString());
                        return;
                    }

                    TdApi.Message message = (TdApi.Message) object;

                    messageList.add(message);

                    waitCount--;

                    if (waitCount == 0 && !requesting) postMessagesToUI();

                }
            };

        }

    }

    private void postMessagesToUI()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addItems(messageList);
                messageList.clear();
                loading = true;
            }
        });

    }

    private void removeItem(TdApi.Message m)
    {
        int found = -1;

        for (int i = 0; i < todoItems.size(); i++) {

            if (todoItems.get(i).getChatId() == m.chatId && todoItems.get(i).getLastMessageIndexedId() == m.id){
                found = i;
                break;
            }
        }

        //messageList.remove(found);
        adapter.removeItem(m);
        todoItems.remove(found);
        
    }


    @Override
    public void messageAction(TdApi.Message message, String action) {

        if (action.equals(Utilities.DELETE)){

            removeItem(message);

        }else{
            ((MainActivity)getActivity()).performMessageAction(message, action);
        }

    }



}
