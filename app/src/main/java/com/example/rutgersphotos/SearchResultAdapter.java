package com.example.rutgersphotos;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public final class SearchResultAdapter extends BaseAdapter {
    private final Context context;
    private final List<SearchResult> results;

    public SearchResultAdapter(Context context, List<SearchResult> results) {
        this.context = context;
        this.results = results;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout;
        ImageView image;
        TextView label;
        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            image = new ImageView(context);
            image.setId(View.generateViewId());
            image.setLayoutParams(new LinearLayout.LayoutParams(
                    DialogHelper.dp(context, 108),
                    DialogHelper.dp(context, 108)
            ));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            label = new TextView(context);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            layout.addView(image);
            layout.addView(label);
        } else {
            layout = (LinearLayout) convertView;
            image = (ImageView) layout.getChildAt(0);
            label = (TextView) layout.getChildAt(1);
        }
        SearchResult result = results.get(position);
        image.setImageURI(Uri.parse(result.getPhoto().getUri()));
        label.setText(result.getAlbumName());
        return layout;
    }
}
