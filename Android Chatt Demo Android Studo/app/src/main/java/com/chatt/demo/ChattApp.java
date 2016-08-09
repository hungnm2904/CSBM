package com.chatt.demo;

import android.app.Application;

import com.chatt.demo.utils.ConstClass;
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
				.applicationId(ConstClass.APP_ID)
				.clientKey(ConstClass.CLIENT_KEY)
				.server(ConstClass.SERVER_IP)
				.build());
		BEInstallation.getCurrentInstallation().saveInBackground();

		BEUser.enableAutomaticUser();

	}
}
