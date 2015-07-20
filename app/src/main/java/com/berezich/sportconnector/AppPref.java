package com.berezich.sportconnector;

/**
 * Created by berezkin on 20.07.2015.
 */
public class AppPref {
    private boolean isAutoLogin;
    private String passHsh;

    public boolean isAutoLogin() {
        return isAutoLogin;
    }

    public String getPassHsh() {
        return passHsh;
    }

    public void setIsAutoLogin(boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

    public void setPassHsh(String passHsh) {
        this.passHsh = passHsh;
    }
}
