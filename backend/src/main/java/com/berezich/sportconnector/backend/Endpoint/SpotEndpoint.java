package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.Spot;
import com.berezich.sportconnector.backend.StoreDataInfo;
import com.berezich.sportconnector.backend.UpdateSpotInfo;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.logging.Logger.*;

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
    public Spot get(@Named("id") Long id) throws NotFoundException {
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
    public Spot insert(Spot spot) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that spot.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(spot).now();
        Spot spotRes = ofy().load().entity(spot).now();
        logger.info("Created Spot.");
        StoreDataInfoEndpoint storeDataInfoEndpoint = new StoreDataInfoEndpoint();
        StoreDataInfo storeDataInfo = null;
        try {
            storeDataInfo =  storeDataInfoEndpoint.get(spot.regionId());
        } catch (NotFoundException e) {
            e.printStackTrace();
            logger.info("storeDataInfo with id="+spot.regionId()+" not found");
            return spotRes;
        }
        storeDataInfo.set_lastSpotUpdate(new Date());
        try {
            storeDataInfoEndpoint.update(storeDataInfo.get_id(),storeDataInfo);
        } catch (NotFoundException e) {
            e.printStackTrace();
            logger.info("storeDataInfo with id=" + storeDataInfo.get_id() + " not found");
        }

        UpdateSpotInfoEndpoint updateSpotInfoEndpoint = new UpdateSpotInfoEndpoint();
        UpdateSpotInfo updateSpotInfo = new UpdateSpotInfo(spotRes.regionId(),spotRes.id(),new Date());
        updateSpotInfoEndpoint.insert(updateSpotInfo);

        return spotRes;
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
    public Spot update(@Named("id") Long id, Spot spot) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(spot).now();
        logger.info("Updated Spot: " + spot);
        return ofy().load().entity(spot).now();
    }

    /**
     * Deletes the specified {@code Spot}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Spot}
     */
    @ApiMethod(
            name = "removeSpot",
            path = "spot/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Spot.class).id(id).now();
        logger.info("Deleted Spot with ID: " + id);
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
    public CollectionResponse<Spot> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Spot> query = ofy().load().type(Spot.class).limit(limit);
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
}