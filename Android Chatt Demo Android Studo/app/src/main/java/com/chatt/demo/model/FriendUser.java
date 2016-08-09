package com.chatt.demo.model;

import android.graphics.Bitmap;

import com.csbm.BEUser;

/**
 * Created by akela on 06/08/2016.
 */
public class FriendUser {
    private Bitmap iconFriend;
    private BEUser friend;

    public FriendUser(Bitmap iconFriend, BEUser friend) {
        this.iconFriend = iconFriend;
        this.friend = friend;
    }

    public Bitmap getIconFriend() {
        return iconFriend;
    }

    public void setIconFriend(Bitmap iconFriend) {
        this.iconFriend = iconFriend;
    }

    public BEUser getFriend() {
        return friend;
    }

    public void setFriend(BEUser friend) {
        this.friend = friend;
    }


}
