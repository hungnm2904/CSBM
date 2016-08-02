package com.csbm.todolist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.csbm.BEException;
import com.csbm.BEUser;
import com.csbm.LogInCallback;
import com.csbm.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class LoginSignupActivity extends AppCompatActivity {

    Button loginButton;
    Button signupButton;
    String txtUsername;
    String txtPassword;
    EditText username;
    EditText password;
    List<String> listKey = new ArrayList<>();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);
        signupButton = (Button) findViewById(R.id.signup);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtUsername = username.getText().toString();
                txtPassword = password.getText().toString();

                BEUser.logInInBackground(txtUsername, txtPassword, new LogInCallback() {
                    @Override
                    public void done(BEUser user, BEException e) {
                        if (e == null){
                            //go to welcome
                            Intent intent = new Intent(LoginSignupActivity.this, Welcome.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("ERROR: ", e.getMessage());

                        }
                    }
                });



            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtUsername = username.getText().toString();
                txtPassword = password.getText().toString();
                if (txtUsername.equals("") && txtPassword.equals("")) {
                    Toast.makeText(getApplicationContext(), "Complete the sign up form",
                            Toast.LENGTH_SHORT).show();
                } else {
                    BEUser user = new BEUser();
                    user.setUsername(txtUsername);
                    user.setPassword(txtPassword);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(BEException e) {
                            if (e == null){
                                //go to welcome
                                Intent intent = new Intent(LoginSignupActivity.this, Welcome.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.d("SIGNIN ERROR: ", e.getMessage());
                            }
                        }
                    });


                }
            }
        });
    }


}
