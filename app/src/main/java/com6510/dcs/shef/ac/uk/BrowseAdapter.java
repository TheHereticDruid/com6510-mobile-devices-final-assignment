package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

// merge test
public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.ImageViewHolder> {

    class ImageViewHolder extends RecyclerView.ViewHolder  {
        private final ImageView photoItemView;

        private ImageViewHolder(View itemView) {
            super(itemView);
            photoItemView = itemView.findViewById(R.id.photo_item);
        }
    }

    private LayoutInflater mInflator;
    private List<Photo> photos = new ArrayList<>();

    private Context context;
    private GalleryViewModel viewModel;

    Bitmap emptyThumbnail;

    public BrowseAdapter(Context context, GalleryViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        mInflator = LayoutInflater.from(context);
        emptyThumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.blank_square);
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
            /* thumbnail already cached inside adapter */
            holder.photoItemView.setImageBitmap(photo.getImThumbnail());
            //System.out.println("Thumbnail already cached for file " + photo.getImPath());
        } else {
            /* temporarily set empty thumbnail */
            holder.photoItemView.setImageBitmap(emptyThumbnail);
            /* load photo from disk asynchronously, cache inside adapter */
            new LoadSinglePhotoTask().execute(new HolderAndPosition(position, holder));
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

    public void setPhotosDiff(List<Photo> photos) {
        Util.MyDiffCallback diffCallback = new Util.MyDiffCallback(this.photos, photos);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffCallback);
        this.photos.clear();
        this.photos.addAll(photos);
        result.dispatchUpdatesTo(this);
    }

    private class LoadSinglePhotoTask extends AsyncTask<HolderAndPosition, Void, Bitmap> {
        HolderAndPosition holderAndPosition;

        @Override
        protected Bitmap doInBackground(HolderAndPosition... holderAndPositions) {
            holderAndPosition = holderAndPositions[0];
            Photo photo;
            try {
                photo = photos.get(holderAndPosition.position);
            } catch (IndexOutOfBoundsException e) {
                /* photo got deleted in onChanged callback */
                System.out.println("Error: photo does not exist anymore, not loading");
                return null;
            }

            File photoFile = new File(photo.getImPath());
            /* load thumbnail from disk if exists */
            Bitmap thumbnailBitmap = BitmapFactory.decodeFile(photo.getImThumbPath());

            /* update photo metadata in room db if:
               1. file has been modified since it was last indexed (has newer timestamp)
               2. thumbnail file does not exist
             */
            if (photoFile.lastModified() > photo.getImTimestamp() || thumbnailBitmap == null) {
                System.out.println("Need to index file: " + photo.getImPath());
                Util.readPhotoMetadata(photo);
                thumbnailBitmap = Util.makeThumbnail(photo.getImPath(), photo.getImThumbPath());
                /* update photo in db */
                viewModel.insertPhoto(photo);
            }

            /* cache thumbnail inside adapter to avoid reading from disk again */
            if (thumbnailBitmap == null) {
                photo.setImThumbnail(emptyThumbnail);
            } else {
                photo.setImThumbnail(thumbnailBitmap);
            }
            return thumbnailBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                holderAndPosition.holder.photoItemView.setImageBitmap(bitmap);
            }
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