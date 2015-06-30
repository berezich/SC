package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.Person;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class PersonEndpoint {

    private static final Logger logger = Logger.getLogger(PersonEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Person.class);
    }

    /**
     * Returns the {@link Person} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Person} with the provided ID.
     */
    @ApiMethod(
            name = "getPerson",
            path = "person/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Person get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Person with ID: " + id);
        Person person = ofy().load().type(Person.class).id(id).now();
        if (person == null) {
            throw new NotFoundException("Could not find Person with ID: " + id);
        }
        return person;
    }

    /**
     * Inserts a new {@code Person}.
     */
    @ApiMethod(
            name = "insertPerson",
            path = "person",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Person insert(Person person) throws BadRequestException {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that person._id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        person.setId(null);
        validatePersonProperties(person);
        ofy().save().entity(person).now();
        logger.info("Created Person.");
        Person personRes = ofy().load().entity(person).now();
        setSpotCoachesPartners(personRes,null);
        return personRes;
    }

    /**
     * Updates an existing {@code Person}.
     *
     * @param id    the ID of the entity to be updated
     * @param person the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code getId} does not correspond to an existing
     *                           {@code Person}
     */
    @ApiMethod(
            name = "updatePerson",
            path = "person/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Person update(@Named("id") Long id, Person person) throws NotFoundException, BadRequestException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        Person oldPerson = ofy().load().type(Person.class).id(id).now();
        if(oldPerson==null)
            throw  new NotFoundException("Person with id:" + id + " not found");
        if(oldPerson.getType()!=person.getType())
            throw new BadRequestException("Person with id:" + id + " has already another type = "+oldPerson.getType());
        person.setId(id);
        validatePersonProperties(person);
        ofy().save().entity(person).now();
        logger.info("Updated Person: " + person);
        Person personRes = ofy().load().entity(person).now();
        setSpotCoachesPartners(personRes,oldPerson);
        return personRes;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void addPersonsFavoriteSpot(@Named("lstPersonIds") List<Long> idLst,@Named("spotId") Long spotId) {
        // TODO: You should validate your ID parameter against your resource's ID here.
        Person person;
        for (int i = 0; i < idLst.size(); i++) {
            Long id = idLst.get(i);
            try {
                checkExists(id);
                person = ofy().load().type(Person.class).id(id).now();
                if(person.getFavoriteSpotIdLst().add(spotId)) {
                    ofy().save().entity(person).now();
                    logger.info("Person with id:" + id + " add favorite spot id:" + spotId);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
                logger.info("Person with id:" + id + " not found to add favorite spot id:"+ spotId);
            }
        }
    }


    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void removePersonsFavoriteSpot(@Named("lstPersonIds") List<Long> idLst,@Named("spotId") Long spotId) {
        // TODO: You should validate your ID parameter against your resource's ID here.
        Person person;
        for (int i = 0; i < idLst.size(); i++) {
            Long id = idLst.get(i);
            try {
                checkExists(id);
                person = ofy().load().type(Person.class).id(id).now();
                if(person.getFavoriteSpotIdLst().remove(spotId)) {
                    ofy().save().entity(person).now();
                    logger.info("Person with id:" + id + " add favorite spot id:" + spotId);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
                logger.info("Person with id:" + id + " not found to add favorite spot id:"+ spotId);
            }
        }
    }

    /**
     * Deletes the specified {@code Person}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code getId} does not correspond to an existing
     *                           {@code Person}
     */
    @ApiMethod(
            name = "removePerson",
            path = "person/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Person.class).id(id).now();
        logger.info("Deleted Person with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listPerson",
            path = "person",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Person> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Person> query = ofy().load().type(Person.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Person> queryIterator = query.iterator();
        List<Person> personList = new ArrayList<Person>(limit);
        while (queryIterator.hasNext()) {
            personList.add(queryIterator.next());
        }
        return CollectionResponse.<Person>builder().setItems(personList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "listPersonByIdLst",
            path = "personByIdLst",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Person> listByIdLst(@Named("idLst") ArrayList<Long>idLst) {
        Map<Long,Person> personMap = ofy().load().type(Person.class).ids(idLst);
        return CollectionResponse.<Person>builder().setItems(personMap.values()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Person.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Person with ID: " + id);
        }
    }
    private void validatePersonProperties(Person person) throws BadRequestException
    {
        if(person.getName()==null || person.getName().equals(""))
            throw new BadRequestException("Name property must be initialized");
        if(person.getSurname()==null || person.getSurname().equals(""))
            throw new BadRequestException("Surname property must be initialized");
        if(person.getAge()<=0)
            throw new BadRequestException("Age property must be more than 0 years");
        if(person.getType()==null)
            throw new BadRequestException("Type property must be 'PARTNER' or 'COACH'");
    }

    private void setSpotCoachesPartners(Person person, Person oldPerson)
    {
        List<Long> addSpotLst = new ArrayList<Long>();
        List<Long> removeSpotLst = new ArrayList<Long>();

        List<Long> oldies = new ArrayList<Long>();
        List<Long> news = new ArrayList<Long>();

        if (person.getFavoriteSpotIdLst() != null)
            news.addAll(person.getFavoriteSpotIdLst());


        if(oldPerson !=null) {
            if (oldPerson.getFavoriteSpotIdLst() != null)
                oldies.addAll(oldPerson.getFavoriteSpotIdLst());

            addSpotLst = new ArrayList<Long>(news);
            addSpotLst.removeAll(oldies);
            removeSpotLst = new ArrayList<Long>(oldies);
            removeSpotLst.removeAll(news);
        }
        else
            addSpotLst = news;

        if(addSpotLst.size()>0)
            new SpotEndpoint().addPersonsToSpots(addSpotLst, person.getType(), person.getId());

        if(removeSpotLst.size()>0)
            new SpotEndpoint().removePersonsFromSpots(removeSpotLst, person.getType(), person.getId());
    }
}