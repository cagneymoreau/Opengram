package com.cagneymoreau.teletest.ui.posting;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.cagneymoreau.teletest.NumberTextWatcher;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Advertisement;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.Image;
import com.cagneymoreau.teletest.data.MessageLocation;
import com.cagneymoreau.teletest.data.MarketController;

import org.drinkless.td.libcore.telegram.TdApi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * Build or edit a post
 * // TODO: 1/18/2022 force data fill or app will crash 
 *
 */

public class Posting extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static String TAG = "Posting";

    View fragment;

    MarketController marketController;

    TextView titleTv, descriptionTv, priceTv;
    EditText titleEt, descriptionEt, priceEt;

    SwitchCompat eventSwitch;
    Spinner categorySpinner;

    HorizontalScrollView imgScroll;
    LinearLayoutCompat imgLayout;
    ArrayList<ImageView> images = new ArrayList<>();

    ScrollView chatsScroll;
    LinearLayoutCompat chatsLayout;


    ArrayList<MessageLocation> listOfOptions;
    ArrayList<MessageLocation> chosenChats = new ArrayList<>();


    Button updateButton, deleteButton;

    Advertisement mAdvert;

    String advertID;

    boolean changesDetected = false;

    boolean creation = false;

   Posting thisPosting = this;

   int year, month, day, hour, minute;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.post_edit, container, false);

        marketController = MarketController.getInstance((MainActivity) getActivity());

        advertID = getArguments().getString("AdvertID", "new");

        connectUI();

        if (advertID.equals("new")) {
            loadNew();
        } else {
            loadEdit();
        }


        return fragment;
    }


    private void connectUI() {
        titleTv = fragment.findViewById(R.id.post_edit_title_textview);
        titleEt = fragment.findViewById(R.id.post_edit_title_editText);

        descriptionTv = fragment.findViewById(R.id.post_edit_desc_textview);
        descriptionEt = fragment.findViewById(R.id.post_edit_desc_editText);

        priceTv = fragment.findViewById(R.id.post_edit_price_textview);
        priceEt = fragment.findViewById(R.id.post_edit_price_editText);
        priceEt.addTextChangedListener(new NumberTextWatcher(priceEt));

        eventSwitch = fragment.findViewById(R.id.post_edit_marketorevent_switch);
        eventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                changesDetected = true;
                if (b){

                    categorySpinner.setSelection(1);

                    Calendar c = Calendar.getInstance();
                     year = c.get(Calendar.YEAR);
                     month = c.get(Calendar.MONTH);
                     day = c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePicker = new DatePickerDialog(getContext(),thisPosting, year,month, day );
                        datePicker.show();
                }else{
                    mAdvert.setExpiration(0);
                    eventSwitch.setText("Event with date");
                }

            }
        });

        categorySpinner = fragment.findViewById(R.id.posting_category_spinner);
        ArrayAdapter catSpinner = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, Advertisement.getCategories());
        catSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catSpinner);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changesDetected = true;
                if (mAdvert.getExpiration() != 0){
                    categorySpinner.setSelection(1);
                    Toast.makeText(getContext(), "Unselect the time picker to use normal categories", Toast.LENGTH_LONG).show();
                }
                if (i == 1){
                    eventSwitch.setChecked(true); // TODO: 1/31/2022 does this activate method?
                }
                mAdvert.setMycategory(Advertisement.getCategories().get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imgScroll = fragment.findViewById(R.id.post_edit_images_scrollview);
        imgLayout = fragment.findViewById(R.id.post_edit_images_layout);

        imageloaderSetup();

        chatsScroll = fragment.findViewById(R.id.post_edit_chats_scrollview);
        chatsLayout = fragment.findViewById(R.id.post_edit_chats_layout);
        
        updateButton = fragment.findViewById(R.id.post_edit_update_button);
        deleteButton = fragment.findViewById(R.id.post_edit_delete_button);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAdvert();
            }
        });


    }


    private void loadNew() {

        creation = true;

        long l = ((MainActivity)getActivity()).getMyId();

        if (l == 0){
            // TODO: 1/4/2022 implement a rerequest
            //missing data. abort
            ((MainActivity)getActivity()).onBackPressed();
        }

        mAdvert = new Advertisement(l);

        deleteButton.setVisibility(View.INVISIBLE);
        updateButton.setText("Post");

        loadChannels();

    }


    private void loadEdit() {
        mAdvert = marketController.getListingToEdit(advertID);

        titleEt.setText(mAdvert.getTitle());
        descriptionEt.setText(mAdvert.getDescription());
        priceEt.setText(mAdvert.getAmount());

        for (int i = 0; i < mAdvert.imagesSize(); i++) {
            displayImage(i);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marketController.deleteThisPosting(mAdvert);
                getActivity().onBackPressed();

            }
        });

        loadChannels();

        updateButton.setText("Update");
    }


    private void saveAdvert()
    {
        if (marketController.getMessageLocationArrayList().size() == 0){
            Toast.makeText(fragment.getContext(), "You cant update without marketplaces!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleEt.getText().toString();

        if (title.length() < 3){
            Toast.makeText(getContext(), "Title must be longer", Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = descriptionEt.getText().toString();
        String amnt = priceEt.getText().toString();

        if (!mAdvert.getTitle().equals(title)){
            mAdvert.setTitle(title);
            changesDetected = true;
        }


        if (!mAdvert.getDescription().equals(desc))
        {
            mAdvert.setDescription(desc);
            changesDetected = true;
        }



        if (!mAdvert.getAmount().equals(amnt)){
            mAdvert.setAmount(amnt);
            changesDetected = true;
        }


        //change channel posted too
       if (checkIfLocationsChanged()){
           changesDetected = true;
       }

        //images are loaded and changed in real time
        //change the add if any change is detected. dont try and compare

        mAdvert.setMostRecentUpdate(System.currentTimeMillis());
        mAdvert.clearFoundLocations();


        if (creation){
            marketController.addMyListing(mAdvert);
        }

        //post to appropriate locations
        if (changesDetected) {
            marketController.postToChannels(mAdvert);
            getActivity().onBackPressed();
        }else {
            Toast.makeText(getContext(), "Nothing Changed", Toast.LENGTH_SHORT).show();
        }

    }

        
        //region -------  images

        private void imageloaderSetup() {

            ImageView v = fragment.findViewById(R.id.post_edit_adder_imageview);
            Drawable cam =  getResources().getDrawable(R.drawable.ic_baseline_add_a_photo_24, getActivity().getTheme());
            v.setImageDrawable(cam);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addImage();
                }
            });
            images.add(v);
            

        }

    // TODO: 1/17/2022 deleting an image thats not last creates indexing issue
        //called to add image to views
        private boolean displayImage(int pos)
        {
            Image im = mAdvert.getImage(pos);
            ImageView img = new ImageView(getContext());

            if (im != null){

                im.displayImage(img, (MainActivity) getActivity());
                //im.seekImageData(((MainActivity) getActivity()), img);

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeImage(mAdvert.getImage(pos), img);
                    }
                });

                images.add(img);

                imgLayout.addView(img);

                img.getLayoutParams().height = 300;
                img.getLayoutParams().width = 300;
            }

            return true;
        }

    
        //called to insert new image from file or photo
        private void addImage()
        {
            if (images.size() == Advertisement.maxImages){
                Toast.makeText(getContext(), "5 images maximum", Toast.LENGTH_SHORT).show();
                return;
            }

            changesDetected = true;
            mLauncher.launch("image/*");
        }


    ActivityResultLauncher<String> mLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {


                    //onbackpressed without image chosen
                    if (uri == null){ return;}

                    Image i = new Image();
                    //String path = ImageFilePath.getPath(getActivity().getApplicationContext(), uri);
                    i.newImage(uri, (MainActivity) getActivity());
                    mAdvert.addImage(i);
                    displayImage(mAdvert.imagesSize()-1);



                }
            });



    private void removeImage(Image im, ImageView view)
        {
            changesDetected = true;
            LinearLayoutCompat v = (LinearLayoutCompat) view.getParent();
            v.removeView(view);
            mAdvert.deleteImage(im);
    
        }




        //endregion
        

        //region ------------ channels

        private void loadChannels()
        {
            listOfOptions = marketController.getMessageLocationArrayList();

            chosenChats.addAll(listOfOptions);

            if (listOfOptions.size() == 0) return;

            TextView t = fragment.findViewById(R.id.post_edit_chats_textview);
            t.setVisibility(View.INVISIBLE);

            for (int i = 0; i < listOfOptions.size(); i++) {

               TdApi.Chat c = ((MainActivity)getActivity()).getSpecificChat(listOfOptions.get(i).getChatId());

               ImageView im = new ImageView(fragment.getContext());

                final int p = i;

                if (c.photo!= null && !c.photo.small.local.path.isEmpty()) {
                    String path = c.photo.small.local.path;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                    im.setImageBitmap(bitmap);


                    im.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (swapChosenChat(p)){
                               im.setColorFilter(null);
                            }else {
                                im.setColorFilter(Utilities.GRAY_TINT, PorterDuff.Mode.MULTIPLY);
                            }
                        }
                    });

                    if (mAdvert.getProposedLocationsSize() > 0) {
                        if (!mAdvert.isProposedLocation(listOfOptions.get(p).getChatId())) {
                            if (swapChosenChat(p)) {
                                im.setColorFilter(null);
                            } else {
                                im.setColorFilter(Utilities.GRAY_TINT, PorterDuff.Mode.MULTIPLY);
                            }
                        } else if (!mAdvert.isLive(listOfOptions.get(p).getChatId())) {
                            changesDetected = true;
                            im.setColorFilter(Utilities.RED_TINT, PorterDuff.Mode.MULTIPLY);
                        }

                    }else{
                        if (swapChosenChat(p)) {
                            im.setColorFilter(null);
                        } else {
                            im.setColorFilter(Utilities.GRAY_TINT, PorterDuff.Mode.MULTIPLY);
                        }
                    }
                    chatsLayout.addView(im);

                }else{

                    TextView tv = new TextView(fragment.getContext());
                    tv.setText(c.title);
                    tv.setPadding(10,0,10,0);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (swapChosenChat(p))
                            {
                                tv.setTextColor(Color.GREEN);
                            }else{
                                tv.setTextColor(Color.RED);

                            }

                        }
                    });

                    if (mAdvert.getProposedLocationsSize() > 0) {
                        if (!mAdvert.isProposedLocation(listOfOptions.get(p).getChatId())) {
                            if (swapChosenChat(p)) {
                                im.setColorFilter(null);
                            } else {
                                im.setColorFilter(Utilities.GRAY_TINT, PorterDuff.Mode.MULTIPLY);
                            }
                        } else if (!mAdvert.isLive(listOfOptions.get(p).getChatId())) {
                            changesDetected = true;
                            im.setColorFilter(Utilities.RED_TINT, PorterDuff.Mode.MULTIPLY);
                        }

                    }


                    chatsLayout.addView(tv);

                }



            }       

        }


        private boolean swapChosenChat(int p)
        {

            MessageLocation m = listOfOptions.get(p);

            int found = -1;
            for (int i = 0; i < chosenChats.size(); i++) {
                if (chosenChats.get(i).getChatId() == m.getChatId()){
                    found = i;
                }
            }

            if (found != -1){
                chosenChats.remove(found);
                return false;
            }

            //didnt find lets add it
            chosenChats.add(m);
            return true;
        }


        private boolean checkIfLocationsChanged()
        {
            if (mAdvert.getProposedLocation().size() != chosenChats.size()){
                mAdvert.setProposedLocation(chosenChats);
                return true;
            }

                int count = 0;
            for (int i = 0; i < chosenChats.size(); i++) {

                for (int j = 0; j < mAdvert.getProposedLocation().size(); j++) {

                    if (mAdvert.getProposedLocation().get(j).getChatId() == chosenChats.get(i).getChatId()){
                        count++;
                        break;
                    }
                }
            }

            if (count != chosenChats.size()){
                mAdvert.setProposedLocation(chosenChats);
                return true;
            }



            return false;
        }

        //endregion

    //region -------- event




    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        year = i;
        month = i1;
        day = i2;

        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), thisPosting, hour, minute, false);
        timePickerDialog.show();


    }


    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        hour = i;
        minute = i1;

        LocalDateTime selected = LocalDateTime.of(year, month, day, hour, minute);

        String event = selected.toString();
        String[] p = event.split("T");
        event = p[0] + "\n" + p[1];
        eventSwitch.setText(event);

        ZonedDateTime zonedDateTime = selected.atZone(ZoneId.systemDefault());
        long val = zonedDateTime.toEpochSecond();
        mAdvert.setExpiration(val);
        
    }
    



    //endregion




}
