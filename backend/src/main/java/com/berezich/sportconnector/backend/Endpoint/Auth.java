package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.ServerAdmin;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.oauth.OAuthServiceFailureException;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.googlecode.objectify.ObjectifyService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.logging.Logger.getLogger;

public class Auth {
    enum PERMISSIONS{ANDROID_APP,IOS_APP,WEB_APP}
    static {
        ObjectifyService.register(ServerAdmin.class);
    }
    private static final Logger logger = getLogger(SpotEndpoint.class.getName());

    protected static void admin_check(String login, String pass) throws BadRequestException
    {
        ServerAdmin serverAdmin = ofy().load().type(ServerAdmin.class).id(login).now();
        if(serverAdmin!=null)
            logger.info(String.format("pass:%s hshpass:%s bdpass:%s",pass,msgDigestAuth(pass),serverAdmin.getPass()));
        else
            logger.info(String.format("serverAdmin login:%s not found",login));
        if(serverAdmin==null || !serverAdmin.getPass().equals(msgDigestAuth(pass)))
        //if(serverAdmin==null || !serverAdmin.getPass().equals(pass))
            throw new BadRequestException("AdminAuthRequestException@: authorization failed");

    }
    /*
      * OAuth 2.0 for service account created for application (android/IOs/web)
     */
    protected static void oAuth_2_0_check(PERMISSIONS permissions) throws BadRequestException{
        OAuthService oauth = OAuthServiceFactory.getOAuthService();
        String scope = "https://www.googleapis.com/auth/userinfo.email";
        Set<String> allowedClients = new HashSet<>();

        //all google accounts
        //allowedClients.add("292824132082.apps.googleusercontent.com");

        //service account for Android app
        if(permissions == PERMISSIONS.ANDROID_APP)
            allowedClients.add("182489181232-bbiekce9fgm6gtelunr9lp82gmdk3uju.apps.googleusercontent.com");

        if(true)
            try {
                //User user = oauth.getCurrentUser(scope);
                String tokenAudience = oauth.getClientId(scope);
                if (!allowedClients.contains(tokenAudience)) {
                    throw new OAuthRequestException("audience of token '" + tokenAudience
                            + "' is not in allowed list");
                }
                // proceed with authenticated user
                // ...
            } catch (OAuthRequestException ex) {
                // handle auth error
                throw new BadRequestException("OAuthRequestException@: " + ((ex.getMessage().equals(""))? "auth failed" : ex.getMessage()));
            } catch (OAuthServiceFailureException ex) {
                // optionally, handle an oauth service failure
                throw new BadRequestException("OAuthServiceFailureException@: "+ ((ex.getMessage().equals(""))? "auth failed" : ex.getMessage()));
            }
    }
    private static String msgDigestAuth(String stringToEncrypt)
    {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        messageDigest.update(stringToEncrypt.getBytes());
        messageDigest.update((stringToEncrypt + "kui90ialcnjksh356jsmbcs23").getBytes());
        String encryptedString = new String(messageDigest.digest());
        byte[]   bytesEncoded = Base64.encodeBase64(encryptedString.getBytes());
        encryptedString = new String(bytesEncoded);
        return encryptedString;
    }
}
