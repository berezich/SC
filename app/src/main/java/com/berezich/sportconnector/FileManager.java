package com.berezich.sportconnector;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;

import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;
import com.google.api.client.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by Sashka on 11.09.2015.
 */
public class FileManager {
    //private final int MAX_SIZE = 1024*10;
    private static final int COMPRESS_QUALITY = 75;
    public static class PicInfo {
        private String name;
        private Uri uri;
        private Bitmap bitmap;
        private Long size;
        private Date date;
        private String description;
        private String mimeType;
        private Fragment fragment;

        public PicInfo(Fragment fragment, String fileUri) {
            String TAG = fragment.getTag();
            this.fragment = fragment;
            uri= Uri.parse(fileUri);
            try {
                bitmap =  MediaStore.Images.Media.getBitmap(fragment.getActivity().getContentResolver(), uri);
            } catch (IOException e) {
                Log.d(TAG, "getBitmap error uri = " + uri.toString());
                e.printStackTrace();
                return;
            }
            Cursor returnCursor = fragment.getActivity().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            this.name = returnCursor.getString(nameIndex);
            this.size = returnCursor.getLong(sizeIndex);
            this.mimeType = fragment.getActivity().getContentResolver().getType(uri);
        }

        /**
         * @return compressed with jpeg and COMPRESS_QUALITY picture in byte[]
         */
        public byte[] getCompressedPic(){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY , bos);
            return bos.toByteArray();
        }

        public File savePicToCache(String fileName, Long personId)
        {
            if(fileName.equals(""))
                return null;

            File file = FileManager.getAlbumStorageDir(getFragment().getTag(),getFragment().getActivity().getBaseContext(),personId.toString());
            if(file!=null)
            {
                Log.d(getFragment().getTag(), "filePath = " + file.getPath());
                file = new File (file,fileName);
                if (file.exists ()) file.delete ();
                try {
                    FileOutputStream out = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, FileManager.COMPRESS_QUALITY, out);
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



        public String getName() {
            return name;
        }

        public Uri getUri() {
            return uri;
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

        public Fragment getFragment() {
            return fragment;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
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
        public UploadFileAsyncTask(Fragment fragment)
        {
            try {
                listener = (OnAction) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + " must implement OnAction for UploadFileAsyncTask");
            }
        }
        @Override
        protected Pair<PicInfo, Exception > doInBackground(Pair <PicInfo,String>... params) {
            PicInfo picInfo = params[0].first;
            Bitmap bitmap = picInfo.getBitmap();
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
                Log.d(picInfo.getFragment().getTag(),String.format("upload pic response http status: %s\n entity content: %s",
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

    public static File getAlbumStorageDir(String LOG_TAG, Context context,String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory has already created");
        }
        return file;
    }
}
