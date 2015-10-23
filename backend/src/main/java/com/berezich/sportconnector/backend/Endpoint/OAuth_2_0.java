package com.berezich.sportconnector.backend.Endpoint;

import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.oauth.OAuthServiceFailureException;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import sun.rmi.runtime.Log;

/**
 * Created by berezkin on 14.08.2015.
 */
public class OAuth_2_0 {
    enum PERMISSIONS{ANDROID_APP,ADMIN}
    public static void check(PERMISSIONS permissions) throws BadRequestException
    {
        OAuthService oauth = OAuthServiceFactory.getOAuthService();
        String scope = "https://www.googleapis.com/auth/userinfo.email";
        Set<String> allowedClients = new HashSet<>();
        //all Google accounts
        //allowedClients.add("292824132082.apps.googleusercontent.com");
        //allowedClients.add("berezaman@gmail.com");
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
}
