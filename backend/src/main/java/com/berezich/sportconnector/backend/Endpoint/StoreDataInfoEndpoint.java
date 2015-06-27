package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.StoreDataInfo;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.googlecode.objectify.ObjectifyService;

import java.util.Date;
import java.util.logging.Logger;

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
public class StoreDataInfoEndpoint {

    private static final Logger logger = Logger.getLogger(StoreDataInfoEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(StoreDataInfo.class);
    }

    /**
     * Returns the {@link StoreDataInfo} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code StoreDataInfo} with the provided ID.
     */
    @ApiMethod(
            name = "getStoreDataInfo",
            path = "storeDataInfo/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public StoreDataInfo get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting StoreDataInfo with ID: " + id);
        StoreDataInfo storeDataInfo = ofy().load().type(StoreDataInfo.class).id(id).now();
        if (storeDataInfo == null) {
            throw new NotFoundException("Could not find StoreDataInfo with ID: " + id);
        }
        return storeDataInfo;
    }

    /**
     * Inserts a new {@code StoreDataInfo}.
     */
    @ApiMethod(
            name = "insertStoreDataInfo",
            path = "storeDataInfo",
            httpMethod = ApiMethod.HttpMethod.POST)
    public StoreDataInfo insert(StoreDataInfo storeDataInfo) throws InstanceAlreadyExists{
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that storeDataInfo.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        storeDataInfo.set_lastSpotUpdate(new Date());
        ofy().save().entity(storeDataInfo).now();
        logger.info("Created StoreDataInfo.");

        return ofy().load().entity(storeDataInfo).now();
    }

    /**
     * Updates an existing {@code StoreDataInfo}.
     *
     * @param id            the ID of the entity to be updated
     * @param storeDataInfo the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code StoreDataInfo}
     */
    @ApiMethod(
            name = "update",
            path = "storeDataInfo/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public StoreDataInfo update(@Named("id") Long id, StoreDataInfo storeDataInfo) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(storeDataInfo).now();
        logger.info("Updated StoreDataInfo: " + storeDataInfo);
        return ofy().load().entity(storeDataInfo).now();
    }
    /**
     * Deletes the specified {@code StoreDataInfo}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code StoreDataInfo}
     */
    /*
    @ApiMethod(
            name = "remove",
            path = "storeDataInfo/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(StoreDataInfo.class).id(id).now();
        logger.info("Deleted StoreDataInfo with ID: " + id);
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
            path = "storeDataInfo",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<StoreDataInfo> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<StoreDataInfo> query = ofy().load().type(StoreDataInfo.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<StoreDataInfo> queryIterator = query.iterator();
        List<StoreDataInfo> storeDataInfoList = new ArrayList<StoreDataInfo>(limit);
        while (queryIterator.hasNext()) {
            storeDataInfoList.add(queryIterator.next());
        }
        return CollectionResponse.<StoreDataInfo>builder().setItems(storeDataInfoList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }
    */

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(StoreDataInfo.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find StoreDataInfo with ID: " + id);
        }
    }
}