package com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.EditText;

public class PopupActivity extends AppCompatActivity {

    EditText txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int) (dm.widthPixels*0.8);
        int height = (int) (dm.heightPixels*0.6);

        getWindow().setLayout(width,height);
        txt = (EditText) findViewById(R.id.txt);
        txt.setText("This is demo popup activity");
    }
}
