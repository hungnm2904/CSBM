package com.csbm.starterproject;

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
//        BEObject object = null;
//        BEObject object1 = new BEObject("Author");
//        object1.put("authors", "hungnguyen");
//        object1.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(BEException e) {
//                if (e != null){
//                    System.out.println("ERROR: " + e.getMessage());
//                }
//
//            }
//        });
//        BEObject object2 = new BEObject("Author");
//        object2.put("authors", "toantq");
//        object2.saveInBackground();
//        BEObject object3 = new BEObject("Author");
//        object3.put("authors", "locnguyen");
//        object3.saveInBackground();
//

        BEQuery<BEObject> query = BEQuery.getQuery("Author");
            query.findInBackground(new FindCallback<BEObject>() {
                @Override
                public void done(List<BEObject> list, BEException e) {
                    BEObject book = new BEObject("Book");

                    BERelation<BEObject> relation = book.getRelation("authors");
                    relation.add(list.get(0));
                    relation.add(list.get(1));
                    relation.add(list.get(2));
                    book.saveInBackground();
                }
            });
//
//        BEObject book = new BEObject("Book");

// now let’s associate the authors with the book
// remember, we created a "authors" relation on Book
//        BERelation<BEObject> relation = book.getRelation("authors");
//        relation.add(object);
//        relation.add(object2);
//        relation.add(object3);

// now save the book object
//        book.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(BEException e) {
//                if (e!= null){
//                    System.out.println("Error " + e.getMessage() + e.getCode());
//                } else {
//                    System.out.println("SUCCEED");
//                }
//            }
//        });
    }
}
