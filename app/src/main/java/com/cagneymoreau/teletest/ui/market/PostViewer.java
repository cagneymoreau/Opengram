package com.cagneymoreau.teletest.ui.market;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.data.Image;
import com.cagneymoreau.teletest.data.MarketController;
import com.cagneymoreau.teletest.data.MessageLocation;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

/**
 * View another sellers posting
 */
public class PostViewer extends Fragment {


    private static String TAG = "Posting";

    View fragment;

    MarketController marketController;

    TextView titleTv, descriptionTv, priceTv;

    //thumbnails for expanding
    HorizontalScrollView imgScroll;
    LinearLayoutCompat imgLayout;
    ArrayList<ImageView> images = new ArrayList<>();

    //in what chat was this posting posted
    HorizontalScrollView chatsScroll;
    LinearLayoutCompat chatsLayout;
    ArrayList<MessageLocation> chats;

    Button contactButton;

    ImageView sellerImage;

    Advertisement mAdvert;

    String advertID;

    TdApi.User seller;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.post_viewer, container, false);

        marketController = MarketController.getInstance((MainActivity) getActivity());

        advertID = getArguments().getString("posting", "new");

        connectUI();

        loadPostToView();

        return fragment;

    }


    private void connectUI() {
        titleTv = fragment.findViewById(R.id.post_viewer_title_textview);

        descriptionTv = fragment.findViewById(R.id.post_viewer_desc_textview);

        priceTv = fragment.findViewById(R.id.post_viewer_price_textview);

        imgScroll = fragment.findViewById(R.id.post_viewer_images_scrollview);
        imgLayout = fragment.findViewById(R.id.post_viewer_images_layout);

        chatsScroll = fragment.findViewById(R.id.post_viewer_chats_scrollview);
        chatsLayout = fragment.findViewById(R.id.post_viewer_chats_layout);

        contactButton = fragment.findViewById(R.id.post_viewer_contact_button);

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactSeller();
            }
        });

        sellerImage = fragment.findViewById(R.id.post_viewer_seller_imageview);

    }


    private void loadPostToView() {

        mAdvert = marketController.getItemFound(advertID);

        titleTv.setText(mAdvert.getTitle());

        descriptionTv.setText(mAdvert.getDescription());

        priceTv.setText(mAdvert.getAmount());

        for (int i = 0; i < mAdvert.imagesSize(); i++) {
            displayImage(i);
        }

        loadChannels();

        displaySeller();

    }



    // TODO: 1/17/2022 deleting an image thats not last creates indexing issue
    //called to add image to views
    private boolean displayImage(int pos)
    {
        Image im = mAdvert.getImage(pos);
        ImageView img = new ImageView(getContext());

        if (im != null){
            //im.displayImage(img, (MainActivity) getActivity());
            im.seekImageData(((MainActivity) getActivity()), img);

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)getActivity()).setDataBindingItem(im);
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_imageFull);
                }
            });

            images.add(img);

            imgLayout.addView(img);

            img.getLayoutParams().height = 300;
            img.getLayoutParams().width = 300;


        }

        return true;
    }


    private void loadChannels()
    {
        chats = mAdvert.getActualLocations();

        if (chats.size() == 0) return;

        for (int i = 0; i < chats.size(); i++) {

            TdApi.Chat c = ((MainActivity)getActivity()).getSpecificChat(chats.get(i).getChatId());

            ImageView im = new ImageView(fragment.getContext());

            final int p = i;

            if (c.photo!= null && !c.photo.small.local.path.isEmpty()) {
                String path = c.photo.small.local.path;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                im.setImageBitmap(bitmap);

                chatsLayout.addView(im);

            }else{

                TextView tv = new TextView(fragment.getContext());
                tv.setText(c.title);
                tv.setPadding(10,0,10,0);
                chatsLayout.addView(tv);

            }



        }

    }


    private void displaySeller()
    {
        seller = ((MainActivity)getActivity()).getUser((int) mAdvert.getSellerId());

        if (seller == null)
        {
            Log.e(TAG, "displaySeller: null seller", null);
            return;
        }

        Utilities.setUserAvater(seller, sellerImage, ((MainActivity) getActivity()));

    }


    private void contactSeller()
    {

        TdApi.Chat chat = ((MainActivity)getActivity()).getSpecificChat(seller.id);

        if (chat == null){

            ((MainActivity)getActivity()).createNewPrivateChat(seller.id, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR){

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                openChat(seller.id);
                            }
                        });

                    }else{
                        Log.e(TAG, "onResult: unable to get chat stared", null);
                        ((MainActivity)getActivity()).onBackPressed();
                    }
                }
            });

        }else {
                openChat(seller.id);

        }


    }

    private void openChat(long chatId)
    {
        Bundle b = new Bundle();
        long id = seller.id;
        b.putLong("chatId", id);
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.action_global_simpleChat, b);
    }


}
