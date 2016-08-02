package com.csbm.starterproject;

import android.app.Application;

import com.csbm.CSBM;

/**
 * Created by akela on 24/07/2016.
 */
public class ProjectApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .applicationId(getString(R.string.APP_ID)).clientKey("123123")
                .server("http://" + getString(R.string.SERVER_IP) + ":1337/csbm/")
                .build());
    }
}
