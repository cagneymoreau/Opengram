package com.cagneymoreau.teletest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;
import com.cagneymoreau.teletest.databinding.ActivityMainBinding;
import com.cagneymoreau.teletest.ui.login.LoginFragment;
import com.google.android.material.navigation.NavigationView;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;




/**
 * area specific
 */

//basic app function

    //!! scan through app and get polished with current mvp messagin features

// FUTUREITEM: normal telegram features in order of importance, calls leave join, open chat to last position



// FIXME: 2/25/2022 appears channs are doubling with new layout

// FIXME: 2/14/2022 old calls to download and post imagesviews arent being erased
// TODO: 2/14/2022 need to pass timestamp to download calls so setimageview can compare timestamps in race conditions in local vs download
// TODO: 2/15/2022 even better use a simple unlock in onbind and a lock on the return of a value to lock out delayed race

// FIXME: 2/11/2022 when app first loads channel images are missing as well as user

// FIXME: 2/7/2022 download file method is not using unique id and may produce incorrect reults

// FIXME: 6/22/2022 the sortedlists change range does not work

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




public class MainActivity extends AppCompatActivity  {


    private final static String TAG = " Mainact";







    SearchView searchView;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;


    //update options
    NavigationView navigationView;




    SearchView.OnQueryTextListener currSearchListener;




    ImageView myAvatar;
    TextView title;
    TextView subtitle;




    TelegramController telegramController;
    Controller controller;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 2/16/2022 constant debug log fill
        //FirebaseApp.initializeApp(this);

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
                R.id.newGroup,
                R.id.contacts,
                R.id.chatList,
                R.id.settings,
                R.id.about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController (this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);



        telegramController = TelegramController.getInstance(this);
        telegramController.onCreate();
        controller = Controller.getInstance(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


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



   public void fillDrawerHeader(TdApi.User myself)
    {
        MainActivity m = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Utilities.getUserImage(myself, myAvatar, m);
                Utilities.setUserAvater(myself, myAvatar, m);
                title.setText(myself.firstName);
                subtitle.setText(myself.phoneNumber);
                myAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        controller.setDataBindingItem(myself);
                        Navigation.findNavController(m, R.id.nav_host_fragment).navigate(R.id.action_global_profile);
                    }
                });
            }
        });


    }


    public void newApkAvailable()
    {
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


    public void closeSearchView()
    {
        searchView.clearFocus();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        searchView.clearFocus();
    }




}

