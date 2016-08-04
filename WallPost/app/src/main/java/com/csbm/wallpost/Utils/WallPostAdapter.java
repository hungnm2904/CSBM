package com.csbm.wallpost.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.csbm.wallpost.R;

import java.util.ArrayList;

/**
 * Created by akela on 05/08/2016.
 */
public class WallPostAdapter extends BaseAdapter {
    private ArrayList<Images> listImg;

    public ArrayList<Images> getListImg() {
        return listImg;
    }

    public void setListImg(ArrayList<Images> listImg) {
        this.listImg = listImg;
    }

    public WallPostAdapter(){
        listImg = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return listImg.size();
    }

    @Override
    public Images getItem(int position) {
        return listImg.get(position);
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
            row = inflater.inflate(R.layout.wall_post_item_layout, null);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.imgItem);
            holder.comment = (TextView) row.findViewById(R.id.textComment);
            holder.created = (TextView) row.findViewById(R.id.textCreated);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Images item = getItem(position);
        holder.image.setImageBitmap(item.getImg());
        holder.comment.setText(item.getComment());
        holder.created.setText(String.valueOf(item.getDate()));

        return row;
    }
    static class ViewHolder {
        ImageView image;
        TextView comment;
        TextView created;
    }
}
