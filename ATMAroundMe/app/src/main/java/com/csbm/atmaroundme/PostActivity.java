package com.csbm.atmaroundme;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.csbm.BEACL;
import com.csbm.BEException;
import com.csbm.BEGeoPoint;
import com.csbm.SaveCallback;

public class PostActivity extends AppCompatActivity {

    EditText latitude;
    EditText longitude;
    EditText bankName;
    EditText bankAddress;
    Button btnPost;
    private BEGeoPoint geoPoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        latitude = (EditText) findViewById(R.id.latitude);
        longitude = (EditText) findViewById(R.id.longitude);
        bankName = (EditText) findViewById(R.id.bankName);
        bankAddress = (EditText) findViewById(R.id.bankAddress);
        btnPost = (Button) findViewById(R.id.post_button);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set up a progress dialog
                final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
                dialog.setMessage(getString(R.string.progress_post));
                dialog.show();


                double laGeo = Double.parseDouble(latitude.getText().toString());
                double longGeo = Double.parseDouble(longitude.getText().toString());
                String strBankName = bankName.getText().toString();
                String strBankAddress = bankAddress.getText().toString();
                geoPoint = new BEGeoPoint(laGeo, longGeo);
                ATMAround post = new ATMAround();
                post.setBankName(strBankName);
                post.setAddress(strBankAddress);
                post.setLocation(geoPoint);
                BEACL acl = new BEACL();

                // Give public read access
                acl.setPublicReadAccess(true);
                post.setACL(acl);

                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(BEException e) {
                        dialog.dismiss();
                        finish();
                    }
                });
            }
        });
    }
}
