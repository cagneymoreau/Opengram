package com.cagneymoreau.teletest;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.cagneymoreau.teletest.data.TelegramController;


import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Telegram specific class for ui like loader etc
 */

public class Utilities {

    private static final String TAG = "utilities";

    public static final int GRAY_TINT = 0x76ffffff;
    public static final int RED_TINT = 0x76ff0000;
    public static final int YELLOW_TINT = 0x76ffed05;

    /**
     * Pop-Up Menu Options
     */
    public final static String REPLY = "Reply";
    public final static String FORWARD = "Forward";
    public final static String EDIT = "Edit";
    public final static String PIN = "Pin";
    public final static String DELETE = "Delete";
    public final static String LINK = "Get Link";
    public final static String NEWMESS = "newMessage";

    public final static String GROUP = "newgroup";
    public final static String CHANNEL = "newchann";




    //region ---------------------  set image to view


    private static void displayFile(MainActivity mainActivity, ImageView imageView, File f, Bitmap b)
    {
        try {

            Uri img = Uri.fromFile(f);

            final InputStream imageStream = mainActivity.getContentResolver().openInputStream(img);

            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(selectedImage);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onResult: ", e);
            imageView.setImageBitmap(getInitials("err ??", mainActivity));
        }

    }


    private static void setTdApiImageToImageView(TdApi.File file,int sampleSize, ImageView avatar_imgView, MainActivity mainActivity, String backupText)
    {

        if (file != null){

            if (!file.local.path.isEmpty()) {
                String path = file.local.path;

                pathToImageView(path, mainActivity, avatar_imgView, sampleSize);

            }else{

                Client.ResultHandler handler = new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {

                        if (TdApi.File.CONSTRUCTOR == object.getConstructor())
                        {
                            TdApi.File file = (TdApi.File) object;
                            String path = file.local.path;

                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pathToImageView(path, mainActivity, avatar_imgView, sampleSize);
                                }
                            });


                        }
                    }
                };

                int id = file.id;
                TelegramController telegramController = TelegramController.getInstance(mainActivity);
                telegramController.downloadFile(id, 1, handler);

                //meanwhile
                Bitmap b = getInitials(backupText, mainActivity);
                avatar_imgView.setImageBitmap(b);

            }

        }
        //no image
        else{
            Bitmap b = getInitials(backupText, mainActivity);
            avatar_imgView.setImageBitmap(b);

        }

    }

    private static void pathToImageView(String path, MainActivity mainActivity, ImageView imageView, int downsample)
    {
        String[] parts = path.split("\\.");
        String suffix = parts[parts.length-1];

        if (suffix.equals("gif") || suffix.equals("mp4")) {

            Log.d(TAG, "pathToImageView: ");

        } else if (suffix.equals("tgs"))
        {
            String json = "";
            try {

                byte[] data = Files.readAllBytes(Paths.get(path));

                json = decompress(data);

            }catch (Exception e)
            {
                Log.e(TAG, "pathToImageView: ", e);
            }

            if (json.isEmpty()) return;

            LottieAnimationView view = (LottieAnimationView) imageView;
            view.setAnimationFromJson(json, "test");

            Log.d(TAG, "pathToImageView: do somehting");

        }else{

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = downsample;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);

            imageView.setImageBitmap(bitmap);

        }


    }




    public static void setChatAvater(TdApi.Chat chat, ImageView avatar_imgView, MainActivity mainActivity)
    {
        String title = chat.title;
        TdApi.File file = null;
        if (chat.photo != null) {
           file = chat.photo.small;
        }

        setTdApiImageToImageView(file, 2, avatar_imgView, mainActivity, title);

    }

    public static void setUserAvater(TdApi.User user, ImageView imageView, MainActivity mainActivity)
    {

        String title = user.firstName;
        TdApi.File file = null;
        if (user.profilePhoto != null) {
            file = user.profilePhoto.small;
        }

        setTdApiImageToImageView(file, 2, imageView, mainActivity, title);



        /*

        //show error
        if (user == null){

            imageView.setImageBitmap(getInitials("err ??", mainActivity));
            return;
        }

        if ( user.profilePhoto == null){

            imageView.setImageBitmap(getInitials(user.firstName, mainActivity));


            TelegramController telegramController = TelegramController.getInstance(mainActivity);
            telegramController.getUserProfilePhoto(user, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() == TdApi.ChatPhotos.CONSTRUCTOR){

                        TdApi.ChatPhotos p = (TdApi.ChatPhotos) object;

                        if (p.totalCount > 0){
                            setUserAvater(user, imageView, mainActivity);
                        }
                    }

                }
            });



        }

        //check is we can get locally
        else if (user.profilePhoto.small.local.isDownloadingCompleted)
        {
            displayFile(mainActivity, imageView, new File(user.profilePhoto.small.local.path), getInitials(user.firstName, mainActivity));
        }
        else{

            Log.e(TAG, "setUserAvater: unabe to set user photo", null);
            imageView.setImageBitmap(getInitials(user.firstName, mainActivity));

        }


    }

    public static void setUserAvater(TdApi.UserFullInfo user, ImageView imageView, MainActivity mainActivity)
    {


         */


    }


    public static void getUserImage(TdApi.User user, ImageView imageView, MainActivity mainActivity)
    {
        String title = "error";
        TdApi.File file = null;

        if (user != null)
        {
            title = user.firstName;
            if (user.profilePhoto != null)
            {
                file = user.profilePhoto.small;
            }
        }

        setTdApiImageToImageView(file, 2, imageView, mainActivity, title);

    }



    //endregion

    //region ---------------------------  set message media to view


    public static void getMessagePhoto(TdApi.MessagePhoto photo, ImageView imageView, MainActivity mainActivity)
    {
         TdApi.File file = photo.photo.sizes[0].photo;

         setTdApiImageToImageView(file, 0, imageView, mainActivity, "error");

    }

    public static void setMessageVideoThumb(TdApi.MessageVideo video, ImageView imageView, MainActivity mainActivity)
    {
        if (video.video.thumbnail == null) return;

        TdApi.File file = video.video.thumbnail.file;

        setTdApiImageToImageView(file, 0, imageView, mainActivity, "error");

    }

    public static void setMessageVideoNoteThumb(TdApi.MessageVideoNote video, ImageView imageView, MainActivity mainActivity)
    {
        if (video.videoNote.thumbnail == null) return;

        TdApi.File file = video.videoNote.thumbnail.file;

        setTdApiImageToImageView(file, 0, imageView, mainActivity, "error");

    }

    public static void getSticker(TdApi.MessageSticker sticker, ImageView imageView, MainActivity mainActivity)
    {
            TdApi.File file = sticker.sticker.sticker;
            
            if (sticker.sticker.isAnimated){
                Log.d(TAG, "getSticker: ");
            }

        setTdApiImageToImageView(file, 0, imageView, mainActivity, "error");

    }

    public static void getAnimation(TdApi.MessageAnimation animation, ImageView imageView, MainActivity mainActivity)
    {
        TdApi.File file = animation.animation.animation;

        setTdApiImageToImageView(file, 0, imageView, mainActivity, "error");

    }

    public static void setWebViewImage(TdApi.WebPage webPage, ImageView imageView, MainActivity mainActivity)
    {

        String title = webPage.title;
        TdApi.File file = null;
        if (webPage.photo != null) {
            file = webPage.photo.sizes[0].photo;
        }

        setTdApiImageToImageView(file, 2, imageView, mainActivity, title);



    }


    //endregion


    //region -------------  basics




    public static ArrayList<TdApi.User> filter(String query, ConcurrentMap<Long, TdApi.User> users, boolean sort)
    {
        if (query.isEmpty()) return new ArrayList<>();

        query = query.toLowerCase();

        Collection<TdApi.User> val = users.values();
        ArrayList<TdApi.User> usersList = new ArrayList<>(val);

        ArrayList<TdApi.User> matchingchats = new ArrayList<>();

        for (int i = 0; i < usersList.size(); i++) {

            String title = usersList.get(i).firstName.toLowerCase();

            if (title.contains(query)) matchingchats.add(usersList.get(i));

        }

        if (sort){

            Collections.sort(matchingchats,new UserComparator());
        }

        return matchingchats;

    }

    public static class UserComparator implements Comparator<TdApi.User> {

        // override the compare() method
        public int compare(TdApi.User u1, TdApi.User u2)
        {
            int user1 = 0, user2 = 0;

            if (u1.isMutualContact) user1 += 3;
            if (u2.isMutualContact) user2 += 3;

            if (u1.status.getConstructor() == TdApi.UserStatusOnline.CONSTRUCTOR) user1 += 3;
            else if (u1.status.getConstructor() == TdApi.UserStatusRecently.CONSTRUCTOR) user1 += 2;
            else if (u1.status.getConstructor() == TdApi.UserStatusLastWeek.CONSTRUCTOR) user1 += 1;

            if (u2.status.getConstructor() == TdApi.UserStatusOnline.CONSTRUCTOR) user2 += 3;
            else if (u2.status.getConstructor() == TdApi.UserStatusRecently.CONSTRUCTOR) user2 += 2;
            else if (u2.status.getConstructor() == TdApi.UserStatusLastWeek.CONSTRUCTOR) user2 += 1;

            return Integer.compare(user2, user1);

        }
    }


    public static Bitmap getInitials(String title, Activity a){

        if (title.isEmpty()) title = "?";

        String[]  words = title.split(" ");
        String initials = "";
        initials += words[0].substring(0,1);
        if (words.length > 1){
            initials += words[words.length-1].substring(0,1);
        }

        Bitmap b = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        //c.drawARGB(128,0,204,204);
        c.drawColor(a.getResources().getColor(R.color.text_contrast, a.getTheme()));
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(48);
        c.drawText(initials,4,70,paint);

        return b;
    }

    public static String getProperty(String key,Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("apikey.properties");
        properties.load(inputStream);
        return properties.getProperty(key);

    }

    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    public static String getStatusHumanReadable(TdApi.UserStatus stat)
    {

        String status = "unknown";

        switch (stat.getConstructor())
        {
            case TdApi.UserStatusOnline.CONSTRUCTOR:
                status = "Online";
                break;

            case TdApi.UserStatusRecently.CONSTRUCTOR:
                status = "Last seen recently";
                break;

            case TdApi.UserStatusLastWeek.CONSTRUCTOR:
                status = "Last seen within a week";
                break;

            case TdApi.UserStatusLastMonth.CONSTRUCTOR:
                status = "Last seen within a month";
                break;

            default:
                status = "Last seen a long time ago";
                break;

        }

        return status;
    }


    public static File getFilePath(String name, String suffix, Context context)
    {
        File file = new File("");



        //Log.i(TAG, "getFilePath: " + name + NAMEFORFILE);
        String fileName = Uri.parse(name + " -").getLastPathSegment();

        try {

            file = File.createTempFile(fileName, suffix, context.getCacheDir());
        } catch (IOException e) {
            Log.e(TAG, "getFilePath: failed", e);
            //Crashlytics.log(TAG + " " +  e);
            //Reporting.report_Event(mContext, Reporting.ERROR, "reportgenerator:618", e.toString());
        }

        return file;

    }



    //endregion




}
