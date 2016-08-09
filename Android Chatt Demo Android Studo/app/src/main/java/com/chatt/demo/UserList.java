package com.chatt.demo;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatt.demo.model.FriendUser;
import com.chatt.demo.model.UserAdapter;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.ConstClass;
import com.chatt.demo.utils.GPSTracker;
import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEInstallation;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.BEUser;
import com.csbm.GetDataCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Class UserList is the Activity class. It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */
    public class UserList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The Chat list.
     */
    private ArrayList<FriendUser> uList = new ArrayList<>();
    String strLocation;
    /**
     * The user.
     */
    public static BEUser user;
    GPSTracker gps;
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private Toolbar mToolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId;
    private boolean mUserSawDrawer = false;
    TextView txtUsername;
    FriendUser friendUser;
    private UserAdapter adapter =  new UserAdapter();
    ListView listView;
    ImageView imgIconUser;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);

        txtUsername = (TextView) findViewById(R.id.txtUsername);

        listView = (ListView) findViewById(R.id.list);
        imgIconUser = (ImageView) findViewById(R.id.iconUser);
        txtUsername.setText(getString(R.string.welcome) + " " + user.getUsername());

        String strCurrentUser = user.getUsername();
        if (strCurrentUser != null){
            BEInstallation installation = BEInstallation.getCurrentInstallation();
            installation.put("user", user.getUsername());
            installation.saveInBackground();
//            installation.get
        }
        updateUserStatus(true);

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
        loadUserList();
        // load icon user
        BEFile iconUser = user.getBEFile(ConstClass.USER_PICTURE_PROFILE);
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

    }
    private void navigate(int mSelectedId) {
        Intent intent = null;
        if (mSelectedId == R.id.navigation_item_profle){
            // go to profile activity

        }
        if (mSelectedId == R.id.navigation_item_addFriend){
            // go to find friend activity
            FindFriendActivity.user = user;
            intent = new Intent(UserList.this, FindFriendActivity.class);
            startActivity(intent);
        }
        if (mSelectedId == R.id.navigation_item_listFriend){
            // doo nothing because you are in there.
        }
        if (mSelectedId == R.id.navigation_item_logOut){
            updateUserStatus(false);
            user.logOut();
            intent = new Intent(UserList.this, Login.class);
            startActivity(intent);

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

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPermisstionLocation();
    }

    /* (non-Javadoc)
         * @see android.support.v4.app.FragmentActivity#onResume()
         */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserList();
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

    /**
     * Load list of users.
     */
    private void loadUserList() {
        ArrayList<String> listFriend = null;
        BEUser user = BEUser.getCurrentUser();
        BEQuery<BEObject> query = BEQuery.getQuery(ConstClass.FRIEND_LIST);
        query.whereEqualTo(getString(R.string.created_by), user);
        try {
            BEObject beObject = query.getFirst();
            listFriend = (ArrayList<String>) beObject.get(ConstClass.FRIEND_LIST_FRIENDS);

        } catch (BEException e) {
            e.printStackTrace();
        }

        if (listFriend != null){
            if (uList.size() != 0){
                uList.clear();
            }
            for (String mUser : listFriend) {
                try {
                    final BEUser tmpUser = BEUser.getQuery().whereEqualTo(ConstClass.USER_USERNAME, mUser).getFirst();
                    //load bitmap
                    BEFile fileImg = tmpUser.getBEFile(ConstClass.USER_PICTURE_PROFILE);
                    byte[] dataImg = fileImg.getData();
                    Bitmap bmp = BitmapFactory.decodeByteArray(dataImg, 0,dataImg.length);
                    friendUser = new FriendUser(bmp, tmpUser);
                    uList.add(friendUser);
                    adapter.notifyDataSetChanged();
                } catch (BEException e) {
                    e.printStackTrace();
                }
            }
            adapter.setUList(uList);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0,
                                        View arg1, int pos, long arg3) {
                    Intent intent = new Intent(UserList.this, Chat.class);
                    intent.putExtra(Const.EXTRA_DATA, uList.get(pos).getFriend()
                            .getUsername());
                    if (strLocation != null){
                        intent.putExtra(Const.DEFAULT_LOCATION, strLocation);
                    } else {
                        intent.putExtra(Const.DEFAULT_LOCATION, ConstClass.SENT);
                    }
                    startActivity(intent);
                }
            });

        } else {
        }
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
    public void getPermisstionLocation(){
        gps = new GPSTracker(UserList.this);
        if (gps.isCanGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongtitude();

            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // if got location add to extra. else return false;
            if (addresses.size() > 0){
                if (addresses.get(0).getSubAdminArea() != null){
                    strLocation = ConstClass.SENT + ": " + addresses.get(0).getSubAdminArea() + " - "
                            + addresses.get(0).getAdminArea();
                } else {
                    strLocation = ConstClass.SENT + ": " + addresses.get(0).getAdminArea();
                }
//   addresses.get(0).getAdminArea() + " - " + addresses.get(0).getSubAdminArea() addresses.get(0).getThoroughfare()
            }
        } else {
            gps.isShowSettingAlert();
        }
    }
}
