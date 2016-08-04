package com.csbm.uploadanddownloadphoto;

import com.csbm.CSBM;

/**
 * Created by akela on 11/07/2016.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .applicationId("57a32b539f7883732176d51d")
                .clientKey("123123")
                .server("http://192.168.1.20:1337/csbm/")
                .build());
    }
}
