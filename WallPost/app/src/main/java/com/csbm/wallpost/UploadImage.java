package com.csbm.wallpost;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEUser;
import com.csbm.ProgressCallback;
import com.csbm.SaveCallback;
import com.csbm.wallpost.Utils.Const;

import java.io.ByteArrayOutputStream;

public class UploadImage extends AppCompatActivity {

    Button btnUpload;
    Button btnCancel;
    LinearLayout layoutImage;
    ImageView imageView;
    EditText comment;
    String txtComment;
    byte[] image;
    private static final int SLECTED_PICTURE = 1;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        layoutImage = (LinearLayout) findViewById(R.id.layoutImage);
        layoutImage.setVisibility(View.INVISIBLE);
        layoutImage.setVisibility(View.GONE);

        imageView = (ImageView) findViewById(R.id.imageView);
        comment = (EditText) findViewById(R.id.comment);
        btnUpload = (Button) findViewById(R.id.btnUploadImage);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null){
                    btnClick();
                } else {
                    txtComment = comment.getText().toString();
                    new saveImage().execute(image);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadImage.this, WallPost.class));
                finish();
            }
        });
    }



    public void btnClick(){
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, SLECTED_PICTURE);

//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, SLECTED_PICTURE);
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
                    image = stream.toByteArray();
                    layoutImage.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(imgSelected);

//                    System.out.println(txtComment);
                    // Save image to cloud and to server

                }else {
                    // error
                }
        }

    }


    public class saveImage extends AsyncTask<byte[], Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(UploadImage.this);
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
            final BEFile file = new BEFile( params[0], "image/png");
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

            BEObject object = new BEObject(Const.WALL_POST);

            // Create a column named "ImageName" and set the string
            // Create a column named "ImageFile" and insert the image
            object.put(Const.PROFILE_IMAGE, file);
            object.put(Const.COMMENT, txtComment);
            object.put(Const.USER, BEUser.getCurrentUser());
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            startActivity(new Intent(UploadImage.this, WallPost.class));
            finish();
        }
    }

}
