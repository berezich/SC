package com.berezich.sportconnector.backend;

import com.googlecode.objectify.annotation.Entity;

/**
 * Created by berezkin on 10.09.2015.
 */
@Entity
public class FileUrl {
    String urlForUpload;
    public FileUrl() {
    }

    public FileUrl(String urlForUpload) {
        this.urlForUpload = urlForUpload;
    }

    public String getUrlForUpload() {
        return urlForUpload;
    }

    public void setUrlForUpload(String urlForUpload) {
        this.urlForUpload = urlForUpload;
    }
}
