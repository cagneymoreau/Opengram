package com.cagneymoreau.teletest.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.cagneymoreau.teletest.ui.chat.recycle.MessageListAdapter;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

// TODO: 1/26/2022 text input + paste?
// TODO: 1/26/2022 attach stuff input pic, video file
// TODO: 1/26/2022  add background
// TODO: 1/26/2022 chatt bubbles with user icon 
// TODO: 1/26/2022 sticker button and display

/**
 * Interact with a specific chat
 */
public class SimpleChat extends Fragment implements MessageListCallback {


    TdApi.SearchChatMessages h;
    TdApi.SearchMessagesFilterPhoto p;

    private final static String TAG = "SimpleChat_fragment";

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

    boolean cantpost = false;

    TdApi.Chat chat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.simple_chat, container, false);

        prevId = 0L;

        chatId = getArguments().getLong("chatId");

        chat = ((MainActivity)getActivity()).getSpecificChat(chatId);

        if (chat == null){


            ((MainActivity)getActivity()).createNewPrivateChat(chatId, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR){

                        chat = (TdApi.Chat) object;
                        loadChat();

                    }else{
                        Log.e(TAG, "onResult: unable to get chat stared", null);
                        ((MainActivity)getActivity()).onBackPressed();
                    }
                }
            });

        }else {
            loadChat();
        }

        return fragment;
    }

    private void loadChat()
    {


        if (chat == null)

            if (chat.type.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR){
                TdApi.ChatTypeSupergroup supergroup = (TdApi.ChatTypeSupergroup) chat.type;
                cantpost =supergroup.isChannel;
            }

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(chat.title);


        if (cantpost){
            buildChannelUI();
        }else{
            buildCanPostUI();
        }

        buildRecycle();

        requestMoreData();

    }


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

                        //Log.d(TAG, "onResult: ");
                    }
                });


                View v = getActivity().getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }


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

    private void buildChannelUI()
    {
        LinearLayout bottomlayout = fragment.findViewById(R.id.simpleChat_bottom_linear);
        bottomlayout.removeAllViews();
        Button button = new Button(fragment.getContext());
        button.setText("join or mute button");
        bottomlayout.addView(button);
        // TODO: 1/28/2022 set button layout and make join mute button function

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
                            requestMoreData();
                        }
                    }
                }
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {

            }

            @Override
            public void onLongClick(View view, int position, float x, float y) {

            }
        }));

    }


    private void requestMoreData()
    {
               if (messageRecieiver == null) {

                   messageRecieiver = new Client.ResultHandler() {
                       @Override
                       public void onResult(TdApi.Object object) {

                           // TODO: 12/28/2021 what ebout errors

                           if (object.getConstructor() != TdApi.Messages.CONSTRUCTOR){
                               Log.d(TAG, "onResult: " + object.toString());
                               return;
                           }

                           TdApi.Messages messages = (TdApi.Messages) object;

                           if (messages.messages != null) {

                               for (int i = 0; i < messages.messages.length; i++) {
                                   messageList.add(messages.messages[i]);
                               }
                           }

                           if (messageList.size() < 32 && messages.messages.length != 0) {
                               requestMoreData();
                           } else {
                               getActivity().runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       adapter.addItems(messageList);
                                       messageList.clear();
                                       loading = true;
                                   }
                               });

                           }

                       }
                   };

               }


               if (messageList.size() > 1){
                   prevId =  messageList.get(messageList.size()-1).id;
               }

        ((MainActivity)getActivity()).getChatHistory(chatId, prevId, 5, messageRecieiver);



    }

    //updates from api or from interacting with a viewholder can be routed to here
    @Override
    public void messageAction(TdApi.Message message, String action) {

        //new message from us or other
        if (action.equals(Utilities.NEWMESS)){

            if  (message.sendingState != null) return;

            adapter.addItem(message);
            recyclerView.scrollToPosition(0);


        }
        //we interacted with viewholder
        else{

            ((MainActivity)getActivity()).performMessageAction(message, action);
        }






    }


    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).cancelLiveChat();
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setLiveChat(this, chatId);
    }
}
