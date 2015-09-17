package com.berezich.sportconnector.backend.Endpoint;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Sashka on 29.08.2015.
 */
public class UploadFile extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private static final Logger logger = Logger.getLogger(UploadFile.class.getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
        List<BlobKey> blobKeys;
        if((blobKeys = blobs.get("myFile"))!=null && !blobKeys.isEmpty()) {
            res.sendRedirect("/serve_file?blob-key=" + blobKeys.get(0).getKeyString());
        }
        if((blobKeys = blobs.get("UsrPhoto"))!=null && !blobKeys.isEmpty()) {
            try {
                Long usrId = Long.parseLong(req.getParameter("UsrId"));
                String replacePicBlobStr = req.getParameter("ReplaceBlob");
                try {
                    if(replacePicBlobStr!=null && !replacePicBlobStr.equals("")) {
                        List<String> deleteFileKeys = new ArrayList<String>();
                        deleteFileKeys.add(replacePicBlobStr);
                        new FileManager().deleteFile(deleteFileKeys);
                    }
                } catch (Exception e) {
                    logger.info(e.toString());
                    e.printStackTrace();
                    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
                new PersonEndpoint().setPersonPhoto(usrId, blobKeys.get(0).getKeyString());
            } catch (NumberFormatException e) {
                logger.info("usrId not valid");
                e.printStackTrace();
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "usrId not valid");
            } catch (NotFoundException e){
                logger.info(e.toString());
                e.printStackTrace();
                res.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            }catch (BadRequestException e){
                logger.info(e.toString());
                e.printStackTrace();
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }catch (Exception e){
                logger.info(e.toString());
                e.printStackTrace();
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return;
        }
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File not uploaded");
    }
}
