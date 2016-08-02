package com.csbmframework;

import com.csbm.CSBM;

/**
 * Created by akela on 19/07/2016.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .server("http://192.168.251.39:1337/csbm/")
                .applicationId("579b232865b7ff791b1dbce7")
                .build());
    }
}
