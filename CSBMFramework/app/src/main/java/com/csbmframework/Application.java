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
                .server("http://192.168.0.104:1337/csbm/")
                .applicationId("57a7271340ee3f5d44c6d231")
                .build());
    }
}
