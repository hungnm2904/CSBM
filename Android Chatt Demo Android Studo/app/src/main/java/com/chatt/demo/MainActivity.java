package com.chatt.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chatt.demo.utils.GPSTracker;
import com.csbm.BEAnonymousUtils;
import com.csbm.BEUser;

public class MainActivity extends AppCompatActivity {
    GPSTracker gps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Button button = (Button) findViewById(R.id.btnGPS);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gps = new GPSTracker(MainActivity.this);
//                if (gps.isCanGetLocation()){
//                    double latitude = gps.getLatitude();
//                    double longitude = gps.getLongtitude();
//                    Toast.makeText(getApplicationContext()
//                            , latitude + " - " + longitude
//                            , Toast.LENGTH_SHORT).show();
//                } else {
//                    gps.isShowSettingAlert();
//                }
//            }
//        });

////         if current user  null go to login
        if(BEAnonymousUtils.isLinked(BEUser.getCurrentUser())){
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            BEUser currUser = BEUser.getCurrentUser();
            if (currUser != null ){
                // go to userlist
                UserList.user = currUser;
                Intent intent = new Intent(MainActivity.this, UserList.class);
                startActivity(intent);
                finish();

            } else {
                // go to login
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        }
    }


}
