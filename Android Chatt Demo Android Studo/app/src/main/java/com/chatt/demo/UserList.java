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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatt.demo.model.UserAdapter;
import com.chatt.demo.utils.Const;
import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.BEUser;
import com.csbm.GetCallback;
import com.csbm.GetDataCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class UserList is the Activity class. It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */
    public class UserList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The Chat list.
     */
    private ArrayList<BEUser> uList = new ArrayList<>();
//    private ArrayList<BEUser> listUserFriend = new ArrayList<>();


    EditText txtName;

    /**
     * The user.
     */
    public static BEUser user;
//    public static BEUser otheruser;
    Button btnAdd;

    private static final String SELECTED_ITEM_ID = "selected_item_id";
//    private static final String FIRST_TIME = "first_time";
    private Toolbar mToolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId;
    private boolean mUserSawDrawer = false;
    TextView txtUsername;
    private ProgressDialog mProgressDialog;
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
        btnAdd = (Button) findViewById(R.id.btnAdd);
        txtUsername = (TextView) findViewById(R.id.txtUsername);

        listView = (ListView) findViewById(R.id.list);
        imgIconUser = (ImageView) findViewById(R.id.iconUser);
        txtUsername.setText(getString(R.string.welcome) + " " + user.getUsername());
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new addFriend().execute("");
            }
        });

//        String strCurrentUser = user.getUsername();
//        if (strCurrentUser != null){
//            BEInstallation installation = BEInstallation.getCurrentInstallation();
//            installation.put("user", user.getUsername());
//            installation.saveInBackground();
////            installation.get
//        }

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


    }

    private void navigate(int mSelectedId) {
        Intent intent = null;
        if (mSelectedId == R.id.navigation_item_1){
            // go to profile activity

        }
        if (mSelectedId == R.id.navigation_item_2){
            // go to find friend activity

        }
        if (mSelectedId == R.id.navigation_item_3){
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


    public List<BEUser> getListUser(){
        //get value search friend
        txtName = (EditText) findViewById(R.id.addFriend);
        final String friendUser = txtName.getText().toString();
        //query name friend if exist update friend for both user.
        final List<BEUser> result = new ArrayList<>();
        BEQuery<BEUser> query = BEUser.getQuery();
        query.whereEqualTo("username", friendUser);
        try {
            BEUser beUser = query.getFirst();
            beUser.addAllUnique("friend", Arrays.asList(user.getUsername()));
            user.addAllUnique("friend", Arrays.asList(friendUser));
            result.add(beUser);
            result.add(user);
        } catch (BEException e) {
            e.printStackTrace();
        }
//        query.getFirstInBackground(new GetCallback<BEUser>() {
//            @Override
//            public void done(BEUser beUser, BEException e) {
//                if (e ==null){
//                    //exist user friend
//                    // add user friend to list
//                    beUser.addAllUnique("friend", Arrays.asList(user.getUsername()));
//                    user.addAllUnique("friend", Arrays.asList(friendUser));
//                    result.add(beUser);
//                    result.add(user);
//
//                } else {
//                    System.out.println("ERROR Get List : " + e.getCode() + " - " + e.getMessage());
//                }
//            }
//        });

        return result;

        //after done reload list


    }

    public void updateListFriend(List<BEUser> listToUpdate){
        BEQuery<BEObject> query;
        for (final BEUser items : listToUpdate){
            query = BEQuery.getQuery("FriendList");
            query.whereEqualTo("createdBy", items.getUsername());
            query.getFirstInBackground(new GetCallback<BEObject>() {
                @Override
                public void done(BEObject beObject, BEException e) {
                    if (e == null){
                        beObject.put("friends", items.get("friend"));
                        beObject.saveInBackground();
                    } else {
                        Log.d("ERROR Update :" , e.getCode() + " - " + e.getMessage());
                    }

                }
            });

        }
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus(false);
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

        ArrayList<String> listFriend = null;
        BEUser user = BEUser.getCurrentUser();
        String strUser = user.getUsername();
        BEQuery<BEObject> query = BEQuery.getQuery(getString(R.string.friend_list));
        query.whereEqualTo(getString(R.string.created_by), strUser);
        try {
            BEObject beObject = query.getFirst();
            listFriend = (ArrayList<String>) beObject.get(getString(R.string.friends));

        } catch (BEException e) {
            e.printStackTrace();
        }

        if (listFriend != null){
            if (uList.size() != 0){
                uList.clear();
            }
            for (String mUser : listFriend) {
                try {
                    BEUser tmpUser = BEUser.getQuery().whereEqualTo(getString(R.string.user_name), mUser).getFirst();
                    uList.add(tmpUser);
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
                    intent.putExtra(Const.EXTRA_DATA, uList.get(pos)
                            .getUsername());
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


    public class addFriend extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(UserList.this, "", getString(R.string.processing), true);
        }

        @Override
        protected String doInBackground(String... params) {
            List<BEUser> result = getListUser();
            updateListFriend(result);
            mProgressDialog.dismiss();

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadUserList();
            Intent intent = getIntent();
            startActivity(intent);
            finish();

        }
    }



}
