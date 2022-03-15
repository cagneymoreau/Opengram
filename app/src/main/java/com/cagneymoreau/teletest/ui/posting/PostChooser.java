package com.cagneymoreau.teletest.ui.posting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.DialogSender;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.data.MarketController;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.RecyclerTouchListener;
import com.cagneymoreau.teletest.ui.Delete_Dialog;
import com.cagneymoreau.teletest.ui.Information_Dialog;
import com.cagneymoreau.teletest.ui.posting.recycler.AdvertisementAdapter;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * View your posts and events for editing etc
 */

public class PostChooser extends Fragment implements DialogSender {


    private final static String TAG = "PostChooser_fragment";

    View fragment;

    RecyclerView recyclerView;
    GridLayoutManager layoutManager;
    AdvertisementAdapter adapter;

    MarketController marketController;

    SortedList<Advertisement> myAdverts;

    DialogSender thisChooser = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.grid_layout, container, false);

        marketController = MarketController.getInstance((MainActivity) getActivity());

        myAdverts = marketController.getMyListings();

        buildRecycle();

        return fragment;
    }

    private void buildRecycle()
    {

        recyclerView = fragment.findViewById(R.id.gridlayout_recyclerView);
        layoutManager = new GridLayoutManager(getContext(), 4);
        adapter = new AdvertisementAdapter(myAdverts, ((MainActivity) getActivity()));
        marketController.setMyListingAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position, float x, float y) {




                Bundle b = new Bundle();
                if (position == 0){

                    if (marketController.getMessageLocationArrayList().size() == 0){
                        Toast.makeText(fragment.getContext(), "You havent created any marketplaces!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    b.putString("AdvertID", "new");
                }else{
                    b.putString("AdvertID", myAdverts.get(position-1).getId());
                }
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.posting_Fragment, b);
            }

            @Override
            public void onLongClick(View view, int position, float x, float y) {
                if (position != 0){
                    new Delete_Dialog("Delete this posting?", thisChooser,  1, position-1).show(getChildFragmentManager(), "delete dialog");
                }
            }
        }));

    }


    @Override
    public void setvalue(int i, TdApi.Chat c, int pos) {
        //always perform delete
        marketController.deleteOldPosts(myAdverts.removeItemAt(pos));

    }
}
