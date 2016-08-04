package com.csbm.wallpost.Utils;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by akela on 05/08/2016.
 */
public class Images {
    private Bitmap img;
    private String comment;
    private Date date;

    public Images(Bitmap img, String comment, Date date) {
        this.img = img;
        this.comment = comment;
        this.date = date;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
