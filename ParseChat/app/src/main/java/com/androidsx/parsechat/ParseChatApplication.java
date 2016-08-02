package com.androidsx.parsechat;

import android.app.Application;

import com.parse.Parse;

public class ParseChatApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, Constants.APP_ID,Constants.CLIENT_KEY);
	}

}
