package com.csbm.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.csbm.BEException;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.BEUser;
import com.csbm.FindCallback;
import com.csbm.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class Welcome extends AppCompatActivity {

    Button addTitle;
    EditText editText;
    ListView lv;
    ArrayList<String> arrayList = null;
    ViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        editText = (EditText) findViewById(R.id.editText);
        arrayList = new ArrayList<>();
        addTitle = (Button) findViewById(R.id.addTitle);
        adapter = new ViewAdapter();
        lv = (ListView) findViewById(R.id.listItem);

        addTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToList();
            }
        });

        // load list title
        loadList();

    }

    public void loadList(){
        BEQuery<BEObject> query = BEQuery.getQuery(getString(R.string.Titlte));
        query.whereEqualTo(getString(R.string.username), BEUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<BEObject>() {
            @Override
            public void done(List<BEObject> list, BEException e) {
                for (BEObject item: list){
                    arrayList.add(item.getString(getString(R.string.title)));
                }
                adapter.setListTitle(arrayList);
                lv.setAdapter(adapter);
                editText.setText("");
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Toast.makeText(getApplicationContext(),
                                "You are clicked: " + arrayList.get(position),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    public void addToList(){
        String text = editText.getText() + "";
        if (text.equals("")){
            return;
        } else {
            // save to server and add to list view

            BEObject title = new BEObject(getString(R.string.Titlte));
            title.put(getString(R.string.username), BEUser.getCurrentUser().getUsername());
            title.put(getString(R.string.title), text);
            title.saveInBackground(new SaveCallback() {
                @Override
                public void done(BEException e) {
                    if (e == null) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.msg_add_succeed),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Error " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

            // list view
            arrayList.add(text);
            adapter.setListTitle(arrayList);
            lv.setAdapter(adapter);
            editText.setText("");
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Toast.makeText(getApplicationContext(),
                            "You are clicked: " + arrayList.get(position),
                            Toast.LENGTH_SHORT).show();
                }
            });


        }

    }
    public void logOut(){
        BEUser.logOut();
        Intent intent = new Intent(Welcome.this, LoginSignupActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int idSelected = item.getItemId();

        if (idSelected == R.id.action_logOut){
            String strUser = BEUser.getCurrentUser().getUsername().toString();
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_logout))
                    .setMessage(getString(R.string.confirm_logout) + strUser + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logOut();
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
