package com.berezich.sportconnector;

/**
 * Created by berezkin on 20.07.2015.
 */
public class AppPref {
    private boolean isAutoLogin;

    public AppPref(boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

    public boolean isAutoLogin() {
        return isAutoLogin;
    }

    public void setIsAutoLogin(boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

}
