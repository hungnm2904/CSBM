package com.chatt.demo;

import android.app.Application;

import com.csbm.BEInstallation;
import com.csbm.BEUser;
import com.csbm.CSBM;


public class ChattApp extends Application{



	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate()
	{

		super.onCreate();
		CSBM.setLogLevel(CSBM.LOG_LEVEL_VERBOSE);

		CSBM.initialize(new CSBM.Configuration.Builder(this)
				.applicationId(getString(R.string.MY_APP_ID))
				.clientKey(getString(R.string.MY_APP_CLIENT_KEY))
				.server("http://" + getString(R.string.MY_SERVER_IP) + ":1337/csbm/")
				.build());
		BEInstallation.getCurrentInstallation().saveInBackground();

		BEUser.enableAutomaticUser();

	}
}
