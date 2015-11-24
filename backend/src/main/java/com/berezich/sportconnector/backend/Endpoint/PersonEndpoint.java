package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.AccountForConfirmation;
import com.berezich.sportconnector.backend.Person;
import com.berezich.sportconnector.backend.Picture;
import com.berezich.sportconnector.backend.ReqChangeEmail;
import com.berezich.sportconnector.backend.ReqResetPass;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 *    In case of finishing of supporting certain version use exception
 *    throw new BadRequestException("needUpdateApp@: some text");
 *    An appropriate message will be shown to the customer
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
    private static final float MIN_RATING = 1;
    static String ERROR_CONFIRM = "Ошибка! Ваша учетная запись %s не активирована!";
    static String ERROR_CONFIRM_ALREADY = "Ваша учетная запись %s уже активирована!";
    static String ERROR_CONFIRM_NOTFOUND = "Ошибка! Ваша учетная запись %s не найдена!";
    static String subjectAccountConfirmation = "registration SportConnector";
    static String msgBodyAccountConfirmation = "Для активации вашей учетной записи перейдите по ссылке: " +
            "https://sportconnector-981.appspot.com/confirm.html?id=%s&x=%s";
    static String ERROR_CONFIRM_EMAIL = "Ошибка! Ваш email %s не изменен!";
    static String ERROR_CONFIRM_EMAIL_ALREADY = "Ваш старый email %s уже изменен!";
    static String ERROR_CONFIRM_EMAIL_NOTFOUND = "Ошибка! Ваша учетная запись %s не найдена!";
    static String subjectConfirmEmail = "change Email SportConnector";
    static String msgBodyConfirmEmail = "Для смены email перейдите по ссылке: " +
            "https://sportconnector-981.appspot.com/email.html?id=%s&x=%s";
    static String ERROR_CONFIRM_RESET_PASS_NOTFOUND = "Ошибка! Повторите процедуру восстановления пароля.";
    static String subjectResetPass = "reset Password SportConnector";
    static String msgBodyResetPass = "Для смены пароля перейдите по ссылке " +
            "и следуйте дальнейшим инструкциям.\n" +
            "https://sportconnector-981.appspot.com/pass.html?x=%s";


    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Person.class);
        ObjectifyService.register(AccountForConfirmation.class);
        ObjectifyService.register(ReqChangeEmail.class);
        ObjectifyService.register(ReqResetPass.class);
    }


    @ApiMethod(
            name = "registerAccount",
            path = "AccountForConfirmation",
            httpMethod = ApiMethod.HttpMethod.POST)
    public AccountForConfirmation registerAccount(AccountForConfirmation account) throws BadRequestException {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that person._id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        account.setUuid();
        validateAccountProperties(account);
        Query<Person> query = ofy().load().type(Person.class).filter("email", account.getEmail());
        if(query!=null && query.count()>0)
        {
            throw new BadRequestException("loginExists@:Person with the same login already exists");
        }
        String digPass = msgDigest(account.getPass());
        if(digPass.equals(""))
            throw new BadRequestException("Server error");
        account.setPass(digPass);
        account.setRegisterDate(Calendar.getInstance().getTime());
        ofy().save().entity(account).now();
        logger.info("Created AccountForConfirmation.");
        account = ofy().load().entity(account).now();
        try {
            //String subject = new String(subjectAccountConfirmation.getBytes("windows-1251"),"windows-1251");
            String subject = subjectAccountConfirmation;
            //String msgBody = new String(msgBodyAccountConfirmation.getBytes("windows-1251"),"UTF-8");
            String msgBody =msgBodyAccountConfirmation;
            sendMail(account.getEmail(),subject,String.format(msgBody,
                    URLEncoder.encode(account.getEmail(), "UTF-8"), URLEncoder.encode(
                            account.getUuid(), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new BadRequestException("createConfirmMsg@: msg for confirmation account didn't send");
        }
        account.setPass("");
        return account;
    }

    @ApiMethod(
            name = "confirmAccount",
            path = "AccountForConfirmation",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void confirmAccount(@Named("id") String id, @Named("x") String x) throws BadRequestException {
        try {
            id = URLDecoder.decode(id, "UTF-8");
            x = URLDecoder.decode(x,"UTF-8");
            AccountForConfirmation account = ofy().load().type(AccountForConfirmation.class).id(id).now();
            if (account != null) {
                if(x.equals(account.getUuid())) {
                    logger.info(String.format("account: %s has been activated", id));
                    Person person = new Person(account);
                    insertPerson(person);
                    ofy().delete().type(AccountForConfirmation.class).id(id).now();
                    logger.info("Deleted AccountForConfirmation with ID: " + id);
                    return;
                }
                logger.info(String.format("AccountForConfirmation: %s hsh = %s doesn't match",id,x));
                throw new BadRequestException(String.format(ERROR_CONFIRM,id));
            }
            logger.info(String.format("accountForConfirmation: %s wasn't found",id));
            Person person = ofy().load().type(Person.class).id(id).now();
            if(person!=null)
                throw new BadRequestException(String.format(ERROR_CONFIRM_ALREADY,id));
            else
                throw new BadRequestException(String.format(ERROR_CONFIRM_NOTFOUND,id));
        } catch (UnsupportedEncodingException e) {
            throw new BadRequestException(String.format(ERROR_CONFIRM,id));
        }
    }

    @ApiMethod(
            name = "authorizePerson",
            path = "authorizePerson",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Person authorizePerson(@Named("email") String email, @Named("pass") String pass) throws NotFoundException,BadRequestException {
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        logger.info("Getting Person with ID: " + email);
        Person person = null;
        Query<Person> query = ofy().load().type(Person.class).filter("email", email);
        QueryResultIterator<Person> queryIterator = query.iterator();
        if(queryIterator!=null && queryIterator.hasNext())
            person = queryIterator.next();
        if (person != null) {
            String encPass = msgDigest(pass);
            if(!encPass.equals("") && person.getPass().equals(encPass)) {
                person.setPass("");
                return person;
            }
        }
        throw new NotFoundException("AuthFailed@:Could not find Person with ID: " + email + " or such passowrd");
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
    public Person get(@Named("id") Long id) throws NotFoundException,BadRequestException {
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ADMIN);
        logger.info("Getting Person with ID: " + id);
        Person person = ofy().load().type(Person.class).id(id).now();
        if (person == null) {
            throw new NotFoundException("Could not find Person with ID: " + id);
        }
        person.setPass("");
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
        String digPass;
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ADMIN);
        validatePersonProperties(person);
        digPass  = msgDigest(person.getPass());
        if(digPass.equals("")) {
            throw new BadRequestException("Server error");
        }
        person.setPass(digPass);
        return insertPerson(person);
    }

    private Person insertPerson(Person person) throws BadRequestException {
        validatePersonProperties(person);
        if(person.getId()!=null) {
            Person samePerson = ofy().load().type(Person.class).id(person.getId()).now();
            if (samePerson != null) {
                throw new BadRequestException("loginExists@:Person with the same login already exists");
            }
        }
        person.setRegistration(Calendar.getInstance().getTime());
        ofy().save().entity(person).now();
        logger.info("Created Person.");
        Person personRes = ofy().load().entity(person).now();
        setSpotCoachesPartners(personRes, null);
        personRes.setPass("");
        return personRes;
    }

    /**
     * Change email of an existing person.
     */
    @ApiMethod(
            name = "changeEmail",
            path = "personEmail",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void changeEmail(@Named("id") Long id, @Named("pass") String pass, @Named("oldEmail") String oldEmail, @Named("newEmail") String newEmail)
            throws NotFoundException, BadRequestException {
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        Person person = ofy().load().type(Person.class).id(id).now();
        if(person==null)
            throw new NotFoundException("Person with id:" + id + " not found");
        if(!person.getPass().equals(msgDigest(pass))) {
            logger.severe(String.format("Warning!!! Attempt of changing person (id:%d) email with not valid password", person.getId()));
            throw new BadRequestException("Changing person email error");
        }
        if(oldEmail.equals(person.getEmail()))
        {
            Query<Person> query = ofy().load().type(Person.class).filter("email", newEmail);
            if(query!=null && query.count()>0)
            {
                throw new BadRequestException("loginExists@:Person with the same login already exists");
            }

            ReqChangeEmail reqChangeEmail = new ReqChangeEmail(oldEmail,newEmail,person.getId(),Calendar.getInstance().getTime());
            ofy().save().entity(reqChangeEmail).now();
            reqChangeEmail = ofy().load().type(ReqChangeEmail.class).id(oldEmail).now();
            if(reqChangeEmail!=null) {
                try {
                    logger.info("ReqChangeEmail was created: " + reqChangeEmail);
                    String subject = subjectConfirmEmail;
                    String msgBody =msgBodyConfirmEmail;
                    sendMail(reqChangeEmail.getNewEmail(),subject,String.format(msgBody,
                            URLEncoder.encode(reqChangeEmail.getEmail(), "UTF-8"), URLEncoder.encode(
                                    reqChangeEmail.getUuid(),"UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new BadRequestException("createConfirmEmailMsg@: msg for confirm email didn't send");
                }
            }
        }
        else
            throw new BadRequestException("oldEmailErr@: old email doesn't match");
    }

    @ApiMethod(
            name = "confirmEmail",
            path = "confirmEmail",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void confirmEmail(@Named("id") String oldEmail, @Named("x") String x) throws BadRequestException {
        try {
            oldEmail = URLDecoder.decode(oldEmail, "UTF-8");
            x = URLDecoder.decode(x, "UTF-8");
            ReqChangeEmail reqChangeEmail = ofy().load().type(ReqChangeEmail.class).id(oldEmail).now();
            if (reqChangeEmail != null) {
                if(x.equals(reqChangeEmail.getUuid())) {
                    Person person = ofy().load().type(Person.class).id(reqChangeEmail.getPersonId()).now();
                    person.setEmail(reqChangeEmail.getNewEmail());
                    ofy().save().entity(person).now();
                    logger.info(String.format("account: %d email changed form %s to %s", reqChangeEmail.getPersonId(),reqChangeEmail.getEmail(),reqChangeEmail.getNewEmail()));
                    ofy().delete().type(ReqChangeEmail.class).id(oldEmail).now();
                    logger.info("Deleted ReqChangeEmail with ID: " + reqChangeEmail.getPersonId()+" email: "+ oldEmail);
                    return;
                }
                logger.info(String.format("ReqChangeEmail: id: %s oldEmail = %s newEmail = %s hsh = %s doesn't match",reqChangeEmail.getPersonId(),reqChangeEmail.getEmail(),reqChangeEmail.getNewEmail(),x));
                throw new BadRequestException(String.format(ERROR_CONFIRM_EMAIL,oldEmail));
            }
            logger.info(String.format("reqChangeEmail: %s wasn't found",oldEmail));
            Query<Person> query = ofy().load().type(Person.class).filter("email", oldEmail);
            if(query==null || query.count()==0)
                throw new BadRequestException(String.format(ERROR_CONFIRM_EMAIL_ALREADY,oldEmail));
            else
                throw new BadRequestException(String.format(ERROR_CONFIRM_EMAIL_NOTFOUND,oldEmail));
        } catch (UnsupportedEncodingException e) {
            throw new BadRequestException(String.format(ERROR_CONFIRM_EMAIL,oldEmail));
        }
    }
    /**
    * reset pass of an existing person
     */
    @ApiMethod(
            name = "resetPass",
            path = "person/resetPass",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void resetPass(@Named("email") String email)
            throws NotFoundException, BadRequestException {
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        Person person;
        Query<Person> query = ofy().load().type(Person.class).filter("email", email);
        if(!(query!=null && query.count()>0))
        {
            throw new NotFoundException("emailNotExists@:Person with email:" + email + " not found");
        }
        person = query.list().get(0);

        ReqResetPass reqResetPass = new ReqResetPass(person.getId());
        ofy().save().entity(reqResetPass).now();
        reqResetPass = ofy().load().type(ReqResetPass.class).id(reqResetPass.getUuid()).now();
        if(reqResetPass!=null) {
            try {
                logger.info("ReqResetPass was created: " + reqResetPass);
                sendMail(person.getEmail(),subjectResetPass,String.format(msgBodyResetPass,
                        URLEncoder.encode(reqResetPass.getUuid(),"UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new BadRequestException("createResetPassMsg@: msg for reset password didn't send");
            }
        }
        else
            throw new BadRequestException("createResetPassMsg@: ReqResetPass construction error");
    }

    /**
     * confirmation reset pass of an existing person via email
     */
    @ApiMethod(
            name = "resetPassConfirmation",
            path = "person/resetPassConfirmation",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void resetPassConfirmation(@Named("x") String x, @Named("pass") String pass)
            throws NotFoundException, BadRequestException {
        ReqResetPass reqResetPass = ofy().load().type(ReqResetPass.class).id(x).now();
        if(reqResetPass==null)
            throw new NotFoundException(ERROR_CONFIRM_RESET_PASS_NOTFOUND);
        else {
            Person person = ofy().load().type(Person.class).id(reqResetPass.getPersonId()).now();
            String digPass = msgDigest(pass);
            if(digPass.equals(""))
                throw new BadRequestException("Server error");
            person.setPass(digPass);
            ofy().save().entity(person).now();
            logger.info("Updated Person pass: " + person);
            ofy().delete().type(ReqResetPass.class).id(x).now();
            logger.info(String.format("Deleted ReqResetPass with ID: %s personId: %d",
                    reqResetPass.getUuid(), reqResetPass.getPersonId()));
        }


    }

    /**
     * Change password of an existing person.
     */
    @ApiMethod(
            name = "changePass",
            path = "personPass",
            httpMethod = ApiMethod.HttpMethod.GET)
    public void changePass(@Named("id") Long id, @Named("oldPass") String oldPass, @Named("newPass") String newPass)
            throws NotFoundException, BadRequestException {
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        Person person = ofy().load().type(Person.class).id(id).now();
        if(person==null)
            throw  new NotFoundException("Person with id:" + id + " not found");
        if(msgDigest(oldPass).equals(person.getPass()))
        {
            String digPass = msgDigest(newPass);
            if(digPass.equals(""))
                throw new BadRequestException("Server error");
            person.setPass(digPass);
            ofy().save().entity(person).now();
            logger.info("Updated Person pass: " + person);
        }
        else
            throw new BadRequestException("oldPassErr@: old password doesn't match");
    }

    /**
     * Updates an existing {@code Person} except EMAIL, PASSWORD, PHOTO, TYPE, REGISTRATION.
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
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        Person oldPerson = ofy().load().type(Person.class).id(id).now();
        if(oldPerson==null)
            throw  new NotFoundException("Person with id:" + id + " not found");
        if(oldPerson.getType()!=person.getType())
            throw new BadRequestException("Person with id:" + id + " has already another type = "+oldPerson.getType());
        if(!oldPerson.getPass().equals(msgDigest(person.getPass()))) {
            logger.warning(String.format("Warning!!! Attempt of updating person (id:%d) with not valid password",person.getId()));
            throw new BadRequestException("Person update error");
        }
        person.setId(id);
        person.setPass(oldPerson.getPass());
        person.setEmail(oldPerson.getEmail());
        person.setPhoto(oldPerson.getPhoto());
        person.setRegistration(oldPerson.getRegistration());
        validatePersonProperties(person);
        ofy().save().entity(person).now();
        logger.info("Updated Person: " + person);
        Person personRes = ofy().load().entity(person).now();
        setSpotCoachesPartners(personRes, oldPerson);
        personRes.setPass("");
        return personRes;
    }

    protected void addPersonsFavoriteSpot(List<Long> idLst,Long spotId) throws BadRequestException{
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


    protected void removePersonsFavoriteSpot(List<Long> idLst, Long spotId) throws BadRequestException{
        Person person;
        if(idLst!=null)
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
    protected void setPersonPhoto(Long id, String pass, String photoKey) throws BadRequestException,NotFoundException{
        if(pass==null||pass.isEmpty()) {
            logger.severe(String.format("Attempt to change profile(id:%d) photo to " +
                            "another one(key:%s) with not valid password!!!",id,photoKey));
            throw new NotFoundException("There is no such person");
        }
        Person person;
        checkExists(id);
        person = ofy().load().type(Person.class).id(id).now();
        if(!person.getPass().equals(msgDigest(pass))) {
            logger.severe(String.format("Attempt to change profile(id:%d) photo(key:%s) to " +
                            "another one(key:%s) with not valid password!!!",
                    id,person.getPhoto().getBlobKey(),photoKey));
            throw new NotFoundException("There is no such person");
        }
        com.berezich.sportconnector.backend.Picture pic = new com.berezich.sportconnector.backend.Picture(photoKey);
        person.setPhoto(pic);
        ofy().save().entity(person).now();
        logger.info("Person with id:" + id + " set photo id:" + photoKey);
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
    public void remove(@Named("id") Long id) throws NotFoundException,BadRequestException {
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ADMIN);
        checkExists(id);
        Person person = ofy().load().type(Person.class).id(id).now();
        if(person!=null) {
            new SpotEndpoint().removePersonFromSpots(person.getFavoriteSpotIdLst(), person.getType(), id);
            List<String> picBlobLst = new ArrayList<>();
            Picture photo = person.getPhoto();
            if(photo!=null)
                picBlobLst.add(photo.getBlobKey());
            List<Picture> pictureList = person.getPictureLst();
            if(pictureList!=null && pictureList.size()>0){
                for (Picture pic:pictureList)
                    picBlobLst.add(pic.getBlobKey());
                new FileManager().deleteFile(picBlobLst);
            }
        }
        ofy().delete().type(Person.class).id(id).now();
        logger.info(String.format("Person with ID: %d removed",id));
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
    public CollectionResponse<Person> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) throws BadRequestException{
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ADMIN);
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Person> query = ofy().load().type(Person.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Person> queryIterator = query.iterator();
        List<Person> personList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            personList.add(queryIterator.next());
        }
        return CollectionResponse.<Person>builder().setItems(personList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "listPersonByIdLst",
            path = "personByIdLst",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Person> listByIdLst(@Named("idLst") ArrayList<Long>idLst) throws BadRequestException{
        OAuth_2_0.check(OAuth_2_0.PERMISSIONS.ANDROID_APP);
        Map<Long,Person> personMap = ofy().load().type(Person.class).ids(idLst);
        if(personMap==null)
            return null;
        Set<Long> keys = personMap.keySet();
        Person person;
        for (Long key : keys)
        {
            person = personMap.get(key);
            person.setPass("");
            personMap.put(key,person);
        }
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
        if(person.getEmail()==null || person.getEmail().equals(""))
            throw new BadRequestException("emailNull@:email property must be initialized");
        if(person.getPass()==null || person.getPass().equals(""))
            throw new BadRequestException("passNull@:Password property must be initialized");
        if(person.getName()==null || person.getName().equals(""))
            throw new BadRequestException("nameNull@:Name property must be initialized");
        if(person.getType()==null)
            throw new BadRequestException("typeNull@:Type property must be 'PARTNER' or 'COACH'");
        if(person.getRating()<MIN_RATING)
            person.setRating(MIN_RATING);
    }

    private void validateAccountProperties(AccountForConfirmation accountForConfirmation) throws BadRequestException
    {
        if(accountForConfirmation.getEmail()==null || accountForConfirmation.getEmail().equals(""))
            throw new BadRequestException("idNull@:Email property must be initialized");
        if(accountForConfirmation.getPass()==null || accountForConfirmation.getPass().equals(""))
            throw new BadRequestException("passNull@:Password property must be initialized");
        if(accountForConfirmation.getName()==null || accountForConfirmation.getName().equals(""))
            throw new BadRequestException("nameNull@:Name property must be initialized");
        if(accountForConfirmation.getType()==null)
            throw new BadRequestException("typeNull@:Type property must be 'PARTNER' or 'COACH'");
        if(accountForConfirmation.getUuid()==null || accountForConfirmation.getUuid().equals(""))
            throw new BadRequestException("uuidNull@:Uuid property must be initialized");
    }

    //update lists of partners and coaches of some spots since a person was updated
    private void setSpotCoachesPartners(Person person, Person oldPerson) throws BadRequestException
    {
        List<Long> addSpotLst;
        List<Long> removeSpotLst = new ArrayList<>();

        List<Long> oldies = new ArrayList<>();
        List<Long> news = new ArrayList<>();

        Long id = null;
        Person.TYPE type = null;

        if(person!=null) {
            id = person.getId();
            type = person.getType();
            if (person.getFavoriteSpotIdLst() != null)
                news.addAll(person.getFavoriteSpotIdLst());
        }


        if(oldPerson !=null) {
            id = oldPerson.getId();
            type = oldPerson.getType();
            if (oldPerson.getFavoriteSpotIdLst() != null)
                oldies.addAll(oldPerson.getFavoriteSpotIdLst());

            addSpotLst = new ArrayList<>(news);
            addSpotLst.removeAll(oldies);
            removeSpotLst = new ArrayList<>(oldies);
            removeSpotLst.removeAll(news);
        }
        else
            addSpotLst = news;

        if(id!=null) {
            if (addSpotLst.size() > 0)
                new SpotEndpoint().addPersonsToSpots(addSpotLst, type, id);

            if (removeSpotLst.size() > 0)
                new SpotEndpoint().removePersonFromSpots(removeSpotLst, type, id);
        }
        else
            new BadRequestException("setSpotCoachesPartners person && oldPerson == null ");
    }
    protected static String msgDigest(String stringToEncrypt)
    {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        messageDigest.update(stringToEncrypt.getBytes());
        messageDigest.update((stringToEncrypt + "329emdIDSxc8989sfh").getBytes());
        String encryptedString = new String(messageDigest.digest());
        byte[]   bytesEncoded = Base64.encodeBase64(encryptedString.getBytes());
        encryptedString = new String(bytesEncoded);
        return encryptedString;
    }

    private void sendMail(String emailTo,String subject,String msgBody) {
        // ...
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String emailForm = "berezaman@gmail.com";

        try {

            Message msg = new MimeMessage(session);
            //msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
            msg.setFrom(new InternetAddress(emailForm, "SportConnector Admin"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo, "Mr. User"));
            msg.setSubject(subject);
            msg.setText(msgBody);

            Transport.send(msg);

        } catch (AddressException e) {
            // TODO: sendMail handle exception
        }catch (UnsupportedEncodingException e) {
            // ...
        } catch (MessagingException e) {
            // ...
        }
    }
}