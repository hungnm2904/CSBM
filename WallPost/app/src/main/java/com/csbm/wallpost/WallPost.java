package com.csbm.wallpost;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.FindCallback;
import com.csbm.GetDataCallback;
import com.csbm.wallpost.Utils.Const;
import com.csbm.wallpost.Utils.Images;
import com.csbm.wallpost.Utils.WallPostAdapter;

import java.util.ArrayList;
import java.util.List;

public class WallPost extends AppCompatActivity {
    private ProgressDialog mProgressDialog;
    ArrayList<Images> listImages = new ArrayList<>();
    private WallPostAdapter adapter = new WallPostAdapter();
    ListView listImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_post);
        // load all image
        listImage = (ListView) findViewById(R.id.listImage);
        new loadAllImage().execute("");


    }


    public class loadAllImage extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(WallPost.this);
            // Set your progress dialog Title
            mProgressDialog.setTitle("Load image");
            // Set your progress dialog Message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            // Show progress dialog
            mProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            loadAll();

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            adapter.setListImg(listImages);
            listImage.setAdapter(adapter);
            mProgressDialog.dismiss();
        }
    }


    public void loadAll() {
        BEQuery<BEObject> query = BEQuery.getQuery("WallPost");
        query.findInBackground(new FindCallback<BEObject>() {
            public void done(List<BEObject> listResult, BEException e) {
                if (e == null) {
                    for (final BEObject object : listResult) {
                        BEFile fileObject = object.getBEFile(Const.PROFILE_IMAGE);
                        fileObject.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, BEException e) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                listImages.add(new Images(bmp, object.getString(Const.COMMENT), object.getCreatedAt()));
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } else {
                    Log.d("WallPost", "Error: " + e.getMessage());
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
        if (id == R.id.upload_image) {
            // go to page upload
            startActivity(new Intent(WallPost.this, UploadImage.class));
            finish();

        }

        return super.onOptionsItemSelected(item);
    }



}
