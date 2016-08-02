package com.csbmframework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.ProgressCallback;
import com.csbm.SaveCallback;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // save data to cloud.
        byte[] data = "text file generate to byte data".getBytes();
        BEFile file = new BEFile(data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(BEException e) {
                // handle succeed or error save file
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                // Update your progress spinner here. percentDone will be between 0 and 100.
            }
        });
        // associate file onto a object just like any other piece of data.
        BEObject dataApplication = new BEObject("DataApplication");
        dataApplication.put("name", "ErrorData");
        dataApplication.put("fileData", file);
        dataApplication.saveInBackground();

    }
}
