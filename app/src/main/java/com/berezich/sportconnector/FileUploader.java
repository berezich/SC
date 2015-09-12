package com.berezich.sportconnector;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Pair;

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

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * Created by Sashka on 11.09.2015.
 */
public class FileUploader {
    public class FileInfo{
        private String name;
        private String path;
        private Bitmap bitmap;
        private int size;
        private Date date;
        private String description;

        public FileInfo(String filePath) {
            this.path = filePath;
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

        public int getSize() {
            return size;
        }

        public Date getDate() {
            return date;
        }

        public String getDescription() {
            return description;
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

        public void setSize(int size) {
            this.size = size;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setDescription(String description) {
            this.description = description;
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
                builder.addPart(fileInfo.getName(), new StringBody("Name", ContentType.MULTIPART_FORM_DATA));
                builder.addPart(fileInfo.getDescription(), new StringBody("Caption", ContentType.MULTIPART_FORM_DATA));

                try{
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                    byte[] data = bos.toByteArray();
                    ByteArrayBody bab = new ByteArrayBody(data, fileInfo.getName());

                    builder.addPart("myFile", bab);
                }
                catch(Exception e){
                    builder.addPart("myFile", new StringBody("",ContentType.MULTIPART_FORM_DATA));
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
