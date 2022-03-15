package com.cagneymoreau.teletest.data;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Interacts with api to controll advertisment objects representing our and other postings
 * This class gets/sets posts from/to the various chats
 * data persistence through simplesave
 *
 * // FIXME: 2/17/2022 adding new channel needs single search
 * // FIXME: 2/17/2022 remove channels that arent a market
 */

public class MarketController {

    private static final String TAG = "MarketController";

    public static MarketController marketController;


    SimpleSave simpleSave;
    MainActivity mActivity;
    Context context;


    final private SortedList<Advertisement> myListings;
    final private SortedList<Advertisement> itemsFound;
    //which channels i have selected to be a market
    ArrayList<MessageLocation> messageLocationArrayList;
    ArrayList<MessageLocation> todoList;

    long myChannelId;

    //searching for advert
    Client.ResultHandler messageRecieiver;
    ArrayList<TdApi.Message> messageList = new ArrayList<>();
    long offsetChatId = 0;
    long offsetMessId = 0;
    int previousUpdate;
    int maxDate;
    //prevent running the gather algo simultaneously. if new channel is added then it will be added to end and performed anyways
    boolean gathering = false;


    int countToPost = 0;


    //for posting an advert
    Advertisement advertisementPostable;
    ArrayList<Pair<String, Integer>> attempted;
    ArrayList<TdApi.Message> returned;
   int dateFortimeout;

    private int cleanCounter = 0;


    //region --------------  object management

    public static synchronized MarketController getInstance(MainActivity activity)
    {
        if (marketController == null){
            marketController = new MarketController(activity);
        }
        return marketController;
    }


    private MarketController(MainActivity activity)
    {
        mActivity = activity;
        this.context = activity.getApplicationContext();
        simpleSave = SimpleSave.getSimpleSave(context);

        myListings = simpleSave.getMylistings();
        itemsFound = simpleSave.getItemsFoundList();
        messageLocationArrayList = simpleSave.getChatsList();
        todoList = simpleSave.getTodoList();
        previousUpdate = ((int) System.currentTimeMillis()/1000) - (90*24*60*60);
        int maxupdate = 0;

        myChannelId = simpleSave.getMyChannel();


        firstStartChannelCheck();


        confirmListExisting(myListings);

        //When app start you must wait on callbacks to verify if adds still exist
        //this value tracks how many callbacks are waiting so you can call cleanup once they have all returned
        cleanCounter += 1000;
        confirmListExisting(itemsFound);
        cleanCounter -= 1000;


        gatherUpdates();


    }

    public void onPause()
    {
        simpleSave.saveData();
    }


    //endregion


    //region ---------------- new messagehandling

    /**
     * Method determines if this message is from this app and need processing
     */
    public void unkownLastMessageSorter(TdApi.Message message)
    {
        if (message.chatId == getMyChannelId()) {
            postToMyChannel(message);
        }
        //its a market chat
        else if(isMarketAlready(message.chatId))
        {
            //route based on who posted it
            if (message.sender.getConstructor() == TdApi.MessageSenderChat.CONSTRUCTOR) return;
            TdApi.MessageSenderUser user = (TdApi.MessageSenderUser) message.sender;
            if (user.userId == mActivity.getMyId())postFromMyself(message);
            else postFromOtherUser(message);

            // TODO: 2/4/2022 this should hav worked but didnt!!
            /*
            //its a file...could be an api post
            if (message.content.getConstructor() == TdApi.MessageDocument.CONSTRUCTOR) {

                TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) message.content;

                //its an api post
                TdApi.FormattedText text = messageDocument.caption;
                if (!text.text.equals(mActivity.getResources().getString( R.string.message_text)))return;

            }

             */

        }
    }

    /**
     * 1)picture message
     * @param message
     */
    private void postToMyChannel(TdApi.Message message)
    {
        if (message.content.getConstructor() == TdApi.MessagePhoto.CONSTRUCTOR){

            if (advertisementPostable != null && message.sendingState == null) {
                collectEmbeddings(message);
            }
        }
    }

    private void postFromMyself(TdApi.Message message)
    {
            //we are posting our listing to this channel
        if (advertisementPostable == null || message.sendingState != null) return;// we are not attempting to post so bail
            verifyPost(message);
            postFromOtherUser(message); //our adds should show up in the for sale section as well

    }

    private void postFromOtherUser(TdApi.Message message)
    {

        if (message.content.getConstructor() == TdApi.MessageDocument.CONSTRUCTOR) {

            TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) message.content;

            //its an api post
            TdApi.FormattedText text = messageDocument.caption;
            if (!text.text.contains(mActivity.getResources().getString( R.string.message_text)))return;


            ArrayList<TdApi.Message> m = new ArrayList<TdApi.Message>();
            m.add(message);
            downloadFoundMessages(m);

        }



    }


    //endregion


    //region ----------------  list database sync handling

    public void confirmListExisting(SortedList<Advertisement> mList)
    {
        for (int i = 0; i < mList.size(); i++) {
            final Advertisement a = mList.get(i);
            a.clearFoundLocations();
            confirmAdvertsExisting(a);
        }
    }

    public void confirmAdvertsExisting(Advertisement a)
    {
        for (int j = 0; j < a.getActualLocations().size(); j++) {
            final MessageLocation m = a.getActualLocations().get(j);

            //if we left this chat then don't add to list of found
            if (!isMarketAlready(m.getChatId())) continue;

            cleanCounter++;

            mActivity.getMessage(m.chat_id, m.lastMessageIndexedId, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (TdApi.Message.CONSTRUCTOR == object.getConstructor()){
                        TdApi.Message m = (TdApi.Message) object;
                       a.addFoundLocations(m);

                    }
                    decrementCleanCounter();
                }
            });

        }

    }


    //remove old ads or ads that have been completely deleted
    public void cleanList(SortedList<Advertisement> mList)
    {
        ArrayList<Integer> delete = new ArrayList<>();

        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).expired() || mList.get(i).revoked()) delete.add(i);
        }

        for (int i = delete.size()-1; i >= 0; i--) {
            mList.removeItemAt(delete.get(i));
        }
    }

    private synchronized void decrementCleanCounter()
    {
        cleanCounter--;

        if (cleanCounter == 0){
            cleanList(itemsFound);
        }

    }


    //endregion


    //region ------------ ITEMSFOUND

    //call to start search
    private void gatherUpdates()
    {
        synchronized (messageLocationArrayList) {

            if (gathering) {
                Log.e(TAG, "gatherUpdates: ", null);
                return;
            }
            gathering = true;

            if (messageRecieiver == null) {
                buildMessageReciever();
            }
            //on firt use this will be null
            if (messageLocationArrayList.size() > 0) {
                collectMessages();
            } else {
                gathering = false;
            }
        }
    }


    private void buildMessageReciever()
    {

        messageRecieiver = new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {


                //collect various errors here
                if (object.getConstructor() != TdApi.Messages.CONSTRUCTOR){
                    Log.d(TAG, "onResult: " + object.toString());
                    return;
                }

                TdApi.Messages messages = (TdApi.Messages) object;

                if (messages.messages != null) {

                    messageList.addAll(Arrays.asList(messages.messages));

                    //these calls return true if we need to continue scanning this channel
                    if ( messages.messages.length != 0){

                        TdApi.Message message = messageList.get(messageList.size()-1);
                        offsetChatId = message.chatId;
                        offsetMessId = message.id;
                        maxDate = message.date;
                        collectMessages();

                    }
                    //were done
                    else{
                        Log.d(TAG, "onResult: DONE SORTING ln 165");

                        downloadFoundMessages(messageList);
                        messageList.clear();
                        gathering = false;
                        offsetMessId = 0L;
                        offsetChatId = 0L;
                        maxDate = 0;
                    }

                }else{

                    Log.e(TAG, "onResult: unexpected error", null );
                }

            }
        };

    }

    private void collectMessages()
    {
        String tag = mActivity.getResources().getString(R.string.message_text);


        mActivity.getItemsFoundFromChat(tag, offsetChatId, offsetMessId, 32, previousUpdate, maxDate,  messageRecieiver);


    }


    /**
     * so basically we need to start downloading by passing file.id then
     * we get a feedback in onresult letting us know its ready and we can attempt to process it
     * by accessing it through local.whatever
     * @param m
     */
    public synchronized void downloadFoundMessages(ArrayList<TdApi.Message> m)
    {
        for (int i = 0; i < m.size(); i++) {

            final TdApi.Message singleMess = m.get(i);

            //item from group we are not part of
            if(!isMarketAlready(singleMess.chatId)) continue;

            //get string from file
            if (singleMess.content.getConstructor() != TdApi.MessageDocument.CONSTRUCTOR){
                Log.e(TAG, "sortFoundMessages: SEARCH FOUND NON DOCUMENT ", null);
                continue;
            }

            TdApi.MessageDocument doc = (TdApi.MessageDocument) singleMess.content;

            int id = doc.document.document.id;

            mActivity.downloadFile(id, 1, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    synchronized (itemsFound) {

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (TdApi.File.CONSTRUCTOR == object.getConstructor()) {
                                    TdApi.File updateFile = (TdApi.File) object;
                                    Advertisement a = getAdvertFromPath(updateFile.local.path);

                                    //make sure the user who posted the message matches the advert
                                    if (!a.isAuthentic(singleMess)) return;

                                    int p = itemsFound.indexOf(a);
                                    if (p == -1) {
                                        //its new
                                        a.addActualLocation(singleMess);
                                        itemsFound.add(a);
                                    } else {
                                        //replace with newest
                                        if (itemsFound.get(p).getMostRecentUpdate() < a.getMostRecentUpdate()) {
                                            a.addActualLocation(singleMess);
                                            itemsFound.add(a);
                                        }
                                        //its a match, add to actual location
                                        if (itemsFound.get(p).getMostRecentUpdate() == a.getMostRecentUpdate()) {
                                            itemsFound.get(p).addActualLocation(singleMess);
                                        }

                                    }
                                }



                            }
                        });


                    }
                }
            });


        }


    }

    private Advertisement getAdvertFromPath(String path)
    {
        //open local messageFile
        //get string from local file

        ArrayList<String> lines = new ArrayList<>();

        try{
            FileReader fr = new FileReader(path);

            BufferedReader reader = new BufferedReader(fr);

            lines.add(reader.readLine());
            while ((lines.get(lines.size()-1)) != null){
                lines.add(reader.readLine());
            }

            //remove last null
            lines.remove(lines.size()-1);

            reader.close();

        }catch (Exception e){

            Log.e(TAG, "putFileIntoList: ", e);
        }

        //convert to advert
        //check which is most recent update and add if this one is newer
        return Advertisement.buildAdvert(lines);

    }

    public SortedList<Advertisement> getItemsFound()
    {
        return itemsFound;
    }

    public void setItemsFoundAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> a)
    {
        simpleSave.setItemsFoundAdapter(a);
    }


    public Advertisement getItemFound(String uniqueId)
    {
        synchronized (itemsFound) {
            int p = itemsFound.indexOf(new Advertisement(uniqueId));

            return itemsFound.get(p);
        }

    }

    //endregion


    //region---------------------- MYLISTINGS

    public void setMyListingAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> a)
    {
        simpleSave.setMylistingsAdapter(a);
    }

    public SortedList<Advertisement> getMyListings()
    {
        return myListings;
    }



    public Advertisement getListingToEdit(String id)
    {
        for (int i = 0; i < myListings.size(); i++) {
            if (myListings.get(i).id.equals(id)) return myListings.get(i);
        }

        return null;
    }



    public void addMyListing(Advertisement advertisement)
    {
        myListings.add(advertisement);
    }

    /**
     * This should delete old advert and post new
     */
    public void postToChannels(Advertisement advertisement)
    {
        advertisementPostable = advertisement;
        attempted = new ArrayList<>();
        returned = new ArrayList<>();


        deleteOldPosts(advertisement);

        countToPost = 0;
        for (int i = 0; i < advertisement.imagesSize(); i++) {
            if (advertisement.getImage(i).messagePhoto == null) {
                countToPost++;  }
        }

        if (countToPost > 0) uploadNewImages(advertisement);
        else postToEachChannel(advertisement);

    }

    public void deleteOldPosts(Advertisement advertisement)
    {
        Log.d(TAG, "deleteOldPosts: " + advertisement.getTitle());
        //delete old posts
        for (int i = 0; i < advertisement.getActualLocations().size(); i++) {

            mActivity.deleteMessage(advertisement.getActualLocations().get(i).chat_id, new long[] {advertisement.getActualLocations().get(i).lastMessageIndexedId}, null);

        }
        advertisement.clearActuaLocations();

    }

    public void deleteSinglePost(Advertisement advertisement, long chanID){

        for (int i = 0; i < advertisement.getActualLocations().size(); i++) {
            if (advertisement.getActualLocations().get(i).chat_id == chanID) {
                mActivity.deleteMessage(advertisement.getActualLocations().get(i).chat_id, new long[]{advertisement.getActualLocations().get(i).lastMessageIndexedId}, null);
            }
        }

    }


    private void uploadNewImages(Advertisement advertisement)
    {
        Log.d(TAG, "uploadNewImages: " + advertisement.getTitle());
        //post images so we have html links
        TdApi.MessageSendOptions picOptions = new TdApi.MessageSendOptions(true, false, new TdApi.MessageSchedulingStateSendAtDate(1) );


        for (int i = 0; i < advertisement.imagesSize(); i++) {

            if (advertisement.getImage(i).getHostedFilehtml() == null){

                TdApi.InputMessagePhoto photoContent = new TdApi.InputMessagePhoto();
                // TODO: 1/18/2022 remove this text
                TdApi.FormattedText text = new TdApi.FormattedText();
                final String match_ID = advertisement.getTitle() + "_" + i;
                text.text = match_ID;
                attempted.add(new Pair<>(match_ID, i));
                photoContent.caption = text;

                TdApi.InputFileLocal pFile = new TdApi.InputFileLocal();
                pFile.path = advertisement.getImage(i).getLocalFile();
                photoContent.photo = pFile;

                final int pos = i;
                //must use new result handler so we can pass a position flag to each
                mActivity.sendPosting(marketController.myChannelId, 0, picOptions, new TdApi.ReplyMarkupRemoveKeyboard(), photoContent, new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {

                        // TODO: 1/24/2022 we may have an error here, or message failed to send in the mainactvoty updates. both need to be handled



                        if (object.getConstructor() == TdApi.Message.CONSTRUCTOR){
                            //TdApi.Message m = (TdApi.Message) object;
                            //Image i = advertisement.getImage(pos);
                            //i.setMessage(m);

                        }else{
                            //some type of error
                            Log.e(TAG, "onResult: pic post error", null);
                            TdApi.Message m = new TdApi.Message();
                            TdApi.MessagePhoto p = new TdApi.MessagePhoto();
                            TdApi.FormattedText text = new TdApi.FormattedText();
                            text.text = match_ID;
                            p.caption = text;
                            m.content = p;
                            collectEmbeddings(m);
                        }
                    }
                });
            }
        }
    }

    //get the photos storage info
    public void collectEmbeddings(TdApi.Message message)
    {
        Log.d(TAG, "collectEmbeddings: " + advertisementPostable.getTitle());
        //builds up a list in this first section
        if (attempted == null || attempted.size() < 1) return;

            TdApi.MessagePhoto pIn = (TdApi.MessagePhoto) message.content;
            TdApi.FormattedText tIn = (TdApi.FormattedText) pIn.caption;
            int pos = -1;
            for (int i = 0; i < attempted.size(); i++) {
                if (tIn.text.equals(attempted.get(i).first)){
                    pos = i;
                }
            }

        if (pos != -1) returned.add(message);

        if (returned.size() < attempted.size()) return;

        //make request to api
        for (int i = 0; i < returned.size(); i++) {

            int position = -1;
            final TdApi.Message mess = returned.get(i);
            final TdApi.MessagePhoto p = (TdApi.MessagePhoto) mess.content;
            TdApi.FormattedText t = (TdApi.FormattedText) p.caption;
            for (int j = 0; j < attempted.size(); j++) {

                if (t.text.equals(attempted.get(j).first)){
                    position = j;
                }
            }
            if (position == -1) {
                Log.e(TAG, "collectEmbeddings: didnt find", null );
                return;
            }

            final Pair<String, Integer> att = attempted.remove(position);


            mActivity.getEmbeddingCode( mess.chatId, mess.id, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() == TdApi.Text.CONSTRUCTOR){

                        TdApi.Text text = (TdApi.Text) object;

                        if (p.photo != null) {

                            advertisementPostable.getImage(att.second).setMessage(mess);
                            advertisementPostable.getImage(att.second).hostedFilehtml = text.text;
                            advertisementPostable.getImage(att.second).tempfile = null;

                        }

                    }else{
                        Log.e(TAG, "onResult: collect embeddings" + object.toString(), null );
                    }

                    countToPost--;
                    postToEachChannel(advertisementPostable);
                }
            });
        }
    }


    // post message to appropraite channels
    private void postToEachChannel(Advertisement advertisement)
    {
        Log.d(TAG, "postToEachChannel: -2 " + advertisement.getTitle());

        if (attempted.size() != 0 || countToPost != 0){
            return;
        }

        Log.d(TAG, "postToEachChannel: -1 " + advertisement.getTitle());

        for (int i = 0; i < advertisement.getProposedLocationsSize(); i++) {

            Log.d(TAG, "postToEachChannel: " + i + " " + advertisement.getTitle());


            TdApi.MessageSendOptions options = new TdApi.MessageSendOptions(true, false, new TdApi.MessageSchedulingStateSendAtDate(1) );

            TdApi.InputMessageDocument content = new TdApi.InputMessageDocument();

            String messageText = mActivity.getResources().getString(R.string.message_text) + "\n\n" + advertisement.mycategory.getCategory() + "\n\n" + advertisement.getTitle();

            content.caption = new TdApi.FormattedText( messageText, new TdApi.TextEntity[0]);

            TdApi.InputFileLocal file = new TdApi.InputFileLocal();

            File f = createTempHtmlFile(advertisement.generateHTML(), advertisement.getTitle());

            if (f == null){
                Log.e(TAG, "postToEachChannel: fail", null);
            }

            file.path = f.getPath();

            content.document = file;



            mActivity.sendPosting(advertisement.getProposedLocation(i).chat_id, 0L, options, new TdApi.ReplyMarkupRemoveKeyboard(), content, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    // save the its recorded here

                    if (object.getConstructor() == TdApi.Message.CONSTRUCTOR){

                        TdApi.Message m = (TdApi.Message) object;
                       dateFortimeout = m.date;


                    }else{
                        Log.e(TAG, "onResult: " + object.toString(), null );
                    }



                }
            });

        }
    }

    //a post has been verified by server
    private synchronized void verifyPost(TdApi.Message message)
    {
        // sometimes we get empty messages here so then post is not verified
        //also if user delects a store channel. the app will delete it and if its the last post preceeded by another of our posts a last message for channel post will arrive here for wrong posting

        if (message.date > (dateFortimeout + 100)) return;

            advertisementPostable.addActualLocation(message);
            advertisementPostable.addFoundLocations(message);

            if (advertisementPostable.getProposedLocationsSize() == advertisementPostable.getActualLocations().size()) {

                //we have posted to all locations so clear fields
                //myListings.remove(advertisementPostable);
                myListings.add(advertisementPostable);
                Log.d(TAG, "verifyPost: " + advertisementPostable.generateHTML());
                advertisementPostable = null;
                simpleSave.saveData();

            }



    }

    public void deleteThisPosting(Advertisement advertisement)
    {
        myListings.remove(advertisement);

        deleteOldPosts(advertisement);
    }



    /**
     * removes all postsings from this channel
     * @param chanId
     */
    public void leaveChannel(long chanId)
    {

        for (int i = 0; i < myListings.size(); i++) {
           deleteSinglePost(myListings.get(i), chanId);
           myListings.get(i).leaveChannel(chanId);
        }
    }

    //endregion


    //region ------------------- marketchats

    public boolean isMarketAlready(long id)
    {
        for (int i = 0; i < messageLocationArrayList.size(); i++) {
            if (messageLocationArrayList.get(i).chat_id == id || messageLocationArrayList.get(i).old_chatId == id) return true;

        }
        return false;
    }


    public void addChat(long l)
    {
        synchronized (messageLocationArrayList) {

            MessageLocation m = new MessageLocation(l);

            long v = mActivity.checkForBasicGroup(l);
            if (v > 0) v= v*-1;
            if (v != 0) m.old_chatId = v;

            messageLocationArrayList.add(m);

            gatherUpdates();
        }
    }

    public void deleteChat(long l)
    {
        synchronized (messageLocationArrayList) {

            leaveChannel(l);

            int index = -1;
            for (int i = 0; i < messageLocationArrayList.size(); i++) {
                if (messageLocationArrayList.get(i).chat_id == l) {
                    index = i;
                    break;
                }
            }
            messageLocationArrayList.remove(index);
        }
    }

    //when an alert comes in live or a log in happens and a group changed we need to make sure that the new supergroup id is added as well
    public void checkForUpgrade(long currentBasic, long basicNew)
    {
            synchronized (messageLocationArrayList) {

                for (int i = 0; i < messageLocationArrayList.size(); i++) {
                    if (messageLocationArrayList.get(i).chat_id == currentBasic){
                        messageLocationArrayList.get(i).old_chatId = currentBasic;
                        messageLocationArrayList.get(i).chat_id = basicNew;
                        break;
                    }
                }
            }
    }

    public   ArrayList<MessageLocation> getMessageLocationArrayList()
    {
        return messageLocationArrayList;
    }

    //endregion


    //region ----- mychannel

    Client.ResultHandler channHandler;

    public void buildMyChannel()
    {


            channHandler = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    //channel made
                    if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR){
                        TdApi.Chat s = (TdApi.Chat) object;
                        myChannelId = s.id;
                        simpleSave.createMyChannel(myChannelId);

                        TdApi.ChatTypeSupergroup gr = (TdApi.ChatTypeSupergroup) s.type;

                        //make public
                        mActivity.makeMyChannelpublic( gr.supergroupId, channHandler);
                    }
                    else if (object.getConstructor() == TdApi.Message.CONSTRUCTOR){
                        //message posted. lets pin it
                        //TdApi.Message m = (TdApi.Message) object;

                        //mActivity.pinMessage(m.chatId, m.id, null);

                    }
                    else if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR){


                        //channel is public
                        TdApi.MessageSendOptions options = new TdApi.MessageSendOptions(true, false, new TdApi.MessageSchedulingStateSendAtDate(1) );

                        TdApi.InputMessageText textMess = new TdApi.InputMessageText();

                        TdApi.FormattedText textC = new TdApi.FormattedText();
                        textC.text = "This channel will hold all the hosted photos or other app data. Do not post here and do not delete any content or you may disrupt the app functioning properly";

                        textMess.text = textC;

                        //post message explaining channel
                        mActivity.sendPosting(myChannelId, 0L, options, new TdApi.ReplyMarkupRemoveKeyboard(), textMess, channHandler);

                        Log.d(TAG, "onResult: ");
                    }else{
                        Log.e(TAG, "onResult: failed to make channel" + object.toString(), null );
                    }

                }
            };

            mActivity.createChannel(channHandler);



    }

    private void firstStartChannelCheck()
    {
        if (myChannelId == 0){
            buildMyChannel();
        }
    }

    public long getMyChannelId()
    {
        return myChannelId;
    }

    //endregion



    //region ------------todolist

    public ArrayList<MessageLocation> getToDoList()
    {
        return todoList;
    }

    public void addTodoListItem(long chatid, long messageid)
    {
        MessageLocation m = new MessageLocation(chatid, messageid);
        todoList.add(m);
    }


    //endregion




    // TODO: 1/21/2022 move this into simplesave method
    private File createTempHtmlFile( String html, String title)
    {
        try {

            File out = mActivity.getCacheDir();
            File outputFile = File.createTempFile(title, ".html", out);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] data = html.getBytes(StandardCharsets.UTF_8);
            outputStream.write(data);
            outputStream.close();

            return outputFile;

        }catch (Exception e)
        {
            Log.e(TAG, "createTempHtmlFile: ", e);
        }

        return null;
    }







}
