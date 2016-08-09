package com.csbm.atmaroundme.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csbm.atmaroundme.ATMAround;
import com.csbm.atmaroundme.R;

import java.util.ArrayList;

/**
 * Created by akela on 09/08/2016.
 */
public class ATMAroundAdapter extends BaseAdapter {
    public void setAtmArounds(ArrayList<ATMAround> atmArounds) {
        this.atmArounds = atmArounds;
    }

    private ArrayList<ATMAround> atmArounds;

    public ArrayList<ATMAround> getAtmArounds() {
        return atmArounds;
    }



    public ATMAroundAdapter(){
        atmArounds = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return atmArounds.size();
    }

    @Override
    public ATMAround getItem(int position) {
        return atmArounds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup viewGroup) {


        ViewHolder holder ;
        View row = v;
        if (v == null){
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            row = inflater.inflate(R.layout.atmaround_item, null);
            holder = new ViewHolder();
            holder.atmName = (TextView) row.findViewById(R.id.atm_name);
            holder.atmAddress = (TextView) row.findViewById(R.id.atm_address);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
////
//        ListFriendSearch buddyItem = getItem(pos);
        ATMAround atmItem = getItem(position);
        holder.atmName.setText(atmItem.getBankName());
        holder.atmAddress.setText(atmItem.getAddress());
//

        return row;
//        return null;
    }
    static class ViewHolder {
        TextView atmName;
        TextView atmAddress;
    }
}
