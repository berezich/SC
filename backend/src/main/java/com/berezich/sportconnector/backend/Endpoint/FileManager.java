package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.FileUrl;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreFailureException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.ObjectifyService;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Sashka on 29.08.2015.
 */
@Api(
        name = "sportConnectorApi",
        version = "v1",
        resource = "",
        namespace = @ApiNamespace(
                ownerDomain = "backend.sportconnector.berezich.com",
                ownerName = "backend.sportconnector.berezich.com",
                packagePath = ""
        )
)

public class FileManager {
    static {
        ObjectifyService.register(FileUrl.class);
    }
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());

    @ApiMethod(
            name = "getUrlForUpload",
            path = "fileManager",
            httpMethod = ApiMethod.HttpMethod.GET)
    public FileUrl uploadFileHandle() throws BadRequestException {
        OAuth_2_0.check();
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        return new FileUrl(blobstoreService.createUploadUrl("/upload_file"));
    }

    /*@ApiMethod(
            name = "deleteFile",
            path = "fileManager",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void deleteFile(@Named("blobKeyLst")List<String>... blobKeyLst) throws BadRequestException {
        OAuth_2_0.check();
    */

    protected void deleteFile(List<String> blobKeyLst) throws BadRequestException{
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        BlobKey blobKey;
        if(blobKeyLst !=null)
            for(int i=0; i< blobKeyLst.size(); i++) {
                blobKey = new BlobKey(blobKeyLst.get(i));
                try {
                    blobstoreService.delete(blobKey);
                } catch (BlobstoreFailureException e) {
                    logger.info("blob file delete failed\n"+e.getMessage());
                    e.printStackTrace();
                }
            }

    }
}
