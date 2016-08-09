package com.csbm.atmaroundme;

import com.csbm.BEClassName;
import com.csbm.BEGeoPoint;
import com.csbm.BEObject;
import com.csbm.BEQuery;

/**
 * Created by akela on 09/08/2016.
 */
@BEClassName("ATMAround")
public class ATMAround extends BEObject {

    public String getBankName() {
        return getString("bankName");
    }

    public void setBankName(String value) {
        put("bankName", value);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String value) {
        put("address", value);
    }


//    public BEUser getUser() {
//        return getBEUser("user");
//    }
//
//    public void setUser(BEUser value) {
//        put("user", value);
//    }

    public BEGeoPoint getLocation() {
        return getBEGeoPoint("location");
    }

    public void setLocation(BEGeoPoint value) {
        put("location", value);
    }

    public static BEQuery<ATMAround> getQuery() {
        return BEQuery.getQuery(ATMAround.class);
    }
}
