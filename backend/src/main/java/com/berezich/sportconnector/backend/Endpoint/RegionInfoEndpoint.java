package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.RegionInfo;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.api.server.spi.response.BadRequestException;
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
public class RegionInfoEndpoint {

    private static final Logger logger = Logger.getLogger(RegionInfoEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(RegionInfo.class);
    }

    /**
     * Returns the {@link RegionInfo} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code StoreDataInfo} with the provided ID.
     */
    @ApiMethod(
            name = "getRegionInfo",
            path = "storeDataInfo/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public RegionInfo get(@Named("id") Long id) throws NotFoundException, BadRequestException {
        OAuth_2_0.check();
        logger.info("Getting StoreDataInfo with ID: " + id);
        RegionInfo regionInfo = ofy().load().type(RegionInfo.class).id(id).now();
        if (regionInfo == null) {
            throw new NotFoundException("Could not find RegionInfo with ID: " + id);
        }
        return regionInfo;
    }

    /**
     * Inserts a new {@code StoreDataInfo}.
     */
    @ApiMethod(
            name = "insertRegionInfo",
            path = "storeDataInfo",
            httpMethod = ApiMethod.HttpMethod.POST)
    public RegionInfo insert(RegionInfo regionInfo) throws InstanceAlreadyExists, BadRequestException {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that storeDataInfo.getId has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        OAuth_2_0.check();
        validateRegionInfoProperties(regionInfo);
        try {
            checkExists(regionInfo.getId());
            throw new InstanceAlreadyExists("RegionInfo with the same getId already exists");
        } catch (NotFoundException e) {
            regionInfo.setLastSpotUpdate(new Date());
            if(regionInfo.getReleaseDate()==null)
                regionInfo.setReleaseDate(new Date());
            ofy().save().entity(regionInfo).now();
            logger.info("Created RegionInfo getId:"+regionInfo.getId()+" getName:"+regionInfo.getRegionName()
                    + " version:"+regionInfo.getVersion());

            return ofy().load().entity(regionInfo).now();
        }

    }

    /**
     * Updates an existing {@code StoreDataInfo}.
     *
     * @param id            the ID of the entity to be updated
     * @param regionInfo the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code getId} does not correspond to an existing
     *                           {@code StoreDataInfo}
     */

    /*@ApiMethod(
            getName = "updateRegionInfo",
            path = "storeRegionInfo/{getId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    */
    protected RegionInfo update(Long id, RegionInfo regionInfo) throws NotFoundException, BadRequestException {
        OAuth_2_0.check();
        checkExists(id);
        validateRegionInfoProperties(regionInfo);
        ofy().save().entity(regionInfo).now();
        logger.info("Updated RegionInfo: " + regionInfo);
        return ofy().load().entity(regionInfo).now();
    }
    /**
     * Deletes the specified {@code StoreDataInfo}.
     *
     * @param getId the ID of the entity to delete
     * @throws NotFoundException if the {@code getId} does not correspond to an existing
     *                           {@code StoreDataInfo}
     */
    /*
    @ApiMethod(
            getName = "remove",
            path = "storeDataInfo/{getId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("getId") Long getId) throws NotFoundException {
        checkExists(getId);
        ofy().delete().type(StoreDataInfo.class).getId(getId).now();
        logger.info("Deleted StoreDataInfo with ID: " + getId);
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
            getName = "list",
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
            ofy().load().type(RegionInfo.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find RegionInfo getId:" + id);
        }
    }
    private void validateRegionInfoProperties(RegionInfo regionInfo) throws BadRequestException
    {
        if(regionInfo.getId()==null)
            throw new BadRequestException("Id property must be initialized");
        if(regionInfo.getRegionName()==null || regionInfo.getRegionName().equals(""))
            throw new BadRequestException("RegionName property must be initialized");
        if(regionInfo.getVersion()==null || regionInfo.getVersion().equals(""))
            throw new BadRequestException("Version property must be initialized");
    }
}