package com.cagneymoreau.teletest.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;


import java.util.ArrayList;

// TODO: 1/17/2022 save data to the users saved messages chat

/**
 * Save the following
 *  1) list of chats where we want to have the store front option
 *  2) list of this users items for sale
 *  3) list of items other users are selling
 *  4) Store usage info such as timeline
 *
 *
 */

 public class SimpleSave {

     private static final String TAG = "SimpleSave";

    public static SimpleSave simpleSave;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private static final String KEY_CHATS = "keychats";
    private final static String KEY_MYLISTINGS = "keyadvert";
    private final static String KEY_ITEMSFOUND = "keyforsale";
    private final static String KEY_TODOLIST = "todo";



    private ArrayList<MessageLocation> marketchats = new ArrayList<>();
    private ArrayList<MessageLocation> todoList = new ArrayList<>();

    private SortedList<Advertisement> itemsFound;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> itemsFoundAdapter;

    private SortedList<Advertisement> mylistings;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mylistingsAdapter;


    public static SimpleSave getSimpleSave(Context context)
    {
        if (simpleSave == null){
            simpleSave = new SimpleSave(context);
        }

        return simpleSave;
    }


    private SimpleSave(Context context)
    {

        sharedPref = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        collectChats();
        collectMyListings();
        collectItemsFound();
        collectTimeLine();
        collectTodoList();

        Log.d(TAG, "SimpleSave: opened");

    }


    public void saveData()
    {
        saveChats();
        saveMyListings();
        saveItemsFound();
        saveTimeline();
        saveTodoList();
    }

    //region --- my for sale channel

    private static final String CHAN_INFO = "chanfo";

    public long getMyChannel()
    {
        return sharedPref.getLong(CHAN_INFO, 0);
        //return 0;
    }

    public void createMyChannel(long channelid)
    {
        editor.putLong(CHAN_INFO, channelid);
        editor.commit();
    }

    //endregion



    //region----------------- MYLISTINGS

    private void collectMyListings()
    {

        mylistings = new SortedList<>(Advertisement.class, new SortedList.Callback<Advertisement>() {
            @Override
            public int compare(Advertisement o1, Advertisement o2) {

                if (o1.date == o2.date) return 0;

                return o1.date > o2.date ? -1 : 1 ;


            }

            @Override
            public void onChanged(int position, int count) {
                if (mylistingsAdapter != null) {
                    mylistingsAdapter.notifyItemRangeChanged(position + 1, count);
                }
            }

            @Override
            public boolean areContentsTheSame(Advertisement oldItem, Advertisement newItem) {
                return  false;

            }

            @Override
            public boolean areItemsTheSame(Advertisement item1, Advertisement item2) {
                return  (item1.id.equals(item2.id));

            }

            @Override
            public void onInserted(int position, int count) {
                if (mylistingsAdapter != null) {
                    mylistingsAdapter.notifyItemRangeInserted(position, count);
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                if (mylistingsAdapter != null) {
                    mylistingsAdapter.notifyItemRangeRemoved(position + 1, count);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                if (mylistingsAdapter == null) return;
                mylistingsAdapter.notifyItemMoved(fromPosition, toPosition);
            }
        });



        String a = "begin";

        int key = 0;
        while (!a.equals("end"))
        {
            a = sharedPref.getString(KEY_MYLISTINGS + String.valueOf(key), "end");
            key++;
            if (!a.equals("end")){
                try {
                    mylistings.add(Advertisement.restoredAdvert(a));
                }catch (Exception e)
                {
                    Log.e(TAG, "collectMyListings: ", e);
                }
            }

        }
    }


    private void saveMyListings()
    {

        for (int i = 0; i < mylistings.size(); i++) {
            editor.putString(KEY_MYLISTINGS + i, mylistings.get(i).saveableAdvert(false));
        }
        editor.putString(KEY_MYLISTINGS + mylistings.size(), "end");
        editor.commit();
    }



    public  SortedList<Advertisement> getMylistings()
    {
        return mylistings;
    }


    public void setMylistingsAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> a)
    {
        mylistingsAdapter = a;
    }



    //endregion


    //region---------------------- marketchats




    private void collectChats()
    {
        String v = "begin";

        int key = 0;
        while (!v.equals("end"))
        {
            v = sharedPref.getString(KEY_CHATS + String.valueOf(key), "end");
            key++;
            if (!v.equals("end")){
                marketchats.add(new MessageLocation(v));
            }

        }
    }



    private void saveChats()
    {
        for (int i = 0; i < marketchats.size(); i++) {
            editor.putString(KEY_CHATS + i, marketchats.get(i).toString());
        }
        //if list is shortened, on opening, we could continue reading old data that isn't overwritten, this creates a boundary
        editor.putString(KEY_CHATS + marketchats.size(), "end");

        editor.commit();
    }

    public ArrayList<MessageLocation> getChatsList()
    {
        return marketchats;
    }



    //endregion


    //region-----------------------ItemsFound


    private void collectItemsFound()
    {

        itemsFound = new SortedList<>(Advertisement.class, new SortedList.Callback<Advertisement>() {
            @Override
            public int compare(Advertisement o1, Advertisement o2) {
                return 0;
            }

            @Override
            public void onChanged(int position, int count) {
                if (itemsFoundAdapter != null) {
                    itemsFoundAdapter.notifyItemRangeChanged(position, count);
                }
            }

            @Override
            public boolean areContentsTheSame(Advertisement oldItem, Advertisement newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(Advertisement item1, Advertisement item2) {
                return  (item1.id.equals(item2.id));
            }

            @Override
            public void onInserted(int position, int count) {
                if ( itemsFoundAdapter == null) return;
                        itemsFoundAdapter.notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                if (itemsFoundAdapter != null) {
                    itemsFoundAdapter.notifyItemRangeChanged(position, count);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                if (itemsFoundAdapter == null) return;
                itemsFoundAdapter.notifyItemMoved(fromPosition, toPosition);
            }
        });


        String v = "begin";

        int key = 0;
        while (!v.equals("end"))
        {
            v = sharedPref.getString(KEY_ITEMSFOUND + String.valueOf(key), "end");
            key++;
            if (!v.equals("end")){
                try {
                    itemsFound.add(Advertisement.restoredAdvert(v));
                }catch (Exception e)
                {
                    Log.e(TAG, "collectItemsFound: ", e);
                }
            }

        }
    }



    private void saveItemsFound()
    {
        for (int i = 0; i < itemsFound.size(); i++) {

            itemsFound.get(i).clearActuaLocations();

            editor.putString(KEY_ITEMSFOUND + i, itemsFound.get(i).saveableAdvert(false));
        }
        editor.putString(KEY_ITEMSFOUND + itemsFound.size(), "end");
        editor.commit();
    }

    public SortedList<Advertisement> getItemsFoundList()
    {
        return itemsFound;
    }



    public void setItemsFoundAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> a)
    {
        itemsFoundAdapter = a;
    }



    //endregion


    //region ----------------------- TODOLIST



    private void collectTodoList()
    {
        String v = "begin";

        int key = 0;
        while (!v.equals("end"))
        {
            v = sharedPref.getString(KEY_TODOLIST + String.valueOf(key), "end");
            key++;
            if (!v.equals("end")){
                todoList.add(new MessageLocation(v));
            }

        }
    }



    private void saveTodoList()
    {
        for (int i = 0; i < todoList.size(); i++) {
            editor.putString(KEY_TODOLIST + i, todoList.get(i).toString());
        }
        //if list is shortened, on opening, we could continue reading old data that isn't overwritten, this creates a boundary
        editor.putString(KEY_TODOLIST + todoList.size(), "end");

        editor.commit();
    }

    public ArrayList<MessageLocation> getTodoList()
    {
        return todoList;
    }


    //endregion

    /**
     * Really just used for paywall
     */
    //region ----- timeline

    private final static String KEY_TIMELINE = "keytimeline";

    private static final long trial = 6L * 30L * 24L * 60L * 60L * 1000L;

    private static final long annoyDelay = 24L * 60L *60L * 1000L;

    private long firstLogin;

    private long lastAnnoy;

    private long paidTill;


    private void collectTimeLine()
    {
        firstLogin = sharedPref.getLong(KEY_TIMELINE + "_0", System.currentTimeMillis());

         lastAnnoy = sharedPref.getLong(KEY_TIMELINE + "_1", System.currentTimeMillis());

         paidTill = sharedPref.getLong(KEY_TIMELINE + "_2", System.currentTimeMillis());

    }

    private void saveTimeline()
    {
        editor.putLong(KEY_ITEMSFOUND + "_0", firstLogin);

        editor.putLong(KEY_ITEMSFOUND + "_1", lastAnnoy);

        editor.putLong(KEY_ITEMSFOUND + "_2", paidTill);

        editor.commit();
    }

    /**
     * App will not have access restrictions for now
      * @return
     */
    public boolean isAcessGranted()
    {
        long now = System.currentTimeMillis();

        if (now < (firstLogin + trial)) return true;
        return now < paidTill;
    }

    public boolean shouldAnnoyDisplay()
    {
        if (System.currentTimeMillis() < (lastAnnoy + annoyDelay))return false;

        lastAnnoy = System.currentTimeMillis();
        return true;
    }


    public void paidForYear()
    {
            paidTill = System.currentTimeMillis() + 365L * 24L * 60L * 60L * 1000L;
    }


    //endregion



}
