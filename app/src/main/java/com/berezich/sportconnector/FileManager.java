package com.berezich.sportconnector;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.berezich.sportconnector.backend.sportConnectorApi.model.Picture;
import com.google.api.client.util.Base64;
import com.google.api.client.util.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by Sashka on 11.09.2015.
 */
public class FileManager {
    //private final int MAX_SIZE = 1024*10;
    private static final int COMPRESS_QUALITY = 75;
    private static final int REQUIRED_SIZE = 1000;
    private static final String TAG = "MyLog_fileManager";
    public static class PicInfo {
        private String name;
        private String path;
        private Bitmap bitmap;
        private Long size;
        private Date date;
        private String description;
        private String mimeType;

        public PicInfo(Fragment fragment,String TAG, String fileUri) {
            Uri uri= Uri.parse(fileUri);
            Cursor returnCursor = fragment.getActivity().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            int dataIdx = returnCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            returnCursor.moveToFirst();
            this.name = returnCursor.getString(nameIndex);
            this.size = returnCursor.getLong(sizeIndex);
            this.mimeType = fragment.getActivity().getContentResolver().getType(uri);
            this.path = returnCursor.getString(dataIdx);
            bitmap = decodeFile(new File(path));
            try {
                int rotation = checkRotationDegrees(this.path);
                Log.d(TAG,"picture rotation = "+rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotation);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    Log.d(TAG,"picture rotated");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



            /*try {
                bitmap =  MediaStore.Images.Media.getBitmap(fragment.getActivity().getContentResolver(), uri);
            } catch (IOException e) {
                Log.d(TAG, "getBitmap error uri = " + uri.toString());
                e.printStackTrace();
                return;
            }*/

        }

        /**
         * @return compressed with jpeg and COMPRESS_QUALITY picture in byte[]
         */
        public byte[] getCompressedPic(){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY , bos);
            return bos.toByteArray();
        }

        public File savePicPreviewToCache(String TAG,Context context ,String fileName, Long personId)
        {
            return FileManager.savePicPreviewToCache(TAG, context, fileName, personId, bitmap);
        }



        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public Long getSize() {
            return size;
        }

        public Date getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
    public static class UploadFileAsyncTask extends AsyncTask< Pair <PicInfo,String>, Void, Pair<PicInfo, Exception >> {
        private OnAction listener=null;
        private final String TAG = "MyLog_UploadFile";
        public UploadFileAsyncTask(Fragment fragment)
        {
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement onUploadFileFinish for UploadFileAsyncTask");
            }
        }
        @Override
        protected Pair<PicInfo, Exception > doInBackground(Pair <PicInfo,String>... params) {
            PicInfo picInfo = params[0].first;
            String uploadUrl = params[0].second;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(uploadUrl);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addPart("Name", new StringBody(picInfo.getName(), ContentType.MULTIPART_FORM_DATA));
                builder.addPart("Type", new StringBody(picInfo.getMimeType(), ContentType.MULTIPART_FORM_DATA));
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                if(myPersonInfo==null)
                    new NullPointerException("myPersonInfo == null");
                builder.addPart("usrId", new StringBody(myPersonInfo.getId().toString(), ContentType.MULTIPART_FORM_DATA));

                try{
                    byte[] data = picInfo.getCompressedPic();
                    builder.addBinaryBody("usrPhoto", data, ContentType.create(picInfo.getMimeType()), picInfo.getName());
                }
                catch(Exception e){
                    return new Pair<>(picInfo,e);
                }

                HttpEntity entity = builder.build();
                postRequest.setEntity(entity);
                HttpResponse response = null;
                response = httpClient.execute(postRequest);
                Log.d(TAG,String.format("upload pic response http status: %s\n entity content: %s",
                        response.getStatusLine(), response.getEntity().getContent()));
                //TODO: check response state

                return new Pair<>(picInfo,null);
            } catch (Exception e) {
                return new Pair<>(picInfo,e);
            }
        }

        @Override
        protected void onPostExecute(Pair<PicInfo,Exception> result) {
            listener.onUploadFileFinish(result);
        }

        public static interface OnAction
        {
            void onUploadFileFinish(Pair<PicInfo,Exception> result);
        }
    }

    public static void providePhotoForImgView(Context context,ImageView imageView, Picture photoInfo, Long personId){
        int height = (int) context.getResources().getDimension(R.dimen.personProfile_photoHeight);
        int width = (int) context.getResources().getDimension(R.dimen.personProfile_photoWidth);

        if(photoInfo!=null)
        {
            String photoId = UsefulFunctions.getDigest(photoInfo.getBlobKey());
            File myFolder = FileManager.getAlbumStorageDir(TAG,context, personId.toString());
            boolean isNeedLoad=true;
            if(myFolder !=null)
            {
                File myPhoto = new File(myFolder,photoId);
                if(myPhoto.exists()) {
                    setPicToImageView(myPhoto, imageView,height,width);
                    isNeedLoad = false;
                }
            }
            if(isNeedLoad)
            {
                Log.d(TAG,"need to load myPhoto from server");
                String dynamicUrl = String.format("%s=s%d-c",photoInfo.getServingUrl(),(int) context.getResources().getDimension(R.dimen.personProfile_photoHeight));
                Log.d(TAG,String.format("url for download image = %s",dynamicUrl));
                new FileManager.DownloadImageTask(context,photoId,imageView).execute(dynamicUrl);
            }
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Pair<Bitmap,Exception>> {
        String imgId;
        Context context;
        String TAG="MyLog_loadImg";
        OnAction listener=null;
        boolean isRespHandled = true;
        ImageView imageView;
        String msgError;

        public DownloadImageTask(Fragment fragment, String imgId) {
            this.imgId = imgId;
            this.context = fragment.getActivity().getBaseContext();
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement onDownloadFileFinish for DownloadImageTask");
            }

        }

        public DownloadImageTask(Context context, String imgId, ImageView imageView) {
            this.imgId = imgId;
            this.context = context;
            isRespHandled = false;
            this.imageView = imageView;
            this.msgError = "";
        }

        public DownloadImageTask(Fragment fragment, String imgId, ImageView imageView, String msgError) {
            this.imgId = imgId;
            this.context = context;
            isRespHandled = false;
            this.imageView = imageView;
            this.msgError = msgError;
        }

        protected Pair<Bitmap,Exception> doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return new Pair<>(null,e);
            }
            return new Pair<>(mIcon11,null);
        }

        protected void onPostExecute(Pair<Bitmap,Exception> result) {

            if(isRespHandled) {
                if (listener != null)
                    listener.onDownloadFileFinish(result.first, imgId, result.second);
            }
            else if(imageView!=null) {
                Exception exception = result.second;
                Bitmap bitmap = result.first;
                if (exception==null) {
                    if(bitmap==null) {
                        Log.e(TAG, "bitmap not loaded from server cause: bitmap == null");
                        Toast.makeText(context, context.getString(R.string.personprofile_reqError), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "bitmap loaded from server");
                    imageView.setImageBitmap(bitmap);
                    Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                    if(myPersonInfo!=null) {
                        FileManager.savePicPreviewToCache(TAG, context, imgId, myPersonInfo.getId(), bitmap);
                    }
                }
                else {
                    Log.e(TAG, "bitmap not loaded from server");
                    Log.e(TAG, exception.getMessage());
                    exception.printStackTrace();
                    if(!msgError.equals(""))
                        Toast.makeText(context,msgError,Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        }
        public static interface OnAction
        {
            void onDownloadFileFinish(Bitmap bitmap,String imgId,Exception exception);
        }
    }

    public static File getAlbumStorageDir(String LOG_TAG, Context context,String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName);
        if (file.mkdirs()) {
            Log.d(LOG_TAG, String.format("Directory %s created",file.getPath()));
        }
        return file;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static Bitmap cropCenterBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
    // Decodes image and scales it to reduce memory consumption
    private static Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File  savePicPreviewToCache(String TAG,Context context ,String fileName, Long personId, Bitmap bitmap)
    {
        if(fileName==null || fileName.equals("")) {
            Log.e(TAG, "file name not valid");
            return null;
        }
        if(!isExternalStorageWritable()) {
            Log.e(TAG, "ExternalStorage not writable");
            return null;
        }

        File file = FileManager.getAlbumStorageDir(TAG, context, personId.toString());
        if(file!=null)
        {
            Log.d(TAG, "filePath = " + file.getPath());
            file = new File (file,fileName);
            if (file.exists ())
                file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                Bitmap endBitmap = cropCenterBitmap(bitmap);
                int photoHeight = (int)context.getResources().getDimension(R.dimen.personProfile_photoHeight);
                int photoWidth = (int)context.getResources().getDimension(R.dimen.personProfile_photoWidth);
                Log.d(TAG,String.format("myPhoto preview size = %dx%d",photoWidth,photoHeight));
                endBitmap = Bitmap.createScaledBitmap(endBitmap,photoWidth, photoHeight, false);
                endBitmap.compress(Bitmap.CompressFormat.JPEG, FileManager.COMPRESS_QUALITY, out);
                out.flush();
                out.close();
                return file;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    public static void setPicToImageView(File imgFile,ImageView imgView, int height, int width)
    {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(imgFile));
            if(in!=null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    IOUtils.copy(in, bos);
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length);
                if (bitmap != null && imgView != null) {
                    imgView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }


    }
    public static int checkRotationDegrees (String filePath)throws IOException
    {
            ExifInterface exif = new ExifInterface(filePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            return rotationInDegrees;
    }
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
}
