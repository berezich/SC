package com.berezich.sportconnector.backend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

/**
 * Created by Sashka on 28.06.2015.
 */
public class Picture {
    String name;
    String blobKey;
    String servingUrl;
    byte[] data;
    public Picture(){}

    public Picture(String blobKey) {
        this.blobKey = blobKey;
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withBlobKey(new BlobKey(blobKey));
        servingUrl = imagesService.getServingUrl(servingUrlOptions);

    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public String getBlobKey() {
        return blobKey;
    }

    public String getServingUrl() {
        return servingUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setBlobKey(String blobKey) {
        this.blobKey = blobKey;
    }

    public void setServingUrl(String servingUrl) {
        this.servingUrl = servingUrl;
    }
}
