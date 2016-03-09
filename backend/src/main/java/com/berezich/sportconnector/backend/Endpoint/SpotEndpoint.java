package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.Person;
import com.berezich.sportconnector.backend.Picture;
import com.berezich.sportconnector.backend.RegionInfo;
import com.berezich.sportconnector.backend.Spot;
import com.berezich.sportconnector.backend.UpdateSpotInfo;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.logging.Logger.getLogger;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
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
public class SpotEndpoint {

    private static final Logger logger = getLogger(SpotEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Spot.class);
    }

    /**
     * Returns the {@link Spot} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Spot} with the provided ID.
     */
    @ApiMethod(
            name = "getSpot",
            path = "spot/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Spot get(@Named("login") String login,
                    @Named("pass") String pass,
                    @Named("id") Long id) throws NotFoundException,BadRequestException {
        Auth.admin_check(login,pass);
        logger.info("Getting Spot with ID: " + id);
        Spot spot = ofy().load().type(Spot.class).id(id).now();
        if (spot == null) {
            throw new NotFoundException("Could not find Spot with ID: " + id);
        }
        return spot;
    }

    /**
     * Inserts a new {@code Spot}.
     */
    @ApiMethod(
            name = "insertSpot",
            path = "spot",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Spot insert(@Named("login") String login,
                       @Named("pass") String pass,
                       Spot spot) throws NotFoundException, BadRequestException {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that spot.getId has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.

        Auth.admin_check(login, pass);
        validateSpotProperties(spot);
        updateRegionInfoAboutSpot(spot);
        spot.setId(null);
        ofy().save().entity(spot).now();
        Spot spotRes = ofy().load().entity(spot).now();
        logger.info("Created Spot id:" + spotRes.getId() + " name:" + spotRes.getName());
        setSpotUpdateInstance(spotRes);
        setFavoritePersonsSpot(spotRes,null);
        return spotRes;
    }

    /**
     * Parses file with {@code blobKey} from blobStorage and inserts new spots to dataStore
     */
    @ApiMethod(
            name = "uploadSpots",
            path = "spot/parseFile",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void uploadSpotsFromFile(@Named("login") String login,
                                    @Named("pass") String pass,
                                    @Named("blobKey") String blobKey)
            throws NotFoundException, BadRequestException, InternalServerErrorException{
        final int BUFF_SIZE = 1024*500;
        int length;
        BlobKey blobKeyObj = null;
        BlobstoreInputStream inputStream;
        byte[] buff = new byte[BUFF_SIZE];
        Auth.admin_check(login, pass);
        try {
            blobKeyObj = new BlobKey(blobKey);
            inputStream = new BlobstoreInputStream(blobKeyObj);

        } catch (BlobstoreInputStream.BlobstoreIOException e) {
            e.printStackTrace();
            throw new BadRequestException (String.format("not valid blobKey exception: %s",e.getMessage()));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException (String.format("can't open blobStoreInputStream exception: %s",e.getMessage()));
        }
        try {
            length = inputStream.read(buff,0,BUFF_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException(String.format("can't read data from blobStoreInputStream exception: %s",e.getMessage()));
        }
        int cnt=0;
        String fileStr;
        if(length>0)
        {
            try {

                //fileStr = new String(buff, "windows-1251");
                fileStr = new String(buff, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                throw new InternalServerErrorException(String.format("convert byte to string failed exception: %s",e.getMessage()));
            }
            String[] courtsStr;
            try {
                courtsStr = fileStr.split("<endline>");
                logger.info( String.format("courts num = %d",courtsStr.length));
            } catch (Exception e) {
                e.printStackTrace();
                throw new InternalServerErrorException(String.format("split <endline> failed exception: %s",e.getMessage()));
            }
            Spot spot;
            List<Spot> spotList = new ArrayList<>();
            List<BlobInfo> blobInfos = new FileManager().getBlobInfos(login,pass);
            for(String courtStr:courtsStr)
            {
                if(!courtStr.equals("")) {
                    try {
                        //logger.info("courtStr for parsing: "+courtStr);
                        spot = new Spot(courtStr);
                        cnt++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new InternalServerErrorException(String.format("courtStr parsing failed " +
                                "index spot: %d courtStr: %s \nexception: %s",cnt,courtStr,e.getMessage()));
                    }
                    if(spot!=null) {

                        BlobInfo blobInfo;
                        List<Picture> pictures = spot.getPictureLst();
                        for (int k=0; k<pictures.size(); k++) {
                            Picture pic = pictures.get(k);
                            if ((blobInfo = FileManager.findBlobByFileName(blobInfos, pic.getName())) != null)
                                pictures.set(k,new Picture(blobInfo.getBlobKey().getKeyString()));
                            else
                                throw new NotFoundException(String.format("picture: %s not found " +
                                        "in blobStore index spot: %d courtStr: %s",pic.getName(),cnt,courtStr));
                        }
                        try {
                            validateSpotProperties(spot);
                        } catch (BadRequestException e) {
                            e.printStackTrace();
                            throw new InternalServerErrorException(String.format("validateSpotProperties() " +
                                    "failed index spot: %d courtStr: %s \nexception: %s",cnt,courtStr,e.getMessage()));
                        }
                        spotList.add(spot);
                    }
                }
            }

            for (Spot spot1:spotList)
                insert(login,pass,spot1);

            logger.info(String.format("%d spots were inserted",spotList.size()));
        } else
            logger.info("0 spots were inserted");



    }

    /**
     * Updates an existing {@code Spot}.
     *
     * @param id   the ID of the entity to be updated
     * @param spot the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Spot}
     */
    @ApiMethod(
            name = "updateSpot",
            path = "spot/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Spot update(@Named("login") String login,
                       @Named("pass") String pass,
                       @Named("id") Long id, Spot spot) throws NotFoundException, BadRequestException {
        //Auth.oAuth_2_0_check(Auth.PERMISSIONS.ADMIN);
        Auth.admin_check(login, pass);
        Spot oldSpot = ofy().load().type(Spot.class).id(id).now();
        if(oldSpot==null)
            throw new NotFoundException("Spot with id:"+id+" not found");
        spot.setId(id);
        validateSpotProperties(spot);
        updateRegionInfoAboutSpot(spot);
        ofy().save().entity(spot).now();
        Spot resSpot = ofy().load().entity(spot).now();
        logger.info("Updated Spot id:" + resSpot.getId() + " name:" + resSpot.getName());
        setSpotUpdateInstance(resSpot);
        setFavoritePersonsSpot(resSpot, oldSpot);
        return resSpot;
    }

    @ApiMethod(
            name = "attacheSpotPictures",
            path = "spot/{id}/pictures",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Spot attacheSpotPictures(@Named("login") String login,
                                    @Named("pass") String pass,
                                    @Named("id") Long id,
                                    @Named("picBlobKeyLst") List<String> picBlobKeyLst,
                                    @Named("isOverwrite") boolean isOverwrite)
            throws NotFoundException, BadRequestException {
        Auth.admin_check(login, pass);
        Spot spot = ofy().load().type(Spot.class).id(id).now();
        if(spot==null)
            throw new NotFoundException("Spot with id:"+id+" not found");
        updateRegionInfoAboutSpot(spot);

        List<Picture> newPictureLst = new ArrayList<>();
        for(String blobKey: picBlobKeyLst)
            newPictureLst.add(new Picture(blobKey));

        List<Picture> pictureList =spot.getPictureLst();
        if(pictureList==null)
            spot.setPictureLst(newPictureLst);
        else if(isOverwrite)
        {
            List<String> oldBlobKeys = new ArrayList<>();
            for (Picture pic: pictureList)
                oldBlobKeys.add(pic.getBlobKey());
            if(oldBlobKeys.size()>0) {
                new FileManager().deleteFile(oldBlobKeys);
            }
            spot.setPictureLst(newPictureLst);
        }
        else
            pictureList.addAll(newPictureLst);

        ofy().save().entity(spot).now();
        Spot resSpot = ofy().load().entity(spot).now();
        logger.info("Pictures attached/replaced/removed to Spot id:" + resSpot.getId() + " name:" + resSpot.getName());
        setSpotUpdateInstance(resSpot);
        return resSpot;
    }

    @ApiMethod(
            name = "setSpotAsFavorite",
            path = "spotSetAsFavorite",
            httpMethod = ApiMethod.HttpMethod.PUT)
    //TODO: oAuth_2_0_check user pass
    public void setAsFavorite(@Named("idSpot") Long idSpot,
                              @Named("idPerson") Long idPerson,
                              @Named("pass") String pass,
                              @Named("isFavorite") boolean isFavorite,
                              @Named("typePerson")Person.TYPE type) throws NotFoundException, BadRequestException {
        Auth.oAuth_2_0_check(Auth.PERMISSIONS.ANDROID_APP);
        Person person = ofy().load().type(Person.class).id(idPerson).now();
        if(!person.getPass().equals(PersonEndpoint.msgDigest(pass))){
            logger.severe(String.format("Warning!!! Attempt of setting spot as favorite for person (id:%d) with not valid password", person.getId()));
            throw new BadRequestException("Setting spto as favorite error");
        }
        Spot oldSpot;
        Spot spot = ofy().load().type(Spot.class).id(idSpot).now();
        if(spot==null) {
            throw new NotFoundException("Spot with id:" + idSpot + " not found");
        }
        oldSpot = new Spot(spot);
        List<Long> personLst = null;
        if(type == Person.TYPE.PARTNER)
            personLst = spot.getPartnerLst();
        else if(type == Person.TYPE.COACH)
            personLst = spot.getCoachLst();

        boolean isContain = personLst.contains(idPerson);
        boolean isChanged = false;
        if(!isContain && isFavorite) {
            personLst.add(idPerson);
            isChanged = true;
        }
        else if(isContain && !isFavorite){
            personLst.remove(idPerson);
            isChanged = true;
        }
        if(isChanged) {
            updateRegionInfoAboutSpot(spot);
            ofy().save().entity(spot).now();
            Spot resSpot = ofy().load().entity(spot).now();
            logger.info("Updated as favorite Spot id:" + resSpot.getId() + " name:" + resSpot.getName());
            setSpotUpdateInstance(resSpot);
            setFavoritePersonsSpot(resSpot, oldSpot);
        }
        return;
    }

    protected void removePersonFromSpots(List<Long> idLst, Person.TYPE type, Long personId) throws BadRequestException{
        //OAuth_2_0.oAuth_2_0_check(OAuth_2_0.PERMISSIONS.ADMIN);
        Spot spot;
        List<Long> personLst;
        for (int i = 0; i < idLst.size(); i++) {
            Long id = idLst.get(i);
            try {
                checkExists(id);
                spot = ofy().load().type(Spot.class).id(id).now();
                if(type == Person.TYPE.COACH)
                    personLst = spot.getCoachLst();
                else
                    personLst = spot.getPartnerLst();
                if(personLst.remove(personId)) {
                    ofy().save().entity(spot).now();
                    updateRegionInfoAboutSpot(spot);
                    setSpotUpdateInstance(spot);
                    logger.info("Person(id:" + id +" type:"+ type + ") was removed from spot(id:" + personId+")");
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
                logger.info("Person(id:" + id + ") not found to add favorite spot(id:"+ personId+")");
            }
        }
    }
    protected void addPersonsToSpots(List<Long> idLst, Person.TYPE type,Long personId) throws BadRequestException{
        //OAuth_2_0.oAuth_2_0_check(OAuth_2_0.PERMISSIONS.ADMIN);
        Spot spot;
        List<Long> personLst;
        for (int i = 0; i < idLst.size(); i++) {
            Long id = idLst.get(i);
            try {
                checkExists(id);
                spot = ofy().load().type(Spot.class).id(id).now();
                if(type == Person.TYPE.COACH)
                    personLst = spot.getCoachLst();
                else
                    personLst = spot.getPartnerLst();
                if(personLst.add(personId)) {
                    ofy().save().entity(spot).now();
                    updateRegionInfoAboutSpot(spot);
                    setSpotUpdateInstance(spot);
                    logger.info("Person(id:" + id +" type:"+ type + ") was added to spot(id:" + personId+")");
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
                logger.info("Person(id:" + id + ") not found to added favorite spot(id:"+ personId+")");
            }
        }
    }


    @ApiMethod(
            name = "removeAllSpots",
            path = "spot/all",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeAll(@Named("login") String login, @Named("pass") String pass) throws BadRequestException{
        final int MAX_LIMIT = 30;
        CollectionResponse<Spot> spotCollectionResponse;
        Auth.admin_check(login, pass);
        String nextToken = "";
        List<Spot> spots = new ArrayList<>();
        while (true) {
            spotCollectionResponse = list(login,pass, nextToken, MAX_LIMIT);
            nextToken = spotCollectionResponse.getNextPageToken();
            spots.addAll(spotCollectionResponse.getItems());
            if(spotCollectionResponse.getItems().size()<MAX_LIMIT)
                break;
        }
        for(Spot spotItem:spots)
            try {
                remove(login,pass,spotItem.getId());
            } catch (NotFoundException e) {
                logger.info(String.format("removing error spot (id = %s): not found",spotItem.getId()));
                e.printStackTrace();
            }
    }

    /**
     * Deletes the specified {@code Spot}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code getId} does not correspond to an existing
     *                           {@code Spot}
     */
    @ApiMethod(
            name = "removeSpot",
            path = "spot/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("login") String login,
                       @Named("pass") String pass,
                       @Named("id") Long id) throws NotFoundException,BadRequestException {
        Auth.admin_check(login, pass);
        checkExists(id);
        Spot spot = ofy().load().type(Spot.class).id(id).now();
        if(spot!=null) {
            ArrayList<Long> personLst = new ArrayList<>();
            if(spot.getCoachLst()!=null)
                personLst.addAll(spot.getCoachLst());
            if(spot.getPartnerLst()!=null)
                personLst.addAll(spot.getPartnerLst());
            new PersonEndpoint().removePersonsFavoriteSpot(personLst, spot.getId());

            List<Picture> pictureList = spot.getPictureLst();
            List<String> picBlobLst = new ArrayList<>();
            if(pictureList!=null && pictureList.size()>0){
                for (Picture pic:pictureList)
                    picBlobLst.add(pic.getBlobKey());
                new FileManager().deleteFile(picBlobLst);
            }
        }
        UpdateSpotInfo updateSpotInfo = ofy().load().type(UpdateSpotInfo.class).id(id).now();
        if(updateSpotInfo!=null) {
            updateSpotInfo.setUpdateDate(Calendar.getInstance().getTime());
            updateSpotInfo.setSpot(null);
            ofy().save().entity(updateSpotInfo).now();
            RegionInfo regionInfo = ofy().load().type(RegionInfo.class).id(updateSpotInfo.getRegionId()).now();
            if(regionInfo!=null) {
                regionInfo.setLastSpotUpdate(Calendar.getInstance().getTime());
                ofy().save().entity(regionInfo);
            }
        }
        ofy().delete().type(Spot.class).id(id).now();
        logger.info(String.format("Spot with ID: %d removed", id));
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listSpot",
            path = "spot",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Spot> list(@Named("login") String login,
                                         @Named("pass") String pass,
                                         @Nullable @Named("cursor") String cursor,
                                         @Nullable @Named("limit") Integer limit) throws BadRequestException{
        Auth.admin_check(login, pass);
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Spot> query = ofy().load().type(Spot.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Spot> queryIterator = query.iterator();
        List<Spot> spotList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            spotList.add(queryIterator.next());
        }
        return CollectionResponse.<Spot>builder().setItems(spotList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    /**
     * List all entities with specific regId.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listSpotByRegId",
            path = "spotByRegId",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Spot> listByRegId(@Named("regionId") Long regionId, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) throws BadRequestException{
        Auth.oAuth_2_0_check(Auth.PERMISSIONS.ANDROID_APP);
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Spot> query = ofy().load().type(Spot.class).filter("regionId", regionId).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Spot> queryIterator = query.iterator();
        List<Spot> spotList = new ArrayList<Spot>(limit);
        while (queryIterator.hasNext()) {
            spotList.add(queryIterator.next());
        }
        return CollectionResponse.<Spot>builder().setItems(spotList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Spot.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Spot with ID: " + id);
        }
    }
    private void updateRegionInfoAboutSpot(Spot spot) throws NotFoundException
    {
        RegionInfoEndpoint regionInfoEndpoint = new RegionInfoEndpoint();
        RegionInfo regionInfo = null;
        logger.setLevel(Level.INFO);
        try {
            regionInfo =  regionInfoEndpoint.getRegionInfo(spot.getRegionId());
            regionInfo.setLastSpotUpdate(new Date());
            regionInfoEndpoint.update(regionInfo.getId(), regionInfo);
            logger.info("RegionInfo with id=" + regionInfo.getId() + " is updated");
        } catch (NotFoundException e) {
            e.printStackTrace();
            logger.info("RegionInfo with id=" + spot.getRegionId() + " not found");
            throw e;
        } catch (BadRequestException e)
        {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }

    private void setSpotUpdateInstance(Spot spot)
    {
        UpdateSpotInfoEndpoint updateSpotInfoEndpoint = new UpdateSpotInfoEndpoint();
        UpdateSpotInfo updateSpotInfo = new UpdateSpotInfo(spot.getId(),spot.getRegionId(),new Date(),spot);
        try {
            try {
                updateSpotInfoEndpoint.update(updateSpotInfo.getId(), updateSpotInfo);
                logger.info("updateSpotInfo with getId=" + updateSpotInfo.getId() + " is inserted");
            } catch (NotFoundException e) {
                updateSpotInfoEndpoint.insert(updateSpotInfo);
                logger.info("updateSpotInfo with getId=" + updateSpotInfo.getId() + " is updated");
            }
        }
        catch (BadRequestException e)
        {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }

    private void setFavoritePersonsSpot(Spot spot, Spot oldSpot) throws BadRequestException
    {
        List<Long> addPersonLst = new ArrayList<Long>();
        List<Long> removePersonLst = new ArrayList<Long>();
        List<Long> newCoaches = spot.getCoachLst();
        List<Long> newPartners = spot.getPartnerLst();
        List<Long> oldCoaches;
        List<Long> oldPartners;

        List<Long> oldies = new ArrayList<Long>();
        List<Long> news = new ArrayList<Long>();

        if (newCoaches != null)
            news.addAll(newCoaches);
        if (newPartners != null)
            news.addAll(newPartners);

        if(oldSpot!=null) {
            oldCoaches = oldSpot.getCoachLst();
            oldPartners = oldSpot.getPartnerLst();

            if (oldCoaches != null)
                oldies.addAll(oldCoaches);
            if (oldPartners != null)
                oldies.addAll(oldPartners);


            addPersonLst = new ArrayList<Long>(news);
            addPersonLst.removeAll(oldies);
            removePersonLst = new ArrayList<Long>(oldies);
            removePersonLst.removeAll(news);
        }
        else
            addPersonLst = news;

        if(addPersonLst.size()>0)
            new PersonEndpoint().addPersonsFavoriteSpot(addPersonLst,spot.getId());

        if(removePersonLst.size()>0)
            new PersonEndpoint().removePersonsFavoriteSpot(removePersonLst, spot.getId());
    }
    private void validateSpotProperties(Spot spot)throws BadRequestException
    {
        if(spot.getRegionId()==null)
            throw  new BadRequestException("RegionId property must be initialized");
        if(spot.getName()==null || spot.getName().equals(""))
            throw  new BadRequestException("Name property must be initialized");
        if(spot.getCoords()==null)
            throw  new BadRequestException("Coordinates property must be initialized");
    }

}