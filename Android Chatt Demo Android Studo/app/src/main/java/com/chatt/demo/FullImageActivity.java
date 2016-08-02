package com.chatt.demo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.utils.Const;
import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.GetCallback;
import com.csbm.GetDataCallback;

public class FullImageActivity extends CustomActivity {
    private String imageId;
    ImageView imageFullSize;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        imageFullSize = (ImageView) findViewById(R.id.imageFullSize);
        imageId = getIntent().getStringExtra(Const.IMAGE_ID);
        new loadImageFullSize().execute(imageId);
        getActionBar().setTitle("Preview");
        //load image
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//        int width = (int) (dm.widthPixels*.8);
//        int height = (int) (dm.heightPixels*.8);
//        getWindow().setLayout(width, height);
    }


    public class loadImageFullSize extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(FullImageActivity.this);
            // Set your progress dialog Title
            mProgressDialog.setTitle("Download image");
            // Set your progress dialog Message
            mProgressDialog.setMessage("Uploading...");
            // Show progress dialog
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            BEQuery<BEObject> query = BEQuery.getQuery(getString(R.string.ImageUpload));
            query.getInBackground(params[0] , new GetCallback<BEObject>() {
                @Override
                public void done(BEObject beObject, BEException e) {
                    if (e == null) {
                        BEFile fileObject = (BEFile) beObject.get(getString(R.string.ImageFile));
                        fileObject.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, BEException ex) {
                                if (ex == null) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                                    imageFullSize.setImageBitmap(bmp);
                                    mProgressDialog.dismiss();
                                } else {
                                    System.out.println(ex.getMessage() + "+++LOAD BITMAP+++++" + ex.getCode());
                                }
                            }
                        });

                    } else {
                        System.out.println(e.getMessage() + "===LOAD FILE==" + e.getCode() + e.getCause());
                    }

                }
            });
            return null;
        }
    }
}
