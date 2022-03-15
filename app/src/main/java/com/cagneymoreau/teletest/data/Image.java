package com.cagneymoreau.teletest.data;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;


import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.Utilities;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Image {


    private static final String TAG = "Image";

    // TODO: 1/12/2022 integrate with tdapi thumb
    // TODO: 1/20/2022 use downsized version of normal images
    // TODO: 1/20/2022 delete the localfile when closing app and force into the telgram api
    // TODO: 1/24/2022 remove message content on save an reinstate on restart, or save photo content seperate and null mess content

    private final static int DEFAULT_IMG = R.drawable.ic_baseline_add_a_photo_24;
    private final static int DEFAULT_EVENT = R.drawable.ic_baseline_event_24;
    private final static int DEFAULT_ITEM = R.drawable.ic_baseline_local_offer_24;

    int resource = 0;
    int downsample = 5;

    //old or myposting
    /** must be deleted before save or share**/
    TdApi.MessagePhoto messagePhoto; // FIXME: 2/21/2022 this probably needs to go altogether
    /** must be deleted before save or share**/
    File tempfile;

    //non api
    String hostedFilehtml;

    //foundside
    long chatid;
    long messageid;
    long file_id;
    /** must be deleted before save or share**/
    TdApi.File imageFile = null;

    public Image()
    {}

    public Image getSaveAbleImage()
    {
        Image im = new Image();
        im.chatid = chatid;
        im.messageid = messageid;
        im.hostedFilehtml = hostedFilehtml;

        return im;
    }

    public void newImage(Uri uri, MainActivity mainActivity) {
        // TODO: 1/18/2022  literally double saving becaues android api is trash tier

        Bitmap bitmap = null;
        ContentResolver contentResolver = mainActivity.getContentResolver();
        try {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        OutputStream outputStream;
        tempfile = Utilities.getFilePath(String.valueOf(System.currentTimeMillis()), "png", mainActivity.getApplicationContext());

        try {
            outputStream = new FileOutputStream(tempfile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 65, outputStream);
            outputStream.close();

        } catch (Exception e) {
            Log.e(TAG, "newImage: ", e);
        }


    }

    public void setBlank() {
        resource = DEFAULT_IMG;
    }

    public String getHostedFilehtml() {
        return hostedFilehtml;
    }

    public String getLocalFile() {
        return tempfile.getAbsolutePath();
    }

    public void setMessage(TdApi.Message message) {
        messagePhoto = (TdApi.MessagePhoto) message.content;
        chatid = message.chatId;
        messageid = message.id;

    }

    public void displayImage(ImageView view, MainActivity activity) {


        //this is a software embedded static image
        if (resource != 0) {

            view.setImageResource(resource);

        }
        //check for local path
        else if (messagePhoto != null ) {

            String local = "";

            for (TdApi.PhotoSize p :
                    messagePhoto.photo.sizes) {
                if (!p.photo.local.path.isEmpty()) {
                    local = p.photo.local.path;
                    break;
                }
                }


            if (!local.isEmpty() && new File(local).exists()) {

                //display from local
                Log.d(TAG, "buildPictureMessage: post pic to imageview");

                try {

                    Uri img = Uri.fromFile(new File(local));

                    final InputStream imageStream = activity.getContentResolver().openInputStream(img);

                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    view.setImageBitmap(selectedImage);
                } catch (Exception e) {
                    view.setImageResource(DEFAULT_IMG);
                }

            }

            else {
            // TODO: 2/2/2022 test this why loading rando images
                //download
                downloadImage(messagePhoto, view, activity);
            }


        } else if (tempfile != null) {
            view.setImageBitmap(getBitmapFromFile(downsample));


        }


    }



    public void seekImageData(MainActivity mainActivity, ImageView imageView)
    {
        /*
        String[] v = hostedFilehtml.split(" ");
        String[] f = v[3].split("=");
        String url = "https://t.me/" + f[1].substring(1, f[1].length()-1);

        mainActivity.getMessageLinkInfo(url, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {

                Log.d(TAG, "onResult: ");
                
            }
        });

        */

        //check software embedded image
        if (resource != 0) {

            imageView.setImageResource(resource);

        }

        //check if we need file object
        else if (imageFile == null)
        {
            mainActivity.getMessage(chatid, messageid, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (TdApi.Message.CONSTRUCTOR != object.getConstructor()){
                        Log.e(TAG, "onResult: ", null);
                        return;
                    }

                    TdApi.Message m = (TdApi.Message) object;

                    if (TdApi.MessagePhoto.CONSTRUCTOR != m.content.getConstructor()){
                        Log.e(TAG, "onResult: ", null);
                        return;
                    }

                    TdApi.MessagePhoto mPhoto = (TdApi.MessagePhoto) m.content;

                    imageFile = mPhoto.photo.sizes[0].photo;

                    seekImageData(mainActivity, imageView);

                }
            });

        }
        //check is we can get locally
        else if (imageFile.local.isDownloadingCompleted)
        {
            displayFile(mainActivity, imageView, new File(imageFile.local.path), DEFAULT_IMG);
        }
        else{

            //download from remote
            mainActivity.downloadFile(imageFile.id , 32, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {

                    if (object.getConstructor() != TdApi.File.CONSTRUCTOR) {
                        //probably returned an error
                        Log.e(TAG, "onResult: " + object.toString(), null);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageResource(DEFAULT_IMG);
                            }
                        });

                        return;
                    }

                    imageFile = (TdApi.File) object;

                    displayFile(mainActivity, imageView, new File(imageFile.local.path), DEFAULT_IMG);
                }
            });
        }




    }




    private void displayFile(MainActivity mainActivity, ImageView imageView, File f, int defaultImg)
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
            imageView.setImageResource(defaultImg);
        }

    }





    private void downloadImage(TdApi.MessagePhoto mPhoto, ImageView view, MainActivity activity)
    {
        Client.ResultHandler h = new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {

                if (object.getConstructor() != TdApi.File.CONSTRUCTOR) {
                    //probably returned an error
                    Log.e(TAG, "onResult: " + object.toString(), null);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageResource(DEFAULT_IMG);
                        }
                    });

                    return;
                }

                    TdApi.File f = (TdApi.File) object;
                    
                    if (f.local.path.isEmpty()){
                        activity.downloadFile(f.id, 1, new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.Object object) {

                                if (object.getConstructor() != TdApi.File.CONSTRUCTOR) {
                                    //probably returned an error
                                    Log.e(TAG, "onResult: " + object.toString(), null);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            view.setImageResource(DEFAULT_IMG);
                                        }
                                    });

                                    return;
                                }

                                try {

                                    TdApi.File ftwo = (TdApi.File) object;

                                    Uri img = Uri.fromFile(new File(ftwo.local.path));

                                    final InputStream imageStream = activity.getContentResolver().openInputStream(img);

                                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);


                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            view.setImageBitmap(selectedImage);
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e(TAG, "onResult: ", e);
                                    view.setImageResource(DEFAULT_IMG);
                                }

                            }
                        });
                    }

                    else{
                        try {

                        Uri img = Uri.fromFile(new File(f.local.path));

                        final InputStream imageStream = activity.getContentResolver().openInputStream(img);

                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.setImageBitmap(selectedImage);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "onResult: ", e);
                        view.setImageResource(DEFAULT_IMG);
                    }
                }


            }
        };

            activity.getRemoteFile(mPhoto.photo.sizes[0].photo.remote.id, new TdApi.FileTypePhoto(), h);
        

    }

    private Bitmap getBitmapFromFile(int downSample) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = downSample;

        return BitmapFactory.decodeFile(tempfile.getAbsolutePath(), options);


    }

    //When we create a new posting with an image we just use the temp/cache file. But when we close the app
    //or delete an add we lose that file and hand over the image management to the telegram api
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (tempfile != null){
            tempfile.delete();
            tempfile = null;
        }
    }

    public static void displayDefault(ImageView view, Advertisement.Categories cat)
    {
        if (cat.getCategory().equals("event")){
            view.setImageResource(DEFAULT_EVENT);
        }else{
            view.setImageResource(DEFAULT_ITEM);
        }
    }

    /**
     * for non market images ts easier to use this aps Image class and this just method allows you to create an easy way to handle images
     * @param f
     */
    public void setTempfile(File f)
    {
        tempfile = f;
    }

    public void setDownSample(int i)
    {
        downsample = i;
    }

}
