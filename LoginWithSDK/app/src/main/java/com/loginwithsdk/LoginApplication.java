package com.loginwithsdk;

import android.app.Application;

import com.csbm.CSBM;

/**
 * Created by akela on 27/06/2016.
 */
public class LoginApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        CSBM.initialize(this, "lrskGXegM4tF9b0ooapCU5wSUE8LvLgsX8ZTgiyG", "pZXADgOHvn4w2Y95sKXdUkliYHNePAS35m261mz0");
        CSBM.initialize(this, "57740a108400b62b21348b8e", "123123");
    }
}
