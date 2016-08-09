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
 * Created by akela on 04/08/2016.
 */
public class ListFriendSearchAdapter extends BaseAdapter {

    private ArrayList<ListFriendSearch> listUser;
    @Override
    public int getCount() {
        return listUser.size();
    }

    @Override
    public ListFriendSearch getItem(int position) {
        return listUser.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View v, ViewGroup viewGroup) {

        ViewHolder holder ;
        View row = v;
        if (v == null){
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            row = inflater.inflate(R.layout.list_user_searched, null);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.iconBuddy);
            holder.buddyName = (TextView) row.findViewById(R.id.buddyItemSearched);
            holder.distance = (TextView) row.findViewById(R.id.distanceKm);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
//
        ListFriendSearch buddyItem = getItem(pos);

        holder.buddyName.setText(buddyItem.getBuddyUser().getUsername());
        //check distance
        if (buddyItem.getDistanceKml() < 1){
            int distance = (int) (buddyItem.getDistanceKml()*1000);
            holder.distance.setText(String.valueOf(distance) + " m" );
        } else {
            String distance = String.format("%.2f", buddyItem.getDistanceKml());
            holder.distance.setText(distance + " km" );
        }

//        getBuddyIcon(buddyItem);
        holder.image.setImageBitmap(buddyItem.getBmp());
        if (buddyItem.getBuddyUser().getBoolean("online")){
            holder.buddyName.setTextColor(Color.parseColor("#00b863"));
        } else {
            holder.buddyName.setTextColor(Color.parseColor("#7d7d7d"));
        }
        return row;

    }
    static class ViewHolder {
        ImageView image;
        TextView buddyName;
        TextView distance;
    }
    public ListFriendSearchAdapter() {
        listUser = new ArrayList<>();
    }
    public ArrayList<ListFriendSearch> getListUser() {
        return listUser;
    }

    public void setListUser(ArrayList<ListFriendSearch> listUser) {
        this.listUser = listUser;
    }
}
