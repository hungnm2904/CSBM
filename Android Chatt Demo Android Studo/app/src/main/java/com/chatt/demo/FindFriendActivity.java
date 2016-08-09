package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatt.demo.model.ListFriendSearch;
import com.chatt.demo.model.ListFriendSearchAdapter;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.ConstClass;
import com.chatt.demo.utils.GPSTracker;
import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEGeoPoint;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.BEUser;
import com.csbm.FindCallback;
import com.csbm.GetCallback;
import com.csbm.GetDataCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindFriendActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    public static BEUser user;
//    Button btnAdd;
//    EditText txtName;
    private Toolbar mToolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId;
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    ImageView imgIconUser;
    TextView txtUsername;
    GPSTracker gps;
    BEGeoPoint currentGeo;
    ListView listViewSearched;

    private ArrayList<ListFriendSearch> listUserSearched = new ArrayList<>();
    private ListFriendSearchAdapter adapter = new ListFriendSearchAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

//        btnAdd = (Button) findViewById(R.id.btnAdd);
        txtUsername = (TextView) findViewById(R.id.txtUsername);
        imgIconUser = (ImageView) findViewById(R.id.iconUser);
        listViewSearched = (ListView) findViewById(R.id.listResultFriend);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        txtUsername.setText(getString(R.string.welcome) + " " + user.getUsername());
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        mDrawer = (NavigationView) findViewById(R.id.main_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        navigate(mSelectedId);
//        btnAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                new addFriend().execute("");
//            }
//        });
        // load icon user
        BEFile iconUser = user.getBEFile("userPicture");
        iconUser.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, BEException e) {
                Bitmap bmp = BitmapFactory
                        .decodeByteArray(
                                bytes, 0,
                                bytes.length);

                imgIconUser.setImageBitmap(bmp);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        loadListBuddyAround();
                                    }
                                }
        );


//        loadListBuddyAround();

    }

    @Override
    public void onRefresh() {
        loadListBuddyAround();
    }

    public void loadListBuddyAround() {
        swipeRefreshLayout.setRefreshing(true);
        // must update location and refresh
        updateLocation();
        if (listUserSearched.size() > 0){
            listUserSearched.clear();
        }
        ArrayList<String> listFriend = null;
        BEUser user = BEUser.getCurrentUser();
//        String strUser = user.getUsername();
        BEQuery<BEObject> query = BEQuery.getQuery(ConstClass.FRIEND_LIST);
        query.whereEqualTo(ConstClass.FRIEND_LIST_CREATED_BY, user);
        try {
            BEObject beObject = query.getFirst();
            listFriend = (ArrayList<String>) beObject.get(getString(R.string.friends));
        } catch (BEException e) {
            e.printStackTrace();
        }
        BEQuery<BEUser> queryUser = BEUser.getQuery();
//        query1.whereNear("userLocation", currentGeo);
        queryUser.whereWithinKilometers("userLocation", user.getBEGeoPoint("userLocation"), 50);
        queryUser.whereNotEqualTo(ConstClass.USER_USERNAME, user.getUsername());
        if (isEmptyListFriend(listFriend)) {
            Log.d("Check ", "list friend is empty");
        } else {
            queryUser.whereNotContainedIn(getString(R.string.user_name), listFriend);
            Log.d("Check", "run when list friend is not empty");
        }
        queryUser.findInBackground(new FindCallback<BEUser>() {
            @Override
            public void done(List<BEUser> listUser, BEException e) {
                Log.d("Check lis user search", listUser.size() + " ");
                for (final BEUser userItem : listUser){
                    BEFile fileObject = userItem.getBEFile(ConstClass.USER_PICTURE_PROFILE);
                    fileObject.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, BEException e) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            double distanceKm = currentGeo.distanceInKilometersTo(userItem.getBEGeoPoint("userLocation"));
                            listUserSearched.add(new ListFriendSearch(bmp, userItem, distanceKm));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        swipeRefreshLayout.setRefreshing(false);
        adapter.setListUser(listUserSearched);
        listViewSearched.setAdapter(adapter);
        listViewSearched.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String username = listUserSearched.get(position).getBuddyUser().getUsername();

                Toast.makeText(getApplicationContext()
                        , "Add succeed " + username + " to list friend."
                        , Toast.LENGTH_LONG).show();

                new addFriend().execute(username);

                listUserSearched.remove(position);
                adapter.notifyDataSetChanged();

            }
        });
    }

    public boolean isEmptyListFriend(Iterable<?> listFriend) {
        for (Object userBuddy : listFriend)
            if (userBuddy != null) return false;
        return true;
    }

    public List<BEUser> getListUser(String username) {
        final List<BEUser> result = new ArrayList<>();
        BEQuery<BEUser> query = BEUser.getQuery();
        query.whereEqualTo(ConstClass.USER_USERNAME, username);
        try {
            BEUser beUser = query.getFirst();
            beUser.put("friend", user.getUsername());
            user.put("friend", username);
            result.add(beUser);
            result.add(user);
        } catch (BEException e) {
            e.printStackTrace();
        }

        return result;

    }

    public class addFriend extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(FindFriendActivity.this, "", getString(R.string.processing), true);
        }

        @Override
        protected String doInBackground(String... params) {
            List<BEUser> result = getListUser(params[0]);
            updateListFriend(result);
            mProgressDialog.dismiss();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            loadUserList();
//            Intent intent = getIntent();
//            startActivity(intent);
//            finish();

        }
    }

    public void updateListFriend(List<BEUser> listToUpdate) {
        BEQuery<BEObject> query;
        for (final BEUser item : listToUpdate) {
            query = BEQuery.getQuery(ConstClass.FRIEND_LIST);
            query.whereEqualTo(ConstClass.FRIEND_LIST_CREATED_BY, item);
            query.getFirstInBackground(new GetCallback<BEObject>() {
                @Override
                public void done(BEObject beObject, BEException e) {
                    if (e == null) {
                        beObject.addAllUnique(ConstClass.FRIEND_LIST_FRIENDS, Arrays.asList(item.getString("friend")));
                        beObject.saveInBackground();
                    } else {
                        Log.d("ERROR Update :", e.getCode() + " - " + e.getMessage());
                    }

                }
            });

        }
    }

    private void navigate(int mSelectedId) {
        Intent intent = null;
        if (mSelectedId == R.id.navigation_item_profle) {
            // go to profile activity
        }
        if (mSelectedId == R.id.navigation_item_addFriend) {
            // do nothing because you are in there
        }
        if (mSelectedId == R.id.navigation_item_listFriend) {
            UserList.user = user;
            intent = new Intent(FindFriendActivity.this, UserList.class);
            startActivity(intent);
        }
        if (mSelectedId == R.id.navigation_item_logOut) {
            // go to login activity
            updateUserStatus(false);
            user.logOut();
            intent = new Intent(FindFriendActivity.this, Login.class);
            startActivity(intent);
        }
    }

    /**
     * Update user status.
     *
     * @param online true if user is online
     */
    private void updateUserStatus(boolean online) {
        user.put(getString(R.string.online), online);
        user.saveEventually();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //get permission GPS
        updateLocation();
    }

    public void updateLocation(){
        gps = new GPSTracker(FindFriendActivity.this);
        if (gps.isCanGetLocation()) {
            currentGeo = new BEGeoPoint();
            if (gps.getLatitude() == 0.0) {
                currentGeo.setLatitude(Const.DEFAULT_LATITUDE);
            } else {
                currentGeo.setLatitude(gps.getLatitude());
            }
            if (gps.getLongtitude() == 0.0) {
                currentGeo.setLongitude(Const.DEFAULT_LONGITUDE);
            } else {
                currentGeo.setLongitude(gps.getLongtitude());
            }

            user.put("userLocation", currentGeo);
            user.saveInBackground();
        } else {
            gps.isShowSettingAlert();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();

        navigate(mSelectedId);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
