package com.csbm.uploadanddownloadphoto;

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
    private ProgressDialog mProgressDialog;
    ImageView imgView;
    Button btnLoad;
    Button btnupload;
    Button btnView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView = (ImageView) findViewById(R.id.imageView);
        btnupload = (Button) findViewById(R.id.uploadbtn);
        btnLoad = (Button) findViewById(R.id.btnLoad);
        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick();
            }
        });
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new loadImage().execute("");
            }
        });
        btnView = (Button) findViewById(R.id.btnView);
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewView.class);
                startActivity(intent);
            }
        });
    }



    public void btnClick(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SLECTED_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SLECTED_PICTURE:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();
                    int columindex = cursor.getColumnIndex(projection[0]);
                    String filePath = cursor.getString(columindex);
                    cursor.close();
                    Bitmap imgSelected = BitmapFactory.decodeFile(filePath);
//                    Bitmap imgSelected = BitmapFactory.decodeResource(getResources(), R.drawable.default_user);

                    // Convert it to ByteArrayOutputStream
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // Compress image to lower quality scale 1 - 100
                    imgSelected.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] image = stream.toByteArray();
                    // Save image to cloud and to server
//                    new processdialog().execute(image);
                }else {
                    // error
                }
        }

    }


    public class processdialog extends AsyncTask<byte[], Integer, String>{
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

            final BEFile file = new BEFile("android", params[0], "image/png");
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(BEException e) {
                    if (e != null){
                        System.out.println(e.getCode() + " ++ " + e.getMessage());
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer integer) {
                    publishProgress(integer);
                    if (integer == 100){
                        mProgressDialog.dismiss();
                    }
                }
            });

            //Create object ImageUpload

            BEObject object = new BEObject("ProfilePicture");

            // Create a column named "ImageName" and set the string
            object.put("ProfilePictureName", "defaultIconUser");
            // Create a column named "ImageFile" and insert the image
            object.put("ProfilePictureFile", file);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(BEException e) {
                    if (e != null){
                        System.out.println(e.getCode() + " - " + e.getMessage());
                    }
                }
            });
            // save()
            System.out.println("success");

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }
    }

    public class loadImage extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set your progress dialog Title
            mProgressDialog.setTitle("Download image");
            // Set your progress dialog Message
            mProgressDialog.setMessage("Uploading...");
            // Show progress dialog
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            BEQuery<BEObject> query = BEQuery.getQuery("ImageUpload");
            query.orderByDescending("createdAt");
            query.getFirstInBackground(new GetCallback<BEObject>() {
                @Override
                public void done(BEObject beObject, BEException e) {
                    BEFile fileObject = (BEFile) beObject.get("ImageFile");
                    fileObject.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, BEException e) {
                            Bitmap bmp = BitmapFactory
                                    .decodeByteArray(
                                            bytes, 0,
                                            bytes.length);

                            imgView.setImageBitmap(bmp);
                            mProgressDialog.dismiss();
                        }
                    });
                }
            });
            return null;
        }
    }
}
