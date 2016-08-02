package com.pushapplication;

import android.app.Application;

import com.csbm.BEInstallation;
import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by akela on 30/06/2016.
 */
public class ApplicationDemo extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        CSBM.initialize(this, "57740a108400b62b21348b8e", "C5cnnIiTvovkFdnnpCKG6uPn7UutNLkPuflveVkEWtRHlQOV");
//        ParseUser.enableAutomaticUser();
        Parse.initialize(this, "lrskGXegM4tF9b0ooapCU5wSUE8LvLgsX8ZTgiyG", "pZXADgOHvn4w2Y95sKXdUkliYHNePAS35m261mz0");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        BEInstallation.getCurrentInstallation().saveInBackground();


    }
}
