package com.cagneymoreau.teletest.ui.groupproj;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.RecyclerTouchListener;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.data.MarketController;
import com.cagneymoreau.teletest.data.MessageLocation;
import com.cagneymoreau.teletest.ui.market.MarketChatSpinnerAdapter;
import com.cagneymoreau.teletest.ui.market.recycle.MarketItemAdapter;

import java.util.ArrayList;

public class GroupProject extends Fragment implements SearchView.OnQueryTextListener {

private final static String TAG = "Market_fragment";

View fragment;

RecyclerView recyclerView;
GridLayoutManager layoutManager;
MarketItemAdapter adapter;

//Spinner itemEventSpinner,
    Spinner chatSpinner, categorySpinner;

MarketController marketController;

SortedList<Advertisement> itemsForSale;
ArrayList<MessageLocation> messageLocationArrayList;

ArrayList<Advertisement.Categories> list;

Advertisement.Categories everything;

//User search constraints
String queryString = "";
long queryChatId;
int queryEventorsale; //0 = both, 1 = forsale, 2 = event
Advertisement.Categories queryCategory;



@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    fragment = inflater.inflate(R.layout.market_fragment, container, false);

    ((MainActivity)getActivity()).setQueryListener(this);

    marketController = MarketController.getInstance((MainActivity) getActivity());

    itemsForSale = marketController.getItemsFound();
    messageLocationArrayList = new ArrayList<>();

    //this zero appears to be added for layout purposes but should be ok as its not tied to marketcontroller
    MessageLocation mess = new MessageLocation(0);
    messageLocationArrayList.add(mess);
    messageLocationArrayList.addAll(marketController.getMessageLocationArrayList());

    buildRecycle();

    buildSpinners();

    return fragment;
}

private void buildRecycle()
{
    recyclerView = fragment.findViewById(R.id.marketfragment_recyclerView);
    layoutManager = new GridLayoutManager(getContext(), 4);
    adapter = new MarketItemAdapter(itemsForSale, (MainActivity) getActivity());
    marketController.setItemsFoundAdapter(adapter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
        @Override
        public void onClick(View view, int position, float x, float y) {

            Bundle b = new Bundle();
            b.putString("posting", itemsForSale.get(position).getId());

            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_postViewer , b);

        }

        @Override
        public void onLongClick(View view, int position, float x, float y) {

        }
    }));

}


private void buildSpinners()
{


    chatSpinner = fragment.findViewById(R.id.market_chat_spinner);
    ArrayAdapter chAdapter = new MarketChatSpinnerAdapter(((MainActivity) getActivity()), messageLocationArrayList);
    chAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    chatSpinner.setAdapter(chAdapter);
    chatSpinner.setSelection(0, false);
    chatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            queryChatId = messageLocationArrayList.get(i).getChatId();
            filter();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    });

    list = Advertisement.getCategories();
    everything = new Advertisement.Categories();
    everything.setCategory("Everything");
    list.add(0, everything);
    categorySpinner = fragment.findViewById(R.id.market_category_spinner);
    ArrayAdapter catSpinner = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, list);
    catSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    categorySpinner.setAdapter(catSpinner);
    categorySpinner.setSelection(0,false);
    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            queryCategory = list.get(i);
            filter();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    });

}




    // TODO: 1/11/2022
    /**
     * This section filters the results when a spinner or text search has changed
     */
    //region -----  filter


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        queryString = s.toLowerCase();
        filter();

        return true;
    }

    /**
     * if a certain post doesn't meet a query constrint the loop is skipped and it is not added to the search results
     */
    private void filter()
    {
        ArrayList<Advertisement> filterList = new ArrayList<>();

        for (int i = 0; i < itemsForSale.size(); i++) {

            //searchbar
            if (!queryString.isEmpty()) {
                if (!itemsForSale.get(i).getTitle().toLowerCase().contains(queryString) && !itemsForSale.get(i).getDescription().toLowerCase().contains(queryString))
                    continue;
            }

            //category
            if (queryCategory != null) {
                if (!queryCategory.getCategory().equals(itemsForSale.get(i).getMycategory().getCategory()) && !queryCategory.getCategory().equals(everything.getCategory()))
                    continue;
            }

            //event,forsale,both
            if (queryEventorsale != 0){
                if (queryEventorsale == 1 && itemsForSale.get(i).getExpiration() != 0) continue;
                if (queryEventorsale == 2 && itemsForSale.get(i).getExpiration() == 0) continue;
            }

            //group
            if (!itemsForSale.get(i).postedHere(queryChatId)) continue;

            filterList.add(itemsForSale.get(i));
        }

        adapter.setSearchReturn(filterList);
    }


    //endregion

}
