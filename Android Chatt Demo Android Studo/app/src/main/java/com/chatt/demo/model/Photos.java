package com.chatt.demo.model;

import android.graphics.Bitmap;

/**
 * Created by akela on 06/07/2016.
 */
public class Photos {

    private Bitmap iconID;

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setIconID(Bitmap iconID) {
        this.iconID = iconID;
    }

    private String photoId;


    public Photos( Bitmap iconID, String photoId) {
        super();

        this.iconID = iconID;
        this.photoId = photoId;
    }
    public Bitmap getIconID() {
        return iconID;
    }

}
