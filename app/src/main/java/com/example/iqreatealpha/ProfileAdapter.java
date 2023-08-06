package com.example.iqreatealpha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileAdapter extends BaseAdapter {
    private List<Model> images;
    private Context context;

    public ProfileAdapter(Context context) {
        this.context = context;
    }

    public void setImages(List<Model> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return images != null ? images.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return images != null ? images.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_profile_images, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.profile_image_item);
        Model image = images.get(position);
        // Load the image into the ImageView using your preferred image loading library (Picasso in this case)
        Picasso.get().load(image.getImageUrl()).into(imageView);

        return view;
    }
}
