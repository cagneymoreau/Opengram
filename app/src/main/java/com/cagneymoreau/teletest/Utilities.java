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


import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
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
    public final static String SAVE = "Save";
    public final static String TODO = "Todo";
    public final static String LINK = "Get Link";

    public final static String NEWMESS = "newMessage";




    /**
     *
     * @param user
     * @param imageView
     * @param mainActivity
     */

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

    public static void setUserAvater(TdApi.User user, ImageView imageView, MainActivity mainActivity)
    {

        //show error
        if (user == null){

            imageView.setImageBitmap(getInitials("err ??", mainActivity));
            return;
        }

        if ( user.profilePhoto == null){

            imageView.setImageBitmap(getInitials(user.firstName, mainActivity));

            mainActivity.getUserProfilePhoto(user, new Client.ResultHandler() {
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



    /**
     * This uses the cached directory. Files will be automi
     * @param name
     * @param suffix
     * @param context
     * @return
     */
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



    public static void setChatAvater(TdApi.Chat chat, ImageView avatar_imgView, MainActivity mainActivity)
    {
        String title = chat.title;
        TdApi.File file = null;
        if (chat.photo != null) {
           file = chat.photo.small;
        }

        setTdApiImageToImageView(file, 2, avatar_imgView, mainActivity, title);

    }


    public static void getMessagePhoto(TdApi.MessagePhoto photo, ImageView imageView, MainActivity mainActivity)
    {
         TdApi.File file = photo.photo.sizes[0].photo;

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
                mainActivity.downloadFile(id, 1, handler);

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

}
