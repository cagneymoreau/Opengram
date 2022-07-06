package com.cagneymoreau.teletest.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.MessageListCallback;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.RecyclerTouchListener;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;
import com.cagneymoreau.teletest.ui.Delete_Dialog;
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
public class SimpleChat extends Fragment implements MessageListCallback, DialogSender {


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

    TelegramController telegramController;
    Controller controller;

    String sendState = Utilities.NEWMESS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.simple_chat, container, false);

        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));
        controller = Controller.getInstance((MainActivity) getActivity());

        //((MainActivity)getActivity()).getSupportActionBar().setDisplayOptions();

        prevId = 0L;

        chatId = getArguments().getLong("chatId");

        chat = telegramController.getSpecificChat(chatId);

        if (chat == null){


            telegramController.createNewPrivateChat(chatId, new Client.ResultHandler() {
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, 1, Menu.NONE, "Search");
        // TODO: 6/23/2022  
        
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       switch (item.getItemId())
       {

           case 1:

           return true;
           
           case 2:
               
           return true;
           
           default:
               
           return false;    

       }
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
            TdApi.Message m = controller.getMessageToForward();
            if (m != null){

                LinearLayout btmwrapLayout = fragment.findViewById(R.id.simpleChat_bottomwrapper_linear);

                View reply = buildReplyView(m);

                btmwrapLayout.addView(reply, 0);

                sendState = Utilities.FORWARD;

            }
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

                TdApi.MessageSendOptions options = new TdApi.MessageSendOptions(true, false, new TdApi.MessageSchedulingStateSendAtDate(1));

                TdApi.InputMessageText textMess = new TdApi.InputMessageText();

                TdApi.FormattedText textC = new TdApi.FormattedText();
                textC.text = messageInput.getText().toString();
                textMess.text = textC;
                messageInput.setText("");

                if (sendState.equals(Utilities.NEWMESS)) {

                    telegramController.sendMessage(chatId, 0, options, new TdApi.ReplyMarkupRemoveKeyboard(), textMess, new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {

                            //Log.d(TAG, "onResult: ");
                        }
                    });


                }else if (sendState.equals(Utilities.FORWARD))
                {
                    //post forward
                    telegramController.forwardMessage();

                    removeReplyView();

                }
                else if (sendState.equals(Utilities.REPLY))
                {
                    //post reply
                    telegramController.replyMessage();
                    removeReplyView();

                }

                View v = getActivity().getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                sendState = Utilities.NEWMESS;

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

        telegramController.getChatHistory(chatId, prevId, 5, messageRecieiver);



    }

    //updates from api or from interacting with a viewholder can be routed to here
    @Override
    public void messageAction(TdApi.Message message, String action) {

        //new message from tdapi
        if (action.equals(Utilities.NEWMESS)){

            if  (message.sendingState != null) return;

            adapter.addItem(message);
            recyclerView.scrollToPosition(0);


        }else if(action.equals(Utilities.REPLY))
        {
            LinearLayout btmwrapLayout = fragment.findViewById(R.id.simpleChat_bottomwrapper_linear);

            View reply = buildReplyView(message);

            btmwrapLayout.addView(reply, 0);

            sendState = Utilities.REPLY;

        }

        else if(action.equals(Utilities.FORWARD))
        {
            controller.setMessageToForward(message);
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_chatList);

        }
        else if (action.equals(Utilities.EDIT))
        {

        }


        else if (action.equals(Utilities.DELETE))
        {
            String delete = "Delete this message?";

           if (message.canBeDeletedOnlyForSelf){
               new Delete_Dialog(delete, "", this,  message, action, 0).show(getChildFragmentManager(), TAG);
           }
           else if (message.canBeDeletedForAllUsers)
           {
               new Delete_Dialog(delete, "Delete for all users?", this, message, action, 0).show(getChildFragmentManager(), TAG);
           }

        }


        // TODO: 6/24/2022 is this need elsewhere etc
            //telegramController.performMessageAction(message, action);








    }

    @Override
    public void setvalue(Object obj, String operation, int pos, int result) {


        TdApi.Message mess = (TdApi.Message) obj;

        switch (operation)
        {
            case Utilities.DELETE:

                boolean allusers = result == 1;
                telegramController.deleteMessage(mess.chatId, new long[] {mess.id}, allusers, null);

                break;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        telegramController.cancelLiveChat();
    }


    @Override
    public void onResume() {
        super.onResume();
        telegramController.setLiveChat(this, chatId);
    }


    private View buildReplyView(TdApi.Message message)
    {
        View replyLayout = View.inflate(fragment.getContext(), R.layout.reply_layout, null);

        ImageView img = replyLayout.findViewById(R.id.reply_img);
        TextView text = replyLayout.findViewById(R.id.reply_text);

        switch (message.content.getConstructor() ) {

            case TdApi.MessageText.CONSTRUCTOR:
                TdApi.MessageText m = (TdApi.MessageText) message.content;
                text.setText(m.text.text);
                break;
            case TdApi.MessagePhoto.CONSTRUCTOR:
                TdApi.MessagePhoto mp = (TdApi.MessagePhoto) message.content;
                text.setText(mp.caption.text);
                Utilities.getMessagePhoto(mp, img, ((MainActivity) getActivity()));
                break;
            case TdApi.MessageAudio.CONSTRUCTOR:
                img.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                break;
            case TdApi.MessageVoiceNote.CONSTRUCTOR:
                img.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                break;
            case TdApi.MessageVideo.CONSTRUCTOR:
                TdApi.MessageVideo mv = (TdApi.MessageVideo) message.content;
                text.setText(mv.caption.text);
                Utilities.setMessageVideoThumb(mv, img, ((MainActivity) getActivity()));
                break;
            case TdApi.MessageVideoNote.CONSTRUCTOR:
                TdApi.MessageVideoNote mvn = (TdApi.MessageVideoNote) message.content;
                Utilities.setMessageVideoNoteThumb(mvn, img, ((MainActivity) getActivity()));
                break;
            case TdApi.MessageDocument.CONSTRUCTOR:
                text.setText("Document");
                break;
            case TdApi.MessageSticker.CONSTRUCTOR:
                text.setText("Sticker");
                break;
            case TdApi.MessageAnimation.CONSTRUCTOR:
                text.setText("Animation");
                break;
            default:
                text.setText("cannot display");
                break;

        }

            return replyLayout;
    }

    private void removeReplyView()
    {

    }


}
