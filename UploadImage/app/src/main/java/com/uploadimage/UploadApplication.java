package com.uploadimage;

import android.app.Application;

import com.csbm.CSBM;

/**
 * Created by akela on 27/06/2016.
 */
public class UploadApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .applicationId("5776340b6d318c920fc59844")
                .clientKey("ykYGQVXfmA1yAvOdvnnUPXZs")
                .server("http://192.168.251.11:1337/csbm/")
                .build());

    }
}
