package com.cagneymoreau.teletest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cagneymoreau.teletest.data.MarketController;
import com.cagneymoreau.teletest.databinding.ActivityMainBinding;
import com.cagneymoreau.teletest.ui.login.LoginFragment;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;




/**
 * area specific
 */

//basic app function


// FIXME: 2/25/2022 appears channs are doubling with new layout

// FIXME: 2/14/2022 old calls to download and post imagesviews arent being erased
// TODO: 2/14/2022 need to pass timestamp to download calls so setimageview can compare timestamps in race conditions in local vs download
// TODO: 2/15/2022 even better use a simple unlock in onbind and a lock on the return of a value to lock out delayed race

// FIXME: 2/11/2022 when app firt loads channel images are missing as well as user

// FIXME: 2/7/2022 download file method is not using unique id and may produce incorrect reults


// TODO: 2/25/2022 simplechat menu options,
//  layout and aimated stickers
//  display channel info messages  
// TODO: 2/25/2022 posting shows channs grayed 
//todo read reciepts on last message in chatlist and in chats

// TODO: 2/25/2022 user profile view

// TODO: 2/25/2022 list of users to create new group or chat
    
//chatlist


//chat


//market


//mypostings


//post viewer
    //other big ideas

// FUTUREITEM: file share display
// FUTUREITEM: profile groupchat availability or group event poll
// FUTUREITEM: channel and private splitter
// FUTUREITEM: send invoice cash or crypto

    //market
    // FUTUREITEM: prevent file spoof
    // FUTUREITEM: delete picture from channel after deleting from add
    // FUTUREITEM: missing micture looks like add picture
// FUTUREITEM: reduce heap size for found adverts to 1kb by replacing images meesagephoto with a remote and local id
// FUTUREITEM: macro for posting all add to new channels thathave been added
// FUTUREITEM: scroll lock on chats for exit content and last seen positioning  
// FUTUREITEM: join and leave chat
// FUTUREITEM: show date on chatlist if over n days
// FUTUREITEM: cant search people who havent been contacted in main bar search
// FUTUREITEM: user profile view
// FUTUREITEM: image quality from downloads
// FUTUREITEM: individual profile
// FUTUREITEM: normal telegram features, calls leave join, open chat to last position
// FUTUREITEM: sticker maker

// FUTUREITEM: map view
// FUTUREITEM: post should include caption to download telemarket
// FUTUREITEM: auto add channels where store use detected
// FUTUREITEM: forwarding of events, add to calendar
// FUTUREITEM: chatlist search could be better
// FUTUREITEM: paymnt mechanism - need bot to charge the person
// FUTUREITEM: search shows new people and channels
// FUTUREITEM: dual lists - auto dual
// FUTUREITEM: settings menu: appearance, autoexpire date (min 3 months)
// FUTUREITEM: across device uniformity needed by saving to telegram api

public class MainActivity extends AppCompatActivity implements Client.ResultHandler, Client.ExceptionHandler {


    private final static String TAG = " Mainact";


    Client telegramClient;
    TdApi.TdlibParameters parameters;
    TdApi.AuthorizationState authorizationState;
    boolean haveAuthorization = false;

    private TdApi.User myself;
    
    private static final ConcurrentMap<Long, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Long, TdApi.UserFullInfo>();
    private static final ConcurrentMap<Long, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>();
    private static final ConcurrentMap<Long, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>();

    private static final ConcurrentMap<Long, Long> groupUpgradeTable = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<Long, TdApi.User>();
    private static final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Long, TdApi.BasicGroup>();
    private static final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Long, TdApi.Supergroup>();
    private static final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();


    /**
     * Unsorted chats solves concurrency and duplication issues
     * easychatlist displays sorts. changing sorting criteris prevent the sortedlist.add() from finding a correct match
     */
    ConcurrentMap<Long, TdApi.Chat> chatsUnsorted = new ConcurrentHashMap<Long, TdApi.Chat>();
    public SortedList<TdApi.Chat> easyChatList;
    public SortedList<TdApi.Chat> channChatList;
    public SortedList<TdApi.Chat> groupChatList;
    public SortedList<TdApi.Chat> privateChatList;


    RecyclerView.Adapter adapter;

    SearchView searchView;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    ArrayList<TdApi.Object>batchBuilder = new ArrayList<>();
    long lastBatch;
    long delay = 500;
    boolean currentlyProcessing = false;

    MarketController marketController;

    int target = -1;
    int actual = -1;

    //update options
    NavigationView navigationView;
    TdApi.Document apk;
    File readyApk;

    boolean closing;

    SearchView.OnQueryTextListener currSearchListener;


    ArrayList<Object> dataBinding = new ArrayList<>();
    Bitmap bitmapBind;

    ImageView myAvatar;
    TextView title;
    TextView subtitle;

    LoginFragment loginFragment;

    MainActivity mainActivity = this;


    MessageListCallback livechat;
    long livechatid;


    //region ----------- startup/callbacks



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 2/16/2022 constant debug log fill
        //FirebaseApp.initializeApp(this);

        lastBatch = System.currentTimeMillis();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarMain.toolbar;

        setSupportActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        View headerLayout = navigationView.getHeaderView(0);
        myAvatar = headerLayout.findViewById(R.id.nav_imageView);
        title = headerLayout.findViewById(R.id.nav_title);
        subtitle = headerLayout.findViewById(R.id.nav_subtitle);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.chatList,
                R.id.market,
                R.id.postChooser,
                R.id.todoList,
                //R.id.groupProject,
                R.id.settings,
                R.id.about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //final NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //final NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController (this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        closing = false;
        init();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (marketController != null) marketController.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: -------------------------------- ondestroy");

        closing = true;

        Log.d(TAG, "onStop: -----------------   on stop");

        if (telegramClient != null){
            telegramClient.close();
            telegramClient = null;
        }

        if (marketController != null) marketController.onPause();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        if (currSearchListener != null)
            searchView.setOnQueryTextListener(currSearchListener);
        return true;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //final NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //final NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }


    public void setQueryListener(SearchView.OnQueryTextListener listener)
    {
        currSearchListener = listener;
        if (searchView != null){
            searchView.setOnQueryTextListener(listener);
        }

    }


    /**
     * Createing the client should kick off the telegram api and onresult will guide the log in process
     */
    public void init()
    {

        easyChatList = buildEasyListCallback();
        channChatList = buildEasyListCallback();
        groupChatList = buildEasyListCallback();
        privateChatList = buildEasyListCallback();

        Client.execute(new TdApi.SetLogVerbosityLevel(0));

        if (telegramClient == null) {

            telegramClient = Client.create(this, this, this);

        }


    }


    public void start(boolean auth)
    {

        MainActivity activity = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (!auth) {

                    if (loginFragment == null){
                        //open login screen
                        Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.action_load_Fragment_to_loginFragment);
                    }else{
                        loginFragment.changeState(authorizationState);
                    }


                    actual = 0;
                }else{

                        whoAmI();
                        marketController = MarketController.getInstance(activity);
                        Update.findUpdatedApk(activity);
                        //open main chat list

                        if (loginFragment != null){
                            Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.action_loginFragment_to_chatList);
                        }else{
                            Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.action_load_Fragment_to_chatList);
                        }

                        actual = 1;


                }


            }
        });

    }

    public void setLiveChat(MessageListCallback callback, long chatId)
    {
        livechatid = chatId;
        livechat = callback;

    }

    public void cancelLiveChat()
    {
        livechat = null;
        livechatid = 0;
    }

    //endregion


    //region -------------  updates

    private void performBatch(TdApi.Object object)
    {

        if (object == null){
            Log.e(TAG, "performBatch: NUll OBJECT TO PROCESS", null);
            return;
        }


        switch (object.getConstructor())
        {

            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                Log.e(TAG, "performBatch: shouldnt happen", null);
                break;

            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                users.put(updateUser.user.id, updateUser.user);
                break;

            case TdApi.UpdateUserStatus.CONSTRUCTOR:  {
                TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                TdApi.User user = users.get(updateUserStatus.userId);
                // FIXME: 2/14/2022 null?
                if (user != null) {
                    synchronized (user) {
                        user.status = updateUserStatus.status;
                    }
                }
                break;
            }

            case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                //Log.d(TAG, "performBatch: upgrade to " + updateBasicGroup.basicGroup.id + " - "  + updateBasicGroup.basicGroup.upgradedToSupergroupId);
                groupUpgradeTable.put(updateBasicGroup.basicGroup.upgradedToSupergroupId, updateBasicGroup.basicGroup.id);
                //Log.e(TAG, "onResult: basicgroup" + updateBasicGroup.basicGroup.toString(),  null);
                break;

            case TdApi.UpdateSupergroup.CONSTRUCTOR:
                TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                //Log.e(TAG, "onResult: basicgroup" + updateSupergroup.supergroup.toString(),  null);
                break;

            case TdApi.UpdateSecretChat.CONSTRUCTOR:
                TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                //Log.e(TAG, "onResult: secretChat" + updateSecretChat.secretChat.toString(),  null);
                break;


            case TdApi.UpdateNewChat.CONSTRUCTOR: {
                TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                TdApi.Chat chat = updateNewChat.chat;
                //dont add to the sorted list until we have sorting information
                chatsUnsorted.put(chat.id, chat);

                // TODO: 2/18/2022  check for upgrades to supergroup here?

                //if a new chat appears and is an upgraded supergroup remove the old group. the api should adjust position really quickly so dont pass stuff on
                if (chat.type.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR){

                    TdApi.ChatTypeSupergroup supergroup = (TdApi.ChatTypeSupergroup) chat.type;

                    if (groupUpgradeTable.containsKey(supergroup.supergroupId)) {

                        long basicgroupid = groupUpgradeTable.get(supergroup.supergroupId);
                        marketController.checkForUpgrade(basicgroupid, supergroup.supergroupId);
                        TdApi.Chat c = chatsUnsorted.remove(basicgroupid);
                        if (c != null) {
                            //easyChatList.remove(c);
                            removeChat(c);
                        }
                    }

                }



                break;


            }
            case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.e(TAG, "performBatch: MISSING CHAT: chattitle", null);
                    return;
                }
                    chat.title = updateChat.title;



                break;
            }
            case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.e(TAG, "performBatch: MISSING CHAT: chatphoto", null);
                    return;
                }

                chat.photo = updateChat.photo;

                break;
            }
            case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
               TdApi.Chat foundchat = chatsUnsorted.get(updateChat.chatId);

               if (foundchat == null) {
                   Log.e(TAG, "performBatch: MISSING CHAT last message", null);
                   return;
               }

               //check if we are viewing this chat live and need it displayed
                if (livechatid == updateChat.chatId){
                    livechat.messageAction(updateChat.lastMessage, Utilities.NEWMESS);
                }


               //check if this message need processing
                if (marketController != null && updateChat.lastMessage != null) marketController.unkownLastMessageSorter(updateChat.lastMessage);

                foundchat.lastMessage = updateChat.lastMessage;

                if (updateChat.positions.length != 0){
                    //easyChatList.remove(foundchat);
                    removeChat(foundchat);
                    foundchat.positions = updateChat.positions;
                    //easyChatList.add(foundchat);
                    addChat(foundchat);
                }

                break;

            }
            case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;

                if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                        Log.e(TAG, "performBatch: MISSING CHAT: chatposition 1", null);
                    return;
                }

                TdApi.Chat foundchat = chatsUnsorted.get(updateChat.chatId);
                if (foundchat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: chatposition 2");
                    return;
                }
                 //easyChatList.remove(foundchat);
                removeChat(foundchat);
                foundchat.positions = new TdApi.ChatPosition[] {updateChat.position};
                //easyChatList.add(foundchat);
                addChat(foundchat);

                break;
            }
            case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);

                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: chatreadinbox2");
                    return;
                }

                chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                chat.unreadCount = updateChat.unreadCount;
                break;
            }
            case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: chat read outbox");
                    return;
                }
                    chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                break;
            }
            case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: unread mentioncount");
                    return;
                }
                chat.unreadMentionCount = updateChat.unreadMentionCount;
                break;
            }
            case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: mention read");
                    return;
                }
                chat.unreadMentionCount = updateChat.unreadMentionCount;
                break;
            }
            case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: reply markup");
                    return;
                }
                    chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                break;
            }
            case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                TdApi.Chat chat = chatsUnsorted.get(updateChat.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: chat read outbox");
                    return;
                }

                    chat.draftMessage = updateChat.draftMessage;
                    //easyChatList.add(chat);
                    addChat(chat);

                break;
            }
            case TdApi.UpdateChatPermissions.CONSTRUCTOR: {
                TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
                TdApi.Chat chat = chatsUnsorted.get(update.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: chat read outbox");
                    return;
                }
                    chat.permissions = update.permissions;
                break;
            }
            case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                TdApi.Chat chat = chatsUnsorted.get(update.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: notification settings");
                    return;
                }
                    chat.notificationSettings = update.notificationSettings;

                break;
            }
            case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                TdApi.Chat chat = chatsUnsorted.get(update.chatId);
                if (chat == null){
                    Log.d(TAG, "performBatch: MISSING CHAT: disable notifications");
                    return;
                }
                    chat.defaultDisableNotification = update.defaultDisableNotification;

                break;
            }
            case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                TdApi.Chat chat = chatsUnsorted.get(update.chatId);
                /*
                synchronized (chat) {
                    chat.isMarkedAsUnread = update.isMarkedAsUnread;
                }

                 */
                break;
            }
            case TdApi.UpdateChatIsBlocked.CONSTRUCTOR: {
                TdApi.UpdateChatIsBlocked update = (TdApi.UpdateChatIsBlocked) object;
                TdApi.Chat chat = chatsUnsorted.get(update.chatId);
                /*
                synchronized (chat) {
                    chat.isBlocked = update.isBlocked;
                }

                 */
                break;
            }
            case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: {
                TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
                TdApi.Chat chat = chatsUnsorted.get(update.chatId);
                /*
                synchronized (chat) {
                    chat.hasScheduledMessages = update.hasScheduledMessages;
                }

                 */
                break;
            }

            case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);


                break;
            case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                break;
            case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                //Log.d(TAG, "performBatch: supergroup debug " + updateSupergroupFullInfo.supergroupId + "  " + updateSupergroupFullInfo.supergroupFullInfo.upgradedFromBasicGroupId);
                //marketController.checkForUpgrade(updateSupergroupFullInfo);
                break;

            case TdApi.UpdateUnreadChatCount.CONSTRUCTOR:
                TdApi.UpdateUnreadChatCount updateUnreadChatCount = (TdApi.UpdateUnreadChatCount) object;

                break;
            case TdApi.UpdateOption.CONSTRUCTOR:

                break;

            case TdApi.Error.CONSTRUCTOR:
                //Log.e(TAG, "onResult: ok \n" + object.toString());
                break;
            case TdApi.Ok.CONSTRUCTOR:
                //Log.e(TAG, "onResult: ok \n");
                break;
            default:
                //Log.e(TAG, "onResult: default \n" + object.toString());

        }


    }


    @Override
    public void onResult(TdApi.Object object) {


        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR){
                onAuthorizationStateUpdate(object);
        }else{
            batchBuilder.add(object);
        }

        //onuithreadmethod
        if (marketController == null) return;

        if ( !currentlyProcessing && System.currentTimeMillis() > (lastBatch + delay)) {

            currentlyProcessing = true;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    beginBatchUpdates();

                    while (batchBuilder.size() > 0){
                        performBatch(batchBuilder.remove(0));
                    }

                    stopBatchUpdates();

                    lastBatch = System.currentTimeMillis();
                    currentlyProcessing = false;
                }
            });
        }


    }

    //endregion


    //region ----------------- telegram authorization


    public void setLoginFragment(LoginFragment l)
    {
        this.loginFragment = l;
    }

    public TdApi.AuthorizationState getAuthorizationState()
    {
        return authorizationState;
    }

    private void onAuthorizationStateUpdate(TdApi.Object object)
    {
        TdApi.AuthorizationState auth = ((TdApi.UpdateAuthorizationState) object).authorizationState;

        if (auth != null){
            authorizationState = auth;
        }

        switch (authorizationState.getConstructor()){

            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                String dbDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
                String filDir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/";

                parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = dbDir;
                parameters.filesDirectory = filDir;
                try {
                    parameters.apiId = Integer.valueOf(Utilities.getProperty("api_id", this));
                    parameters.apiHash = Utilities.getProperty("api_hash", this);
                }catch (Exception e)
                {
                    Log.e(TAG, "onAuthorizationStateUpdate: ", e);
                }

                //parameters.databaseDirectory
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.systemLanguageCode = "en-US";
                parameters.applicationVersion = "1.0";

                String reqString = Build.MANUFACTURER
                        + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                        + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

                parameters.deviceModel = reqString;

                telegramClient.send(new TdApi.SetTdlibParameters(parameters), this);

                break;

            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:

                telegramClient.send(new TdApi.CheckDatabaseEncryptionKey(), this);

                break;

            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                //we need to log in for first time

                start(haveAuthorization);


                //String phoneNumber =  "xxxxxx";
                //telegramClient.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), this);
                break;


            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR:
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;

            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                start(haveAuthorization);



                Log.d(TAG, "onAuthorizationStateUpdate: "); //// TODO: 12/23/2021 remove me/
                break;

            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR:
                // TODO: 12/22/2021 remove debug info throughout
                String firstName = "f";
                String lastName = "m";
                telegramClient.send(new TdApi.RegisterUser(firstName, lastName), this);
                break;

            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR:
                String password = "idk";
                telegramClient.send(new TdApi.CheckAuthenticationPassword(password), this);
                break;

            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                start(haveAuthorization);

                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                // TODO: 12/22/2021 need to make sure errors are covered in thie log in process such as wrong #, access or api failure
                Log.d(TAG, "onAuthorizationStateUpdate: logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                Log.d(TAG, "onAuthorizationStateUpdate: Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                Log.d(TAG, "onAuthorizationStateUpdate: Closed");

                if (!closing) {
                    telegramClient = Client.create(this, this, this); // recreate client after previous has closed
                }

                break;
            default:
                Log.e(TAG, "onAuthorizationStateUpdate: " + authorizationState);
        }

    }


    //endregion


    //region -------------- telegram getter/setters and callbacks

    @Override
    public void onException(Throwable e) {

        Log.e(TAG, "onException: " + e.toString(), e);

    }


    public Client getTelegramClient()
    {
        return telegramClient;
    }


    public ConcurrentMap<Long, TdApi.Chat> getChatsUnsorted()
    {
        int i = chatsUnsorted.size();

        return chatsUnsorted;
    }


    public void setChatAdapter(RecyclerView.Adapter adapter)
    {
        this.adapter = adapter;

    }


    public void removeChatAdapter()
    {
        adapter = null;
    }


    public void getChatsList()
    {
        long offset = 0;

        // TODO: 12/27/2021 will this offset always work
        if (easyChatList.size() > 0){
            offset = easyChatList.get(easyChatList.size()-1).id;
        }

        telegramClient.send(new TdApi.GetChats(new TdApi.ChatListMain(), Integer.MAX_VALUE), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                TdApi.Chats list = (TdApi.Chats) object;
                if (list.chatIds.length < 5){
                    getChatsList();
                }

                //Log.e(TAG, "onResult: " + object.toString(), null);

            }
        });

    }


    public SortedList<TdApi.Chat> getEasyChats()
    {
        return easyChatList;
    }

    public SortedList<TdApi.Chat> getChannChatList()
    {
        return channChatList;
    }

    public SortedList<TdApi.Chat> getGroupChatList()
    {
        return groupChatList;
    }

    public SortedList<TdApi.Chat> getPrivateChatList()
    {
        return privateChatList;
    }

    private SortedList<TdApi.Chat> buildEasyListCallback()
    {

        return new SortedList<>(TdApi.Chat.class, new SortedList.Callback<TdApi.Chat>() {


            @Override
            public int compare(TdApi.Chat o1, TdApi.Chat o2) {

                if (o1.positions.length > 0 && o2.positions.length > 0) {
                    if (o1.positions[0].order != o2.positions[0].order) {
                        return o1.positions[0].order < o2.positions[0].order ? 1 : -1;
                    }
                }
                if (o1.id != o2.id) {
                    return o1.id < o2.id ? 1 : -1;
                }
                return 0;


            }

            @Override
            public void onChanged(int position, int count) {
                if (adapter != null) adapter.notifyItemRangeChanged(position, count);

            }

            @Override
            public boolean areContentsTheSame(TdApi.Chat oldItem, TdApi.Chat newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(TdApi.Chat item1, TdApi.Chat item2) {
                return item1.id == item2.id;
            }


            @Override
            public void onInserted(int position, int count) {
                if (adapter != null) adapter.notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                if (adapter != null) adapter.notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                if (adapter != null) adapter.notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    private void addChat(TdApi.Chat c)
    {
        easyChatList.add(c);

        if (c.type.getConstructor() == TdApi.ChatTypeBasicGroup.CONSTRUCTOR ||
            c.type.getConstructor() == TdApi.ChatTypeSecret.CONSTRUCTOR ||
            c.type.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR){
            privateChatList.add(c);
        }
        else if (c.type.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR){

            TdApi.ChatTypeSupergroup s = (TdApi.ChatTypeSupergroup) c.type;

            if (s.isChannel){
                channChatList.add(c);
            }else{
                groupChatList.add(c);
            }

        }

    }

    private  void removeChat(TdApi.Chat c)
    {
        easyChatList.remove(c);
        channChatList.remove(c);
        groupChatList.remove(c);
        privateChatList.remove(c);

    }


    private void beginBatchUpdates()
    {
        easyChatList.beginBatchedUpdates();
        channChatList.beginBatchedUpdates();
        groupChatList.beginBatchedUpdates();
        privateChatList.beginBatchedUpdates();
    }

    private void stopBatchUpdates()
    {
        easyChatList.endBatchedUpdates();
        channChatList.endBatchedUpdates();
        groupChatList.endBatchedUpdates();
        privateChatList.endBatchedUpdates();
    }

    public void getChatHistory(long chatid, long messageid, int limit, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.GetChatHistory(chatid, messageid, 0, 5, false), handler);
    }


    public void whoAmI()
    {
        telegramClient.send(new TdApi.GetMe(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
               myself = (TdApi.User) object;
               fillDrawerHeader();
            }
        });

    }

    private void fillDrawerHeader()
    {
        MainActivity m = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Utilities.getUserImage(myself, myAvatar, m);
                Utilities.setUserAvater(myself,myAvatar, m);
                title.setText(myself.firstName);
                subtitle.setText(myself.phoneNumber);
            }
        });


    }

    //also works for saved message chat
    public long getMyId()
    {
        if (myself == null){
            Log.e(TAG, "getMyId: ", null);
            return 0;
        }

        return myself.id;
    }


    //  SearchChatMessages combined with  SearchMessagesFilter can find our html files
    public void getItemsFoundFromChat(String tag, long offestChatID, long offMessId, int limit, int lastSearch, int maxDate, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.SearchMessages(new TdApi.ChatListMain(), tag, 0,offestChatID,offMessId, limit, new TdApi.SearchMessagesFilterDocument(), lastSearch,maxDate), handler);

        //telegramClient.send(new TdApi.SearchChatMessages(chatId,  tag, null, messageId, offset, limit, new TdApi.SearchMessagesFilterDocument(), 0), handler);
    }


    public void downloadFile(int fileId, int priority, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.DownloadFile(fileId, priority, 0,0, true), handler);
        //getremotefile

    }

    public void getRemoteFile(String uniqueid, TdApi.FileType type, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.GetRemoteFile(uniqueid,type),handler);
    }

    public void deleteMessage(long chat, long[] messId, Client.ResultHandler h)
    {
        if (h == null)
        {
            telegramClient.send(new TdApi.DeleteMessages(chat, messId, true), this);
        }else{
            telegramClient.send(new TdApi.DeleteMessages(chat, messId, true), h);
        }

    }

    public void createNewPrivateChat(long userid, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.CreatePrivateChat(userid, false), handler);
    }

    public void sendPosting(long chat, long thread, TdApi.MessageSendOptions options, TdApi.ReplyMarkup replyMarkup, TdApi.InputMessageContent content, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.SendMessage(chat, thread, 0, options, replyMarkup, content), handler);
    }

    public void getUserProfilePhoto(TdApi.User user, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.GetUserProfilePhotos(user.id, 0, 1), handler);
    }


    public void createChannel(Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.CreateNewSupergroupChat("My Store Channel",true, "Your for sale posts", null, false)  , handler);
    }

    //public channels need a userGetUserProfilePhotosname field which is t.me/"field data"
    public void makeMyChannelpublic(long id, Client.ResultHandler h)
    {
        int i = new Random().nextInt( 1000);
        String link =  "telemarket_" + getMyId() + "_" + i;
        telegramClient.send(new TdApi.SetSupergroupUsername( id, link),h);

    }

    public void pinMessage(long chat, long mess, Client.ResultHandler h)
    {
        telegramClient.send(new TdApi.PinChatMessage(chat, mess, false, false),h);
    }

    public void getEmbeddingCode(long chat, long message, Client.ResultHandler handler)
    {
        //telegramClient.send(new TdApi.GetMessage(chat, message), handler);

        telegramClient.send(new TdApi.GetMessageEmbeddingCode(chat, message, false),handler);

        // TODO: 1/24/2022 supergroup id

        //telegramClient.send(new TdApi.GetMessageLink(chat, message, false, false),handler);

    }

    public void getMessageLinkInfo(String url, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.GetMessageLinkInfo(url), handler);
    }


    public void getMessage(long chatId, long messId, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.GetMessage(chatId, messId), handler);
    }


    public TdApi.User getUser(long userId)
    {
        if (userId == myself.id){
            return myself;
        }

        TdApi.User user =  users.get(userId);

        if (user != null){
            return user;
        }
        /*
        telegramClient.send(new TdApi.GetUser((int) userId), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                // TODO: 1/25/2022 we could put a callback here if needed
            }
        });

         */

        Log.e(TAG, "getUser: user not found", null);
        return null;
    }

    public void getChat(long id, Client.ResultHandler handler)
    {
        telegramClient.send(new TdApi.GetChat(id), handler);
    }

    public void searchChatsAndUsers(String query, Client.ResultHandler handler)
    {
        //local
        //telegramClient.send(new TdApi.SearchChats(query, 50), handler);
        //global
        telegramClient.send(new TdApi.SearchPublicChats(query), handler);
        //messages
       //telegramClient.send(new TdApi.SearchChatMessages(), handler);
    }

    public TdApi.Chat getSpecificChat(long chatid) {
        return chatsUnsorted.get(chatid);
    }


    public void newApkAvailable(TdApi.Document apk, String[] args)
    {
        this.apk = apk;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Menu menu = navigationView.getMenu();
                //Menu subMenu = menu.addSubMenu("new submenu");
                menu.add(R.id.menu_group, R.id.update,1000,"Update Available").setIcon(R.drawable.ic_baseline_update_24);
                //navigationView.invalidate();
            }
        });

    }

    public TdApi.Document getApk() {return apk;}

    public File getApkFile()
    {
        return readyApk;
    }

    public void setApkFile(File f)
    {
        readyApk = f;
    }


    public void submitLoginCode(String accesscode)
    {

        telegramClient.send(new TdApi.CheckAuthenticationCode (accesscode), this);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {

                if (haveAuthorization) return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mainActivity.getApplicationContext(), "No response. Is Verification Code correct?", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }, 1500);
    }

    //if this id belongs to a supergroup it will return the id from the group it upgraded from
    public long checkForBasicGroup(long chatId)
    {
        TdApi.Chat c = chatsUnsorted.get(chatId);

        //check if supergroup
        if (TdApi.ChatTypeSupergroup.CONSTRUCTOR != c.type.getConstructor()) return 0;
        TdApi.ChatTypeSupergroup superGroup = (TdApi.ChatTypeSupergroup) c.type;
        long superID = superGroup.supergroupId;

        //see if we have upgrade info
        if (!groupUpgradeTable.containsKey(superID))  return 0;
        return groupUpgradeTable.get(superID);

    }



    public void setDataBindingItem(Object o)
    {
        dataBinding.add(o);
    }

    public ArrayList<Object> getDataBinding()
    {
        ArrayList<Object> out = dataBinding;
        dataBinding = new ArrayList<>();
        return out;
    }

    public void setBitmapBind(Bitmap b)
    {
        bitmapBind = b;
    }

    public Bitmap getbitmapBind()
    {
        return bitmapBind;
    }


    public void performMessageAction(TdApi.Message message, String action)
    {
        switch (action)
        {

            case Utilities.REPLY:

              break;

            case Utilities.FORWARD:

                break;
            case Utilities.EDIT:

                break;

            case Utilities.PIN:

                break;

            case Utilities.DELETE:

                break;

            case Utilities.SAVE:

                break;

            case Utilities.TODO:

                marketController.addTodoListItem(message.chatId, message.id);
                Toast.makeText(getApplicationContext(),"Added to Todo", Toast.LENGTH_SHORT).show();

                break;

            case Utilities.LINK:

                break;

        }
    }


    //endregion


    //region------debug stuff

    /*
    public void printLists()
    {



        System.out.println( String.valueOf(easyChatList.size() + "  " + basicGroupsFullInfo.size() + "  " + supergroupsFullInfo.size()));


        StringBuilder sb = new StringBuilder();
        sb.append("Chats: \n");
        for (int i = 0; i < easyChatList.size(); i++) {
            sb.append(easyChatList.get(i).title).append("\n");
        }
        System.out.println(sb.toString());


        sb = new StringBuilder();
        sb.append("basicgroups: \n");
        Iterator iter = basicGroupsFullInfo.keySet().iterator();
        for (int i = 0; i < basicGroupsFullInfo.size(); i++) {
            sb.append(basicGroupsFullInfo.get(iter.next())).append("\n");
        }
        System.out.println(sb.toString());

        sb = new StringBuilder();
        sb.append("supergroups: \n");
        iter = supergroupsFullInfo.keySet().iterator();
        for (int i = 0; i < supergroupsFullInfo.size(); i++) {
            sb.append(supergroupsFullInfo.get(iter.next())).append("\n");
        }
        System.out.println(sb.toString());

    }

     */





    //endregion

}

