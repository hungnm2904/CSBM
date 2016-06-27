package com.csbmframework;

import android.app.Application;

import com.csbm.BEUser;
import com.csbm.CSBM;

/**
 * Created by akela on 09/06/2016.
 */
public class CSBMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CSBM.initialize(this, "123123", "123");

        BEUser.enableAutomaticUser();
//        BEACL roleACL = new BEACL();
//        roleACL.setPublicReadAccess(true);
//        BERole role = new BERole("Administrator", roleACL);
//        role.saveInBackground();

    }
}
