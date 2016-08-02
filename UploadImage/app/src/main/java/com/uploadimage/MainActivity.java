package com.uploadimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.GetCallback;
import com.csbm.GetDataCallback;
import com.csbm.ProgressCallback;
import com.csbm.SaveCallback;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int SLECTED_PICTURE = 1;
    ImageView imgView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgView = (ImageView) findViewById(R.id.imageView);
        Button btnLoad = (Button) findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new loadimg().execute("");
            }
        });

        BEObject object = new BEObject("TestObject");
        object.put("foo", "bar");
        object.saveInBackground();
    }


    public void btnClick(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SLECTED_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SLECTED_PICTURE:
                if (resultCode == RESULT_OK) {
                    // Get file path and decode to bitmap
                    Uri uri = data.getData();
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();
                    int columindex = cursor.getColumnIndex(projection[0]);
                    String filePath = cursor.getString(columindex);
                    cursor.close();
                    Bitmap imgSelected = BitmapFactory.decodeFile(filePath);
//
                    // Convert it to ByteArrayOutputStream
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // Compress image to lower quality scale 1 - 100
                    imgSelected.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] image = stream.toByteArray();
                    // Save image to cloud and to server
//                    new processdialog().execute(image);
                    //BEFile format
                    BEFile file = new BEFile(image, "image/png");
                    // Upload the image into Parse Cloud
                    file.saveInBackground(new SaveCallback() {
                        public void done(BEException e) {
                            // Handle success or failure here ...
                            if (e != null){
                                System.out.println(e.getMessage() + e.getCode());
                            }
                        }
                    });

                    // Create a New Class called "ImageUpload" in Parse
                    BEObject imgupload = new BEObject("ImageUpload");

                    // Create a column named "ImageName" and set the string
                    imgupload.put("ImageName", "AndroidBegin");

                    // Create a column named "ImageFile" and insert the image
                    imgupload.put("ImageFile", file);

                    // Create the class and the columns
                    imgupload.saveInBackground();

                } else {
                    Log.d("RESULT ERROR: ", "fail " + resultCode);
                }
                break;
        }
    }


    private class loadimg extends  AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(MainActivity.this, "", "Waitting for download...", true);
        }

        @Override
        protected String doInBackground(String... params) {

            BEQuery<BEObject> query = new BEQuery<>("ImageUpload");
            query.orderByDescending("createdAt");
            query.getFirstInBackground( new GetCallback<BEObject>() {
                @Override
                public void done(BEObject object, BEException e) {
                    if (e == null) {
                        //get data from column ImageFile
                        BEFile fileObject = (BEFile) object.get("ImageFile");

                        fileObject.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, BEException e) {
                                System.out.println("CO DATA: " + data.length);
                                Bitmap bmp = BitmapFactory
                                        .decodeByteArray(
                                                data, 0,
                                                data.length);

                                imgView.setImageBitmap(bmp);
                                mProgressDialog.dismiss();
                            }
                        });

                    } else {
                        mProgressDialog.dismiss();
                        System.out.println("load error " + e.getMessage());
                    }
                }
            });
            return null;
        }

    }
    private class processdialog extends AsyncTask<byte[], Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set your progress dialog Title
            mProgressDialog.setTitle("Upload image");
            // Set your progress dialog Message
            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            // Show progress dialog
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(byte[]... params) {
            //BEFile format
            BEFile file = new BEFile("android.png", params[0]);
            // Upload the image into Parse Cloud
            file.saveInBackground(new SaveCallback() {
                public void done(BEException e) {
                    // Handle success or failure here ...
                    if (e != null){
                        System.out.println(e.getMessage());
                    }
                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    publishProgress(percentDone);
                    if (percentDone == 100){
                        mProgressDialog.dismiss();
                    }
                }
            });

            // Create a New Class called "ImageUpload" in Parse
            BEObject imgupload = new BEObject("ImageUpload");

            // Create a column named "ImageName" and set the string
            imgupload.put("ImageName", "AndroidBegin");

            // Create a column named "ImageFile" and insert the image
            imgupload.put("ImageFile", file);

            // Create the class and the columns
            imgupload.saveInBackground();

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
