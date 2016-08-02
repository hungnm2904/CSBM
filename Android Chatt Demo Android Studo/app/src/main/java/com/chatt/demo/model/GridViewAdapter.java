package com.chatt.demo.model;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.chatt.demo.R;

import java.util.ArrayList;

/**
 * Created by akela on 22/07/2016.
 */
public class GridViewAdapter  extends ArrayAdapter<Photos> {

    private Context context;
    private int layoutResourceId;

    public ArrayList<Photos> getData() {
        return data;
    }

    public void setData(ArrayList<Photos> data) {
        this.data = data;
    }

    private ArrayList<Photos> data = new ArrayList<Photos>();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList<Photos> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.item_icon);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        Photos item = data.get(position);
        holder.image.setImageBitmap(item.getIconID());
        return row;

    }

    static class ViewHolder {
        ImageView image;
    }
}
