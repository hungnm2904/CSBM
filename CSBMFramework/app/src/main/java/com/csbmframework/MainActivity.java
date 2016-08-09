package com.csbmframework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.csbm.BEException;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.BERelation;
import com.csbm.FindCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // save data to cloud.


        BEQuery<BEObject> query = BEQuery.getQuery("Weapon");
        query.findInBackground(new FindCallback<BEObject>() {
            @Override
            public void done(List<BEObject> objects, BEException e) {
                BEObject alchemist = new BEObject("Alchemist");
                alchemist.put("NameAuthor", "Goku");
                BERelation<BEObject> relation = alchemist.getRelation("Name");
                relation.add(objects.get(0));
                relation.add(objects.get(1));
                alchemist.saveInBackground();
            }
        });

    }
}





