package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

/**
 * Class to handle managing the data bound to the MapsActivity.
 */
public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MapsViewHolder> {
    private List<Photo> mDataset;
    WeakReference<Context> mContextWeakReference;

    /**
     * Class to assign an Image View to the ViewHolder
     */
    public static class MapsViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public MapsViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }

    /**
     * Constructor
     * @param context Context
     */
    public MapsAdapter(Context context) {
        this.mContextWeakReference = new WeakReference<>(context);
    }

    /**
     * Reset the current map data
     * @param newData List of photos to be included in the map
     */
    public void resetDataset(List<Photo> newData) {
        mDataset = new LinkedList<Photo>(newData);
        /* remove photos without GPS data */
        for (Photo photo : newData) {
            if (photo.getImHasCoordinates() == false) {
                mDataset.remove(photo);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * On creating the ViewHolder, add image thumbnails
     * @param parent Parent View
     * @param viewType View Type
     * @return The MapsViewHolder
     */
    @Override
    public MapsAdapter.MapsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ImageView view = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_view, parent, false);
        return new MapsViewHolder(view);
    }

    /**
     * When the viewholder is bound, the following is run.
     * @param holder View Holder in question
     * @param position Photo identification
     */
    @Override
    public void onBindViewHolder(MapsViewHolder holder, int position) {
        final Context context = mContextWeakReference.get();
        final Photo photo = mDataset.get(position);
        holder.mImageView.setImageBitmap(BitmapFactory.decodeFile(photo.getImThumbPath()));
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MapsActivity) context).thumbnailClick(photo);
            }
        });
    }

    /**
     * Get item count.
     * @ Item count
     */
    @Override
    public int getItemCount() {
        if (mDataset == null) {
            return 0;
        }
        return mDataset.size();
    }
}
