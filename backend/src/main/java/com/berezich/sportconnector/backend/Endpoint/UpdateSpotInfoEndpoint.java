package com.berezich.sportconnector.backend.Endpoint;

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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

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
public class UpdateSpotInfoEndpoint {

    private static final Logger logger = Logger.getLogger(UpdateSpotInfoEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(UpdateSpotInfo.class);
    }

    /**
     * Returns the {@link UpdateSpotInfo} with the corresponding ID.
     *
     * @param _id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code UpdateSpotInfo} with the provided ID.
     */
    @ApiMethod(
            name = "getUpdateSpotInfo",
            path = "updateSpotInfo/{_id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public UpdateSpotInfo get(@Named("_id") Long _id) throws NotFoundException {
        logger.info("Getting UpdateSpotInfo with ID: " + _id);
        UpdateSpotInfo updateSpotInfo = ofy().load().type(UpdateSpotInfo.class).id(_id).now();
        if (updateSpotInfo == null) {
            throw new NotFoundException("Could not find UpdateSpotInfo with ID: " + _id);
        }
        return updateSpotInfo;
    }

    /**
     * Inserts a new {@code UpdateSpotInfo}.
     */
    /*
    @ApiMethod(
            name = "insert",
            path = "updateSpotInfo",
            httpMethod = ApiMethod.HttpMethod.POST)
            */
    public UpdateSpotInfo insert(UpdateSpotInfo updateSpotInfo) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that updateSpotInfo._id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(updateSpotInfo).now();
        logger.info("Created UpdateSpotInfo.");

        return ofy().load().entity(updateSpotInfo).now();
    }
    /**
     * Updates an existing {@code UpdateSpotInfo}.
     *
     * @param _id            the ID of the entity to be updated
     * @param updateSpotInfo the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code _id} does not correspond to an existing
     *                           {@code UpdateSpotInfo}
     */
    /*
    @ApiMethod(
            name = "update",
            path = "updateSpotInfo/{_id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public UpdateSpotInfo update(@Named("_id") Long _id, UpdateSpotInfo updateSpotInfo) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(_id);
        ofy().save().entity(updateSpotInfo).now();
        logger.info("Updated UpdateSpotInfo: " + updateSpotInfo);
        return ofy().load().entity(updateSpotInfo).now();
    }
    */
    /**
     * Deletes the specified {@code UpdateSpotInfo}.
     *
     * @param _id the ID of the entity to delete
     * @throws NotFoundException if the {@code _id} does not correspond to an existing
     *                           {@code UpdateSpotInfo}
     */
    /*
    @ApiMethod(
            name = "remove",
            path = "updateSpotInfo/{_id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("_id") Long _id) throws NotFoundException {
        checkExists(_id);
        ofy().delete().type(UpdateSpotInfo.class).id(_id).now();
        logger.info("Deleted UpdateSpotInfo with ID: " + _id);
    }
    */
    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    /*
    @ApiMethod(
            name = "list",
            path = "updateSpotInfo",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<UpdateSpotInfo> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<UpdateSpotInfo> query = ofy().load().type(UpdateSpotInfo.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<UpdateSpotInfo> queryIterator = query.iterator();
        List<UpdateSpotInfo> updateSpotInfoList = new ArrayList<UpdateSpotInfo>(limit);
        while (queryIterator.hasNext()) {
            updateSpotInfoList.add(queryIterator.next());
        }
        return CollectionResponse.<UpdateSpotInfo>builder().setItems(updateSpotInfoList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }
    */
    private void checkExists(Long _id) throws NotFoundException {
        try {
            ofy().load().type(UpdateSpotInfo.class).id(_id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find UpdateSpotInfo with ID: " + _id);
        }
    }
}