package com.stefannew.bookreview;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Array Adapter for Reviews List
 * Takes each review item and returns a list item with the image,
 * review source name, and review snippet
 */
public class ReviewAdapter extends ArrayAdapter<Review> {

    Context context;
    int layoutResourceId;
    ArrayList<Review> data;


    public ReviewAdapter(Context context, int layoutResourceId, ArrayList<Review> data) {
        super(context, layoutResourceId, data);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);
        }

        Review review           = data.get(position);

        TextView source         = (TextView) v.findViewById(R.id.review_source);
        ImageView source_icon   = (ImageView) v.findViewById(R.id.review_icon);
        TextView snippet        = (TextView) v.findViewById(R.id.review_snippet);

        source.setText(review.source);
        new DownloadImageTask(source_icon).execute(review.image_source);
        snippet.setText(review.snippet);

        return v;
    }
}

