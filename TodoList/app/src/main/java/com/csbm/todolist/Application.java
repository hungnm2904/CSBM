package com.csbm.todolist;

import com.csbm.CSBM;

/**
 * Created by akela on 20/07/2016.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .applicationId(getString(R.string.YOUR_APP_ID))
                .server("http://" + getString(R.string.YOUR_SERVER_IP) + ":1337/csbm/")
                .build());
    }
}
