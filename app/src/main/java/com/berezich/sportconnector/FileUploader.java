package com.berezich.sportconnector;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.berezich.sportconnector.backend.sportConnectorApi.model.Person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by Sashka on 11.09.2015.
 */
public class FileUploader {
    public static class FileInfo{
        //private final int MAX_SIZE = 1024*10;
        private String name;
        private Uri uri;
        private Bitmap bitmap;
        private Long size;
        private Date date;
        private String description;
        private String mimeType;
        private Fragment fragment;

        public FileInfo(Fragment fragment, String fileUri) {
            //byte[] dataFile = new byte[MAX_SIZE];
            String TAG = fragment.getTag();
            ParcelFileDescriptor mInputPFD;
            this.fragment = fragment;
            uri= Uri.parse(fileUri);
            try {
                bitmap =  MediaStore.Images.Media.getBitmap(fragment.getActivity().getContentResolver(), uri);
            } catch (IOException e) {
                Log.d(TAG, "getBitmap error uri = " + uri.toString());
                e.printStackTrace();
                return;
            }

            /*
            try {
                mInputPFD = fragment.getActivity().getContentResolver().openFileDescriptor(uri, "r");
                // Get a regular file descriptor for the file
                FileDescriptor fd = mInputPFD.getFileDescriptor();
                Log.d(TAG, "file descriptor = " + fd.toString());

                InputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(fd));
                    in.read(dataFile);
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "File not found.");
                return;
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            */

            Cursor returnCursor = fragment.getActivity().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            this.name = returnCursor.getString(nameIndex);
            this.size = returnCursor.getLong(sizeIndex);
            this.mimeType = fragment.getActivity().getContentResolver().getType(uri);
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
    public static class UploadFileAsyncTask extends AsyncTask< Pair <FileInfo,String>, Void, Exception > {
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
        protected Exception doInBackground(Pair <FileInfo,String>... params) {
            FileInfo fileInfo = params[0].first;
            Bitmap bitmap = fileInfo.getBitmap();
            String uploadUrl = params[0].second;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(uploadUrl);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addPart("Name", new StringBody(fileInfo.getName(), ContentType.MULTIPART_FORM_DATA));
                builder.addPart("Type", new StringBody(fileInfo.getMimeType(), ContentType.MULTIPART_FORM_DATA));
                Person myPersonInfo = LocalDataManager.getMyPersonInfo();
                if(myPersonInfo==null)
                    new NullPointerException("myPersonInfo == null");
                builder.addPart("usrId", new StringBody(myPersonInfo.getId().toString(), ContentType.MULTIPART_FORM_DATA));

                try{
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                    byte[] data = bos.toByteArray();
                    //ByteArrayBody bab = new ByteArrayBody(data, fileInfo.getName());

                    //builder.addPart("myFile", bab);

                    builder.addBinaryBody("usrPhoto", data, ContentType.create(fileInfo.getMimeType()), fileInfo.getName());


                }
                catch(Exception e){
                    return e;
                    //builder.addPart("myFile", new StringBody("",ContentType.MULTIPART_FORM_DATA));
                }
                HttpEntity entity = builder.build();
                postRequest.setEntity(entity);
                HttpResponse response = null;
                response = httpClient.execute(postRequest);

                /*BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String sResponse;
                StringBuilder s = new StringBuilder();
                try {
                    while ((sResponse = reader.readLine()) != null) {
                        s = s.append(sResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exception) {
            listener.onUploadFileFinish(exception);
        }

        public static interface OnAction
        {
            void onUploadFileFinish(Exception exception);
        }
    }
    /*
    static void uploadFile(Bitmap bitmap, String url)
    {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("name", new StringBody("Name", ContentType.MULTIPART_FORM_DATA));
            builder.addPart("Id", new StringBody("ID", ContentType.MULTIPART_FORM_DATA));
            builder.addPart("title",new StringBody("TITLE", ContentType.MULTIPART_FORM_DATA));
            builder.addPart("caption", new StringBody("Caption", ContentType.MULTIPART_FORM_DATA));

            try{
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                byte[] data = bos.toByteArray();
                ByteArrayBody bab = new ByteArrayBody(data, "forest.jpg");
                builder.addPart("picture", bab);
            }
            catch(Exception e){
                //Log.v("Exception in Image", ""+e);
                builder.addPart("picture", new StringBody("",ContentType.MULTIPART_FORM_DATA));
            }
            HttpEntity entity = builder.build();
            postRequest.setEntity(entity);
            HttpResponse response = null;
            try {
                response = httpClient.execute(postRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sResponse;
            StringBuilder s = new StringBuilder();
            try {
                while ((sResponse = reader.readLine()) != null) {
                    s = s.append(sResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    */
}
