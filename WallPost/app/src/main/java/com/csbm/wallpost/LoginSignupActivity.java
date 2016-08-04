package com.csbm.wallpost;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.csbm.BEException;
import com.csbm.BEUser;
import com.csbm.LogInCallback;
import com.csbm.SignUpCallback;
import com.csbm.wallpost.Utils.Const;

/**
 * A login screen that offers login via email/password.
 */
public class LoginSignupActivity extends AppCompatActivity {

    private AutoCompleteTextView username;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        // Set up the login form.
        // Set up the login form.
        username = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndLogin();
            }
        });
    }

    private void validateAndLogin() {
        // Reset errors.
        username.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = username.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            username.setError(getString(R.string.error_field_required));
            focusView = username;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            // go to login
            loginOrRegister(user, password);

        }
    }

    private void loginOrRegister(final String user, final String password) {
        final ProgressDialog dia = ProgressDialog.show(this, null,
                Const.ALERT_WAIT);
        BEUser.logInInBackground(user, password, new LogInCallback() {

            @Override
            public void done(BEUser pu, BEException e) {
                dia.dismiss();
                if (pu != null) {
                    startActivity(new Intent(LoginSignupActivity.this, WallPost.class));
                    finish();
                } else {
                    // user not exist => register
                    if (e.getCode() == 101){
                        new AlertDialog.Builder(LoginSignupActivity.this)
                                .setTitle("Error login!")
                                .setMessage("Username do not exist. Do you want register with " + user)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BEUser regis = new BEUser();
                                        regis.setUsername(user);
                                        regis.setPassword(password);
                                        regis.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(BEException e) {
                                                if (e== null){
                                                    // go to next page
                                                    startActivity(new Intent(LoginSignupActivity.this, WallPost.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(getApplicationContext()
                                                            , "Error: " + e.getMessage()
                                                            , Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();
                    } else {
                        Toast.makeText(getApplicationContext()
                                , "Error: " + e.getMessage()
                                , Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}

