package com.csbmframework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.csbm.BEException;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.BEUser;
import com.csbm.FindCallback;
import com.csbm.SaveCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Welcome extends AppCompatActivity {
    Button logout;
    Button ddosServer;
    Button btnSaveAll;
    int count = 0;
    int failure = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        logout = (Button) findViewById(R.id.btnLogout);
        ddosServer = (Button) findViewById(R.id.ddosServer);
        btnSaveAll = (Button) findViewById(R.id.btnSave);


        BEUser user = BEUser.getCurrentUser();
        final String strUser = user.getUsername().toString();
        TextView textUser = (TextView) findViewById(R.id.txtUser);
        textUser.setText("You are logged: " + strUser);

        ddosServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BEObject regis;
                for (int i = 0; i < 10000; i++) {
                    regis = new BEObject("Registration");
                    regis.put("username", "minhvu" + i);
                    regis.put("password", "123123");
                    regis.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(BEException e) {
                            if (e == null) {

                                count++;
                            } else {
                                failure++;
                            }
                        }
                    });
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("SUCCESS: " + count + " - " + "FAILURE: " + failure);
            }
        });

        btnSaveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BEQuery<BEObject> query = BEQuery.getQuery("Registration");
//                query.countInBackground(new CountCallback() {
//                    @Override
//                    public void done(int count, BEException e) {
//                        System.out.println("SIZE: " + count);
//                    }
//                });
                query.findInBackground(new FindCallback<BEObject>() {
                    @Override
                    public void done(List<BEObject> objects, BEException e) {
                        System.out.println("SIZE: " + objects.size());
                        String data = "";
                        for (BEObject object : objects){
                            BEProxyObject proxyObject = new BEProxyObject(object);
                            for (int i = 0; i < proxyObject.getListKey().size(); i++){
                                data = data + proxyObject.getListKey().get(i).toString();

//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e1) {
//                                    e1.printStackTrace();
//                                }
                            }
                        }
                        try {
                            writeData(data + "\n");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });


            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BEUser.logOut();
                Intent intent = new Intent(Welcome.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void writeData(String data) throws IOException {

        String filePath = "/sdcard/" + "test" + ".txt";
        File file = new File(filePath);
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream fo = new FileOutputStream(file);
        fo.write(data.getBytes());
//        FileWriter fw = new FileWriter(file.getAbsoluteFile());
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write(data);
//        bw.close();
        fo.close();
        System.out.println("Save success");

    }
}
