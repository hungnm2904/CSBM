package com.csbmframework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.csbm.BEException;
import com.csbm.BEUser;
import com.csbm.LogInCallback;
import com.csbm.SignUpCallback;

public class MainActivity extends AppCompatActivity {

    EditText txtUsername;
    EditText txtPassword;
    Button btnLogin;
    Button btnSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.login);
        btnSignup = (Button) findViewById(R.id.signup);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();
                if (username.equals("") && password.equals("")){
                    Toast.makeText(getApplicationContext(), "You must complate all field", Toast.LENGTH_SHORT).show();
                } else {
                    BEUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(BEUser user, BEException e) {
                            if (e == null){
                                Intent intent = new Intent(MainActivity.this, Welcome.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Account doesn't exist. Please signup.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();
                if (username.equals("") && password.equals("")){
                    Toast.makeText(getApplicationContext(), "You must complate all field", Toast.LENGTH_SHORT).show();
                } else {
                    BEUser user = new BEUser();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(BEException e) {
                            if (e == null){
                                Intent intent = new Intent(MainActivity.this, Welcome.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "ERROR " + e.getCode() + " - " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
