package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.ImageViewHolder> {

    class ImageViewHolder extends RecyclerView.ViewHolder  {
        private final ImageView photoItemView;

        private ImageViewHolder(View itemView) {
            super(itemView);
            photoItemView = itemView.findViewById(R.id.photo_item);
        }
    }

    private LayoutInflater mInflator;
    private Context context;
    private List<Photo> photos;

    public BrowseAdapter(Context context) {
        this.context = context;
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = mInflator.inflate(R.layout.browse_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        /* Use the provided View Holder on the onCreateViewHolder method to populate the
           current row on the RecyclerView */
        Photo photo = photos.get(position);
        if (photo.getImThumbnail() != null) {
            /* thumbnail already cached */
            holder.photoItemView.setImageBitmap(photo.getImThumbnail());
        } else {
            /* load photo from disk asynchronously */
            //new LoadSinglePhotoTask().execute(new HolderAndPosition(position, holder));
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getImThumbPath()); /* read photo from disk */
            photo.setImThumbnail(bitmap);
            holder.photoItemView.setImageBitmap(bitmap); /* set photo to grid element */
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowPhotoActivity.class);
                intent.putExtra("photo", photos.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (photos == null) {
            return 0;
        }
        return photos.size();
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    private class LoadSinglePhotoTask extends AsyncTask<HolderAndPosition, Void, Bitmap> {
        HolderAndPosition holderAndPosition;

        @Override
        protected Bitmap doInBackground(HolderAndPosition... holderAndPositions) {
            holderAndPosition = holderAndPositions[0];
            Photo photo = photos.get(holderAndPosition.position);
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getImThumbPath()); /* read photo from disk */
            photo.setImThumbnail(bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holderAndPosition.holder.photoItemView.setImageBitmap(bitmap); /* set photo to grid element */
        }
    }

    private class HolderAndPosition {
        int position;
        BrowseAdapter.ImageViewHolder holder;

        public HolderAndPosition(int position, BrowseAdapter.ImageViewHolder holder) {
            this.position = position;
            this.holder = holder;
        }
    }
}