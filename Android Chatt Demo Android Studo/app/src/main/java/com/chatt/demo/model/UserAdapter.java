package com.chatt.demo.model;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatt.demo.R;

import java.util.ArrayList;

/**
 * Created by akela on 16/07/2016.
 */
public class UserAdapter extends BaseAdapter  {

    private ArrayList<FriendUser> uList;
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
    public FriendUser getItem(int arg0) {
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
        ViewHolder holder ;
        View row = v;
        if (v == null){
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            row = inflater.inflate(R.layout.chat_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.iconBuddy);
            holder.buddyName = (TextView) row.findViewById(R.id.buddyName);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        FriendUser buddyItem = getItem(pos);
        holder.image.setImageBitmap(buddyItem.getIconFriend());
//        holder.image.setImageBitmap(getBmp());
        holder.buddyName.setText(buddyItem.getFriend().getUsername());
//        getBuddyIcon(buddyItem);

        if (buddyItem.getFriend().getBoolean("online")){
            holder.buddyName.setTextColor(Color.parseColor("#00b863"));
        } else {
            holder.buddyName.setTextColor(Color.parseColor("#7d7d7d"));
        }
        return row;

    }

    static class ViewHolder {
        ImageView image;
        TextView buddyName;
    }



    public UserAdapter() {
        uList = new ArrayList<>();
    }

    public void setUList(ArrayList<FriendUser> uList){
        this.uList = uList;
    }

}