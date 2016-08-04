package com.csbm.wallpost;

import com.csbm.CSBM;
import com.csbm.wallpost.Utils.Const;

/**
 * Created by akela on 05/08/2016.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CSBM.initialize(new CSBM.Configuration.Builder(this)
                .applicationId(Const.APP_ID)
                .clientKey(Const.CLIENT_KEY)
                .server(Const.SERVER_IP)
                .build());
    }
}
