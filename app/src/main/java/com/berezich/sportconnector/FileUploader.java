package com.berezich.sportconnector;

import android.graphics.Bitmap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Sashka on 11.09.2015.
 */
public class FileUploader {
    static void uploadFile(Bitmap bitmap, String url)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("You Link");
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
    }
}
