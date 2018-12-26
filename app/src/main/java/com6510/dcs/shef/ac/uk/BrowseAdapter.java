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

import java.io.File;
import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.View_Holder> {
    private Context context;
    private List<Photo> photos;

    public BrowseAdapter(Context context) {
        this.context = context;
    }

    public BrowseAdapter(Context context, List<Photo> photos) {
        super();
        this.photos = photos;
        this.context = context;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_item,
                parent, false);
        context = parent.getContext();
        return new View_Holder(v);
    }

    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {
        /* Use the provided View Holder on the onCreateViewHolder method to populate the
           current row on the RecyclerView */
        Photo photo = photos.get(position);
        if (holder != null && photo != null) {
            if (photo.getImThumbnail() != null) {
                /* thumbnail already cached */
                holder.photoView.setImageBitmap(photo.getImThumbnail());
            } else {
                /* load photo from disk asynchronously */
                new LoadSinglePhotoTask().execute(new HolderAndPosition(position, holder));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowPhotoActivity.class);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            });
        }
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

    public class View_Holder extends RecyclerView.ViewHolder  {
        ImageView photoView;

        View_Holder(View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.photo_item);
        }
    }

    private class LoadSinglePhotoTask extends AsyncTask<HolderAndPosition, Void, Bitmap> {
        HolderAndPosition holderAndPosition;

        @Override
        protected Bitmap doInBackground(HolderAndPosition... holderAndPositions) {
            holderAndPosition = holderAndPositions[0];
            Photo photo = photos.get(holderAndPosition.position);
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getImPath()); /* read photo from disk */
            Bitmap small_bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
            photo.setImThumbnail(small_bitmap);
            return small_bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holderAndPosition.holder.photoView.setImageBitmap(bitmap); /* set photo to grid element */
        }
    }

    private class HolderAndPosition {
        int position;
        BrowseAdapter.View_Holder holder;

        public HolderAndPosition(int position, BrowseAdapter.View_Holder holder) {
            this.position = position;
            this.holder = holder;
        }
    }
}