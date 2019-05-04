package com.rpol.monitor.helpers;

import java.net.CookieManager;

public class Settings {
    // SharedPreferences Settings
    public static final String PREFS = "RPOL_PREFS";
    public static final String PREFS_NICK = "PREFS_NICK";
    public static final String PREFS_PWD = "PREFS_PWD";
    public static final String PREFS_SAVE_PWD = "PREFS_SAVE_PWD";
    public static final String PREFS_UPDATE_INTERVAL = "PREFS_UPDATE_INTERVAL";
    // Connection settings
    private static Boolean logged_in = false;
    private static String nickname = "";
    private static CookieManager cookieManager = null;
    // Notifications settings
    public static final String CHANNEL_ID = "com.rpol.monitor.notifications.ANDROID";
    private static int update_interval = -1;

    public static Boolean isLoggedIn() {
        return logged_in;
    }

    public static void logIn() {
        Settings.logged_in = true;
    }

    public static void logOut() {
        Settings.logged_in = false;
    }

    public static String getNickname() {
        return nickname;
    }

    public static void setNickname(String nickname) {
        Settings.nickname = nickname;
    }

    public static int getUpdate_interval() {
        return update_interval;
    }

    public static void setUpdate_interval(int update_interval) {
        Settings.update_interval = update_interval;
    }

    public static CookieManager getCookieManager() {
        if (cookieManager == null) {
            cookieManager = new CookieManager();
        }
        return cookieManager;
    }
}
