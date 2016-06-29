package com.uploadimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.GetCallback;
import com.csbm.GetDataCallback;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private  static final int SLECTED_PICTURE = 1;
    ImageView imgView;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgView = (ImageView) findViewById(R.id.imageView);


        // Locate the button in main.xml
//        Button button = (Button) findViewById(R.id.uploadbtn);

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Locate the image in res > drawable-hdpi
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
//                        R.drawable.thumb);
//                // Convert it to byte
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                // Compress image to lower quality scale 1 - 100
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] image = stream.toByteArray();
//
//                // Create the ParseFile
//                BEFile file = new BEFile("androidbegin.png", image);
//                // Upload the image into Parse Cloud
//                file.saveInBackground();
//
//                // Create a New Class called "ImageUpload" in Parse
//                BEObject imgupload = new BEObject("ImageUpload");
//
//                // Create a column named "ImageName" and set the string
//                imgupload.put("ImageName", "AndroidBegin Logo");
//
//                // Create a column named "ImageFile" and insert the image
//                imgupload.put("ImageFile", file);
//
//                // Create the class and the columns
//                imgupload.saveInBackground();
//
//                // Show a simple toast message
//                Toast.makeText(MainActivity.this, "Image Uploaded",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });





    }

    public void btnClick(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SLECTED_PICTURE);

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
                    Drawable drawable = new BitmapDrawable(imgSelected);
//                    imgView.setBackground(drawable);



                                    // Convert it to byte
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress image to lower quality scale 1 - 100
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imgSelected.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();

                // Create the ParseFile
                BEFile file = new BEFile("androidbegin.png", image);
                // Upload the image into Parse Cloud
                file.saveInBackground();

                // Create a New Class called "ImageUpload" in Parse
                BEObject imgupload = new BEObject("ImageUpload");

                // Create a column named "ImageName" and set the string
                imgupload.put("ImageName", "AndroidBegin Logo");

                // Create a column named "ImageFile" and insert the image
                imgupload.put("ImageFile", file);

                // Create the class and the columns
                imgupload.saveInBackground();

                // Show a simple toast message
                Toast.makeText(MainActivity.this, "Image Uploaded",
                        Toast.LENGTH_SHORT).show();
                    imgView.setImageBitmap(imgSelected);
//                    reloadImageFromServer();
                }
                break;
        }
    }


    public void reloadImageFromServer(){
//        progressDialog = ProgressDialog.show(MainActivity.this, "",
//                "Waiting for download image from server...", true);
        BEQuery<BEObject> query = new BEQuery<BEObject>(
                "ImageUpload");
        query.addAscendingOrder("createAt");
        query.getFirstInBackground(new GetCallback<BEObject>() {
            @Override
            public void done(BEObject object, BEException e) {
                if (e == null){
                    //get data from column ImageFile
                    BEFile fileObject = (BEFile) object.get("ImageFile");

                    fileObject.getDataInBackground(new GetDataCallback() {

                        public void done(byte[] data,
                                         BEException e) {
                            if (e == null) {
                                Log.d("test",
                                        "We've got data in data.");
                                // Decode the Byte[] into
                                // Bitmap
                                Bitmap bmp = BitmapFactory
                                        .decodeByteArray(
                                                data, 0,
                                                data.length);

                                imgView.setImageBitmap(bmp);

                                // Close progress dialog
//                                progressDialog.dismiss();

                            } else {
                                Log.d("ERROR: ",
                                        "There was a problem downloading the data.");
                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
