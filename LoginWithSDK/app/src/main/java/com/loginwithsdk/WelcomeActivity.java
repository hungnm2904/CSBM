package com.loginwithsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

//    TextView currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        BEUser user = BEUser.getCurrentUser();
//        String strUser = user.getUsername();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("SSUER");
        final String strUser = bundle.getString("USER");
        TextView currentUser = (TextView) findViewById(R.id.username);
        currentUser.setText("You are logged in as " + strUser);

    }
}
