package com.chatt.demo.model;

import android.graphics.Bitmap;

import com.csbm.BEUser;

/**
 * Created by akela on 04/08/2016.
 */
public class ListFriendSearch {


    private Bitmap bmp;
    private BEUser buddyUser;
    private double distanceKml;
    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public ListFriendSearch(Bitmap bmp, BEUser buddyUser, double distanceKml) {
        this.bmp = bmp;
        this.buddyUser = buddyUser;
        this.distanceKml = distanceKml;
    }

    public double getDistanceKml() {
        return distanceKml;
    }

    public void setDistanceKml(double distanceKml) {
        this.distanceKml = distanceKml;
    }


    public BEUser getBuddyUser() {
        return buddyUser;
    }

    public void setBuddyUser(BEUser buddyUser) {
        this.buddyUser = buddyUser;
    }
}
