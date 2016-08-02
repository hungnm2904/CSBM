package com.chatt.demo.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chatt.demo.R;
import com.csbm.BEUser;

import java.util.ArrayList;

/**
 * Created by akela on 16/07/2016.
 */
public class UserAdapter extends BaseAdapter  {

    private ArrayList<BEUser> uList;
    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return uList.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public BEUser getItem(int arg0) {
        return uList.get(arg0);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int pos, View v, ViewGroup viewGroup) {
        if (v == null){
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            v = inflater.inflate(R.layout.chat_item, null);
        }
//            v = getLayoutInflater().inflate(R.layout.chat_item, null);

        BEUser c = getItem(pos);
        TextView lbl = (TextView) v;
        lbl.setText(c.getUsername());
        lbl.setCompoundDrawablesWithIntrinsicBounds(
                c.getBoolean("online") ? R.drawable.ic_online
                        : R.drawable.ic_offline, 0, R.drawable.arrow, 0);

        return v;
    }


    public UserAdapter() {
        uList = new ArrayList<>();
    }

    public void setUList(ArrayList<BEUser> uList){
        this.uList = uList;
    }

}