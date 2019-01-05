package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com6510.dcs.shef.ac.uk.gallery.R;

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MapsViewHolder> {
    private ArrayList<Photo> mDataset;
    WeakReference<Context> mContextWeakReference;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MapsViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public MapsViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MapsAdapter(ArrayList<Photo> myDataset, Context context) {
        mDataset = myDataset;
        this.mContextWeakReference = new WeakReference<>(context);
    }

    public void resetDataset(ArrayList<Photo> newData) {
        mDataset.clear();
        mDataset.addAll(newData);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MapsAdapter.MapsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ImageView view = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_view, parent, false);
        return new MapsViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MapsViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Context context = mContextWeakReference.get();
        final Photo photo=mDataset.get(position);
        holder.mImageView.setImageBitmap(BitmapFactory.decodeFile(photo.getImThumbPath()));
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MapsActivity) context).thumbnailClick(photo);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
