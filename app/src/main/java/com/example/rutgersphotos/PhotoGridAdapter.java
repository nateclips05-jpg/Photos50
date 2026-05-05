package com.example.rutgersphotos;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

public final class PhotoGridAdapter extends BaseAdapter {
    private final Context context;
    private final List<PhotoItem> photos;

    public PhotoGridAdapter(Context context, List<PhotoItem> photos) {
        this.context = context;
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view;
        if (convertView == null) {
            view = (ImageView) LayoutInflater.from(context).inflate(R.layout.photo_grid_item, parent, false);
        } else {
            view = (ImageView) convertView;
        }
        view.setImageURI(Uri.parse(photos.get(position).getUri()));
        return view;
    }
}
