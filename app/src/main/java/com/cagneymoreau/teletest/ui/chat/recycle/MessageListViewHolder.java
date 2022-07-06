package com.cagneymoreau.teletest.ui.chat.recycle;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;


import com.airbnb.lottie.LottieAnimationView;
import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.MessageListCallback;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;
import com.cagneymoreau.teletest.data.Controller;
import com.cagneymoreau.teletest.data.TelegramController;


import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;

/**
 * Any time you have a message list use this viewholder
 */

public class MessageListViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "simplechatviewholder";

    TelegramController telegramController;
    Controller controller;

    View view;
    ImageView /*imageViewContent ,*/ imageviewIn, imageviewOut;
    //TextView textView;
    MainActivity mainActivity;
    //ConstraintLayout contentConst;
    LinearLayout contentLayout;
    int padding = 32;
    int pointpadding = padding + (padding/2);

    int txtColor = Color.WHITE;

    boolean prepared = false;

    MessageListCallback callback;

    public MessageListViewHolder(@NonNull View itemView, MainActivity a, MessageListCallback callback) {
        super(itemView);

        telegramController = TelegramController.getInstance(a);
        controller = Controller.getInstance(a);
        view = itemView;
        mainActivity = a;
        this.callback = callback;

        //imageViewContent = view.findViewById(R.id.chatmessage_ImageView);
        //textView = view.findViewById(R.id.chatmessage_textView);

        imageviewIn = view.findViewById(R.id.chatmessagecard_incoming_imageview);
        imageviewOut = view.findViewById(R.id.chatmessagecard_outgoing_imageview);

        //contentConst = view.findViewById(R.id.chatmessage_content_constraint);
        contentLayout = view.findViewById(R.id.chatmessage_content_layout);

    }


    public void setMessage(TdApi.Message message)
    {

        boolean out = bubbleWrap(message);

       switch (message.content.getConstructor() ){

           case TdApi.MessageText.CONSTRUCTOR:
               buildTextMessage(message, out);
               break;
           case TdApi.MessagePhoto.CONSTRUCTOR:
               buildPictureMessage(message, out);
               break;
           case TdApi.MessageAudio.CONSTRUCTOR:
               buildAudioMessage(message, out);
               break;
           case TdApi.MessageVoiceNote.CONSTRUCTOR:
               buildVoiceNoteMessage(message, out);
               break;
           case TdApi.MessageVideo.CONSTRUCTOR:
               buildVideoMessage(message, out);
               break;
           case TdApi.MessageVideoNote.CONSTRUCTOR:
               buildVideoNote(message, out);
               break;
           case TdApi.MessageDocument.CONSTRUCTOR:
               buildDocumentMessage(message, out);
               break;
           case TdApi.MessageSticker.CONSTRUCTOR:
               buildStickerMessage(message, out);
               break;
           case TdApi.MessageAnimation.CONSTRUCTOR:
               buildAnimatedMessage(message, out);
               break;
           default:
               cantDisplay(message, out);
               break;
       }


    }

    private void cantDisplay(TdApi.Message m, boolean outgoing)
    {
        contentLayout.removeAllViews();
        TextView tv = new TextView(view.getContext());
        tv.setTextColor(txtColor);
        tv.setText("Cant display yet");
        if (outgoing){
            tv.setPadding(padding,padding,pointpadding,padding);
        }else{
            tv.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(tv);

        //imageViewContent.setVisibility(View.INVISIBLE);
        //textView.setText("Cant display yet");


    }

    private void buildTextMessage(TdApi.Message m, boolean outgoing)
    {
      //imageViewContent.setVisibility(View.INVISIBLE);
      TdApi.MessageText mtext = ((TdApi.MessageText) m.content);

      if (mtext.webPage != null){
          buildWebPreviewMessage(mtext, outgoing);
          return;
      }

        contentLayout.removeAllViews();
        TextView tv = new TextView(view.getContext());
        tv.setTextColor(txtColor);
        tv.setText(mtext.text.text);
        if (outgoing){
            tv.setPadding(padding,padding,pointpadding,padding);
        }else{
            tv.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(tv);

      //textView.setText(mtext.text.text);

    }

    private void buildWebPreviewMessage(TdApi.MessageText mtext, boolean outgoing)
    {

        contentLayout.removeAllViews();

        View weblayout = View.inflate(contentLayout.getContext(), R.layout.web_preview_layout, contentLayout);

        String message = mtext.text.text;
        message = message.substring(mtext.text.entities[0].offset, (mtext.text.entities[0].offset + mtext.text.entities[0].length) );

        if (!message.isEmpty()) {
            TextView tv = weblayout.findViewById(R.id.webPreview_messageText_TextView);
            tv.setTextColor(txtColor);
            tv.setText(mtext.text.text);
            tv.setText(message);
        }

        TextView tvLink = weblayout.findViewById(R.id.webPreview_linkText_TextView);
        tvLink.setTextColor(txtColor);
        tvLink.setText(mtext.webPage.url);

        TextView content = weblayout.findViewById(R.id.webPreview_contentPrev_TextView);
        content.setTextColor(txtColor);
        content.setText(mtext.webPage.title);

        ImageView imageView = weblayout.findViewById(R.id.webPreview_photoPrev_TextView);
        Utilities.setWebViewImage(mtext.webPage, imageView, mainActivity);

        if (outgoing){
            weblayout.setPadding(padding,padding,pointpadding,padding);
        }else{
            weblayout.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(weblayout);

        //textView.setText(mtext.text.text);

    }

    private void buildPictureMessage(TdApi.Message m, boolean outgoing)
    {
        //imageViewContent.setVisibility(View.VISIBLE);

        contentLayout.removeAllViews();
        ImageView imageViewContent = new ImageView(view.getContext());
        imageViewContent.setPadding(padding,padding,padding,padding);
        contentLayout.addView(imageViewContent);

        TdApi.MessagePhoto mPhoto = ((TdApi.MessagePhoto) m.content);

        imageViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.setDataBindingItem(mPhoto);
                Navigation.findNavController(mainActivity, R.id.nav_host_fragment).navigate(R.id.action_global_imageFull);
            }
        });

        String t = mPhoto.caption.text;
        if ( t != null && !t.isEmpty()) {
            TextView tv = new TextView(view.getContext());
            tv.setTextColor(txtColor);
            tv.setText(t);
            if (outgoing){
                tv.setPadding(padding,padding,pointpadding,padding);
            }else{
                tv.setPadding(pointpadding,padding,padding,padding);
            }

            contentLayout.addView(tv);
        }

        Utilities.getMessagePhoto(mPhoto, imageViewContent, mainActivity);

    }

    private void buildAudioMessage(TdApi.Message m, boolean outgoing)
    {

        contentLayout.removeAllViews();
        ImageView imageViewContent = new ImageView(view.getContext());
        imageViewContent.setPadding(padding,padding,padding,padding);
        contentLayout.addView(imageViewContent);
        imageViewContent.setImageResource(R.drawable.ic_baseline_loading_24);

        MediaPlayer mp = new MediaPlayer();
        prepared = false;

        imageViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!prepared) return;
                if (mp.isPlaying()){
                    mp.stop();
                    imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }else{
                    mp.start();
                    imageViewContent.setImageResource(R.drawable.ic_baseline_pause_24);
                }
            }
        });


        TdApi.MessageAudio messageAudio = ((TdApi.MessageAudio) m.content);

        String t = messageAudio.caption.text;
        if ( t != null && !t.isEmpty()) {
            TextView tv = new TextView(view.getContext());
            tv.setTextColor(txtColor);
            tv.setText(t);
            if (outgoing){
                tv.setPadding(padding,padding,pointpadding,padding);
            }else{
                tv.setPadding(pointpadding,padding,padding,padding);
            }

            contentLayout.addView(tv);
        }

        if (!messageAudio.audio.audio.local.path.isEmpty()){
            //display from local
            Log.d(TAG, "buildPictureMessage: post pic to imageview");

            try {
                mp.setDataSource(messageAudio.audio.audio.local.path);
                mp.prepare();
                prepared = true;
                imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }catch (Exception e){
                Log.e(TAG, "buildPictureMessage: ", e);
            }

        }else{

            //download
            Client.ResultHandler h = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.File.CONSTRUCTOR){
                        //probably returned an error
                        Log.e(TAG, "onResult: " + object.toString(), null);
                        return;
                    }

                    try {

                        TdApi.File f = (TdApi.File) object;
                        mp.setDataSource(f.local.path);
                        mp.prepare();
                        prepared = true;
                        imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    }catch (Exception e)
                    {
                        Log.e(TAG, "onResult: ", e);
                    }

                }
            };

            telegramController.downloadFile( messageAudio.audio.audio.id, 1, h);

        }

    }

    private void buildVoiceNoteMessage(TdApi.Message m, boolean outgoing)
    {

        contentLayout.removeAllViews();
        ImageView imageViewContent = new ImageView(view.getContext());
        imageViewContent.setPadding(padding,padding,padding,padding);
        contentLayout.addView(imageViewContent);
        imageViewContent.setImageResource(R.drawable.ic_baseline_loading_24);
        imageViewContent.setColorFilter(mainActivity.getResources().getColor(R.color.text_neutral));

        MediaPlayer mp = new MediaPlayer();
        prepared = false;
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }
        });

        imageViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!prepared) return;
                if (mp.isPlaying()){
                    mp.stop();
                    imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }else{
                    mp.start();
                    imageViewContent.setImageResource(R.drawable.ic_baseline_pause_24);
                }
            }
        });


        TdApi.MessageVoiceNote messageAudio = ((TdApi.MessageVoiceNote) m.content);

        String t = messageAudio.caption.text;
        if ( t != null && !t.isEmpty()) {
            TextView tv = new TextView(view.getContext());
            tv.setTextColor(txtColor);
            tv.setText(t);
            if (outgoing){
                tv.setPadding(padding,padding,pointpadding,padding);
            }else{
                tv.setPadding(pointpadding,padding,padding,padding);
            }

            contentLayout.addView(tv);
        }

        if (!messageAudio.voiceNote.voice.local.path.isEmpty()){
            //display from local
            Log.d(TAG, "buildPictureMessage: post pic to imageview");

            try {
                mp.setDataSource(messageAudio.voiceNote.voice.local.path);
                mp.prepare();
                prepared = true;
                imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }catch (Exception e){
                Log.e(TAG, "buildPictureMessage: ", e);
            }

        }else{

            //download
            Client.ResultHandler h = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.File.CONSTRUCTOR){
                        //probably returned an error
                        Log.e(TAG, "onResult: " + object.toString(), null);
                        return;
                    }

                    try {

                        TdApi.File f = (TdApi.File) object;
                        mp.setDataSource(f.local.path);
                        mp.prepare();
                        prepared = true;
                        imageViewContent.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    }catch (Exception e)
                    {
                        Log.e(TAG, "onResult: ", e);
                    }

                }
            };

            telegramController.downloadFile( messageAudio.voiceNote.voice.id, 1, h);

        }

    }

    private void buildVideoMessage(TdApi.Message m, boolean outgoing)
    {
        TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) m.content;

        contentLayout.removeAllViews();
        LinearLayout l = new LinearLayout(contentLayout.getContext());
        VideoView videoView = new VideoView(mainActivity);
        videoView.setLayoutParams(new FrameLayout.LayoutParams(256, 256));

        if (outgoing){
           l. setPadding(padding,padding,pointpadding,padding);
        }else{
            l.setPadding(pointpadding,padding,padding,padding);
        }

        l.addView(videoView);
        contentLayout.addView(l);
        prepared = false;

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!prepared) return;

                if (videoView.isPlaying() )
                {
                     if( videoView.canPause())
                    {
                        videoView.pause();
                    }

                }else{
                    videoView.start();
                }


            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.seekTo(1);
            }
        });

        String t = "video";
        TextView tv = new TextView(view.getContext());
        tv.setTextColor(txtColor);
        tv.setText(t);
        if (outgoing){
            tv.setPadding(padding,padding,pointpadding,padding);
        }else{
            tv.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(tv);

        if (!messageVideo.video.video.local.path.isEmpty()){
            //display from local
            Log.d(TAG, "buildPictureMessage: post pic to imageview");

            try {

                videoView.setVideoPath(messageVideo.video.video.local.path);
                videoView.seekTo( 1 );
                prepared = true;

            }catch (Exception e){
                Log.e(TAG, "buildPictureMessage: ", e);
            }

        }else{

            //download
            Client.ResultHandler h = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.File.CONSTRUCTOR){
                        //probably returned an error
                        Log.e(TAG, "onResult: " + object.toString(), null);
                        return;
                    }

                    try {

                        TdApi.File f = (TdApi.File) object;
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.setVideoPath(f.local.path);
                                videoView.seekTo( 1 );
                                prepared = true;

                            }
                        });

                    }catch (Exception e)
                    {
                        Log.e(TAG, "onResult: ", e);
                    }

                }
            };

            telegramController.downloadFile( messageVideo.video.video.id, 1, h);

        }
    }

    private void buildVideoNote(TdApi.Message m, boolean outgoing)
    {
        contentLayout.removeAllViews();
        TdApi.MessageVideoNote messageVideo = ((TdApi.MessageVideoNote) m.content);

        VideoView videoView = new VideoView(mainActivity);
        videoView.setLayoutParams(new FrameLayout.LayoutParams(256, 256));
        contentLayout.addView(videoView);
        prepared = false;

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!prepared) return;

                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.seekTo(1);
            }
        });

        String t = "video";
            TextView tv = new TextView(view.getContext());
            tv.setTextColor(txtColor);
            tv.setText(t);
            if (outgoing){
                tv.setPadding(padding,padding,pointpadding,padding);
            }else{
                tv.setPadding(pointpadding,padding,padding,padding);
            }

            contentLayout.addView(tv);

        if (!messageVideo.videoNote.video.local.path.isEmpty() && new File(messageVideo.videoNote.video.local.path).exists()){
            //display from local
            Log.d(TAG, "buildPictureMessage: post pic to imageview");

            try {

                videoView.setVideoPath(messageVideo.videoNote.video.local.path);
                videoView.seekTo( 1 );
                prepared = true;

            }catch (Exception e){
                Log.e(TAG, "buildPictureMessage: ", e);
            }

        }else{

            //download
            Client.ResultHandler h = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.File.CONSTRUCTOR){
                        //probably returned an error
                        Log.e(TAG, "onResult: " + object.toString(), null);
                        return;
                    }

                    try {

                        TdApi.File f = (TdApi.File) object;
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.setVideoPath(f.local.path);
                                videoView.seekTo( 1 );
                               prepared = true;

                            }
                        });

                    }catch (Exception e)
                    {
                        Log.e(TAG, "onResult: ", e);
                    }

                }
            };

            telegramController.downloadFile( messageVideo.videoNote.video.id, 1, h);

        }

    }

    private void buildDocumentMessage(TdApi.Message m, boolean outgoing)
    {

        // TODO: 2/14/2022 make the content openable
        contentLayout.removeAllViews();

        TdApi.MessageDocument messageDocument = ((TdApi.MessageDocument) m.content);

        TextView link = new TextView(view.getContext());
        link.setText(messageDocument.document.fileName);
        contentLayout.addView(link);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //file is local
                if (!messageDocument.document.document.local.path.isEmpty() && new File(messageDocument.document.document.local.path).exists()){

                    File file = new File(messageDocument.document.document.local.path);


                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".JPG");

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), mime);
                    mainActivity.startActivity(intent);

                }else{

                    Toast.makeText(view.getContext(), "Begin Download", Toast.LENGTH_SHORT).show();

                    //download
                    Client.ResultHandler h = new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {

                            if (object.getConstructor() != TdApi.File.CONSTRUCTOR){
                                //probably returned an error
                                Log.e(TAG, "onResult: " + object.toString(), null);
                                return;
                            }

                            try {

                                TdApi.File f = (TdApi.File) object;
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(view.getContext(), "Download complete", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }catch (Exception e)
                            {
                                Log.e(TAG, "onResult: ", e);
                            }

                        }
                    };

                    telegramController.downloadFile( messageDocument.document.document.id, 1, h);

                }

            }
        });


        StringBuilder sb = new StringBuilder();

        sb.append(String.valueOf(messageDocument.document.document.size)).append("\n");

        sb.append(messageDocument.caption.text);

        TextView tv = new TextView(view.getContext());
        tv.setTextColor(txtColor);
        tv.setText(sb.toString());
        if (outgoing){
            tv.setPadding(padding,padding,pointpadding,padding);
        }else{
            tv.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(tv);


    }

    private void buildStickerMessage(TdApi.Message m, boolean outgoing)
    {
        contentLayout.removeAllViews();

        TdApi.MessageSticker messageSticker = (TdApi.MessageSticker) m.content;

        contentLayout.removeAllViews();

        LottieAnimationView imageViewContent = new LottieAnimationView(view.getContext());
        imageViewContent.setPadding(padding,padding,padding,padding);
        contentLayout.addView(imageViewContent);


            String t = "sticker";
            TextView tv = new TextView(view.getContext());
            tv.setTextColor(txtColor);
            tv.setText(t);
            if (outgoing){
                tv.setPadding(padding,padding,pointpadding,padding);
            }else{
                tv.setPadding(pointpadding,padding,padding,padding);
            }

            contentLayout.addView(tv);


                Utilities.getSticker(messageSticker, imageViewContent, mainActivity);

    }

    private void buildAnimatedMessage(TdApi.Message m, boolean outgoing)
    {

        TdApi.MessageAnimation messageAnimation = (TdApi.MessageAnimation) m.content;

        contentLayout.removeAllViews();
        VideoView videoView = new VideoView(mainActivity);
        videoView.setLayoutParams(new FrameLayout.LayoutParams(256, 256));
        contentLayout.addView(videoView);
        prepared = false;


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });

        String t = "video";
        TextView tv = new TextView(view.getContext());
        tv.setTextColor(txtColor);
        tv.setText(t);
        if (outgoing){
            tv.setPadding(padding,padding,pointpadding,padding);
        }else{
            tv.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(tv);

        if (!messageAnimation.animation.animation.local.path.isEmpty()){
            //display from local
            Log.d(TAG, "buildPictureMessage: post pic to imageview");

            try {

                videoView.setVideoPath(messageAnimation.animation.animation.local.path);
                videoView.start();
                prepared = true;

            }catch (Exception e){
                Log.e(TAG, "buildPictureMessage: ", e);
            }

        }else{

            //download
            Client.ResultHandler h = new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.File.CONSTRUCTOR){
                        //probably returned an error
                        Log.e(TAG, "onResult: " + object.toString(), null);
                        return;
                    }

                    try {

                        TdApi.File f = (TdApi.File) object;
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.setVideoPath(f.local.path);
                                videoView.start();
                                prepared = true;

                            }
                        });

                    }catch (Exception e)
                    {
                        Log.e(TAG, "onResult: ", e);
                    }

                }
            };

            telegramController.downloadFile( messageAnimation.animation.animation.id, 1, h);

        }



        /*
        contentLayout.removeAllViews();

        TdApi.MessageAnimation messageAnimation = (TdApi.MessageAnimation) m.content;



        contentLayout.removeAllViews();

        ImageView imageViewContent = new ImageView(view.getContext());
        imageViewContent.setPadding(padding,padding,padding,padding);
        contentLayout.addView(imageViewContent);


        String t = "animation";
        TextView tv = new TextView(view.getContext());
        tv.setTextColor(txtColor);
        tv.setText(t);
        if (outgoing){
            tv.setPadding(padding,padding,pointpadding,padding);
        }else{
            tv.setPadding(pointpadding,padding,padding,padding);
        }

        contentLayout.addView(tv);


        Utilities.getAnimation(messageAnimation, imageViewContent, mainActivity);

         */

    }

    private boolean bubbleWrap(TdApi.Message m)
    {
       if(TdApi.MessageSenderUser.CONSTRUCTOR == m.senderId.getConstructor()){

           TdApi.MessageSenderUser user = (TdApi.MessageSenderUser) m.senderId;
           TdApi.User user1 = telegramController.getUser(user.userId);

           if (user.userId == telegramController.getMyId()){
               //out

               //contentConst.setBackgroundResource(R.drawable.outgoing_bubble);
               contentLayout.setBackgroundResource(R.drawable.outgoing_bubble);
               ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();
               params.startToEnd = ConstraintLayout.LayoutParams.UNSET;
               params.endToStart = imageviewOut.getId();

               //imageviewIn.setVisibility(View.INVISIBLE);
               imageviewIn.setImageResource(R.drawable.ic_baseline_more_vert_24);
               imageviewIn.setOnClickListener(getMenuListener(m, imageviewIn));
               //imageviewOut.setVisibility(View.VISIBLE);
              //Utilities.getUserImage(user1, imageviewOut, mainActivity);
               Utilities.setUserAvater(user1, imageviewOut, mainActivity);

              return true;

           }else{

               //contentConst.setBackgroundResource(R.drawable.incoming_bubble);
               contentLayout.setBackgroundResource(R.drawable.incoming_bubble);

               ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();
               params.startToEnd = imageviewIn.getId();
               params.endToStart = ConstraintLayout.LayoutParams.UNSET;

               //imageviewIn.setVisibility(View.VISIBLE);
               //imageviewOut.setVisibility(View.INVISIBLE);
               imageviewOut.setImageResource(R.drawable.ic_baseline_more_vert_24);
               imageviewOut.setOnClickListener(getMenuListener(m, imageviewOut));
               //Utilities.getUserImage(user1, imageviewIn, mainActivity);
               Utilities.setUserAvater(user1, imageviewIn, mainActivity);

               return false;
           }

       }else{
           //contentConst.setBackgroundResource(R.drawable.incoming_bubble);
           contentLayout.setBackgroundResource(R.drawable.incoming_bubble);

           ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();
           params.startToEnd = imageviewIn.getId();
           params.endToStart = ConstraintLayout.LayoutParams.UNSET;

           //imageviewIn.setVisibility(View.VISIBLE);
           //imageviewOut.setVisibility(View.INVISIBLE);
           imageviewOut.setImageResource(R.drawable.ic_baseline_more_vert_24);
           imageviewOut.setOnClickListener(getMenuListener(m, imageviewOut));
           return false;
       }


    }


    private View.OnClickListener getMenuListener(TdApi.Message m, ImageView anchor)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ArrayList<String> options = new ArrayList<>();

                options.add(Utilities.REPLY);
                if (m.canBeForwarded) options.add(Utilities.FORWARD);
                if (m.canBeEdited) options.add(Utilities.EDIT);
                if (m.canBeDeletedOnlyForSelf || m.canBeDeletedForAllUsers) options.add(Utilities.DELETE);
                // TODO: 2/23/2022 pinned

                PopupMenu popupMenu = new PopupMenu(contentLayout.getContext(), anchor);

                for (int i = 0; i < options.size(); i++) {
                    popupMenu.getMenu().add(options.get(i));
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        callback.messageAction(m, menuItem.getTitle().toString());

                        return true;
                    }
                });

                popupMenu.show();

            }
        };

    }


    private View.OnClickListener getProfileListener(TdApi.User user)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                controller.setDataBindingItem(user);
                Navigation.findNavController(mainActivity, R.id.nav_host_fragment).navigate(R.id.action_global_profile);


            }
        };

    }




}
