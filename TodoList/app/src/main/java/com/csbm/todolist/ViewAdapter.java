package com.csbm.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akela on 20/07/2016.
 */
public class ViewAdapter extends BaseAdapter {
    private List<String> listTitle;
    @Override
    public int getCount() {
        return listTitle.size();
    }

    @Override
    public Object getItem(int i) {
        return listTitle.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.list_view_item, viewGroup, false);
        }
        String item = listTitle.get(i);
        TextView title = (TextView) view.findViewById(R.id.txtValue);
        title.setText(item);
        return view;
    }

    public ViewAdapter() {
        listTitle = new ArrayList<>();
    }


    public void setListTitle(List<String> listTitle) {

        this.listTitle = listTitle;
    }
}
