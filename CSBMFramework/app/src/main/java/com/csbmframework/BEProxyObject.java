package com.csbmframework;

import com.csbm.BEObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by akela on 10/06/2016.
 */
public class BEProxyObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashMap<String, Object> values = new HashMap<String, Object>();


    private List<String> listKey = new ArrayList<String>();

    public List<String> getListKey() {
        return listKey;
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public BEProxyObject(BEObject object) {

        // Loop the keys in the ParseObject
        for(String key : object.keySet()) {
//

            String value = object.get(key).toString();
//            System.out.println(key + " " + value);
            listKey.add(key + " - " + value);

//            @SuppressWarnings("rawtypes")
//            Class classType = object.get(key).getClass();
//            if(classType == byte[].class || classType == String.class ||
//                    classType == Integer.class || classType == Boolean.class) {
//                values.put(key, object.get(key));
//            } else if(classType == BEUser.class) {
//                BEProxyObject parseUserObject = new BEProxyObject((BEObject)object.get(key));
//                values.put(key, parseUserObject);
//            } else {
//                // You might want to add more conditions here, for embedded ParseObject, ParseFile, etc.
//            }
        }
    }


    public String getString(String key) {
        if(has(key)) {
            return (String) values.get(key);
        } else {
            return "";
        }
    }

    public int getInt(String key) {
        if(has(key)) {
            return (Integer)values.get(key);
        } else {
            return 0;
        }
    }

    public Boolean getBoolean(String key) {
        if(has(key)) {
            return (Boolean)values.get(key);
        } else {
            return false;
        }
    }

    public byte[] getBytes(String key) {
        if(has(key)) {
            return (byte[])values.get(key);
        } else {
            return new byte[0];
        }
    }

    public BEProxyObject getParseUser(String key) {
        if(has(key)) {
            return (BEProxyObject) values.get(key);
        } else {
            return null;
        }
    }

    public Boolean has(String key) {
        return values.containsKey(key);
    }
}
