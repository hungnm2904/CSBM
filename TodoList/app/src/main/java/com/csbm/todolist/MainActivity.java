package com.csbm.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (BEAnonymousUtils.isLinked(BEUser.getCurrentUser())) {

            Intent intent = new Intent(MainActivity.this, LoginSignupActivity.class);
            startActivity(intent);
            finish();
//        } else {
//            BEUser currentUser = BEUser.getCurrentUser();
//            if (currentUser != null) {
//                Intent intent = new Intent(MainActivity.this, Welcome.class);
//                startActivity(intent);
//                finish();
//            } else {
//                Intent intent = new Intent(MainActivity.this, LoginSignupActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        }
    }
}
