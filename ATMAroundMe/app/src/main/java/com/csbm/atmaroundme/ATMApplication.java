package com.csbm.atmaroundme;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.csbm.BEObject;
import com.csbm.CSBM;

/**
 * Created by akela on 09/08/2016.
 */
public class ATMApplication extends Application {
    public static final boolean APPDEBUG = false;

    // Debugging tag for the application
    public static final String APPTAG = "ATMAround";

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";

    // Key for saving the search distance preference
    private static final String KEY_SEARCH_DISTANCE = "searchDistance";

    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

    private static SharedPreferences preferences;

    private static ConfigHelper configHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        BEObject.registerSubclass(ATMAround.class);
        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .applicationId("57a9bed3f3a1de4134c386f0")
                .clientKey("")
                .server("http://192.168.1.33:1337/csbm/")
                .build());

        preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);

        configHelper = new ConfigHelper();
        configHelper.fetchConfigIfNeeded();
    }

    public static float getSearchDistance() {
        return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
    }
    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }

    public static void setSearchDistance(float value) {
        preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
    }


}
