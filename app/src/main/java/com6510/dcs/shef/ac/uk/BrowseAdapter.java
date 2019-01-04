package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    private List<Photo> photos = new ArrayList<>();

    Bitmap emptyThumbnail;

    public BrowseAdapter(Context context) {
        this.context = context;
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
            System.out.println("Thumbnail already cached for file " + photo.getImPath());
        } else {
            /* temporarily set empty thumbnail */
            holder.photoItemView.setImageBitmap(emptyThumbnail);
            /* load photo from disk asynchronously, create thumbnail, cache inside adapter */
            new CreateThumbnailTask().execute(new HolderAndPosition(position, holder));
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

    private class CreateThumbnailTask extends AsyncTask<HolderAndPosition, Void, Bitmap> {
        HolderAndPosition holderAndPosition;

        @Override
        protected Bitmap doInBackground(HolderAndPosition... holderAndPositions) {
            holderAndPosition = holderAndPositions[0];
            Photo photo = photos.get(holderAndPosition.position);

            /* load thumbnail from disk if exists */
            Bitmap thumbnailBitmap = BitmapFactory.decodeFile(photo.getImThumbPath());

            /* load original photo and write its 100x100 thumbnail */
            if (thumbnailBitmap == null) {
                System.out.println("Building thumbnail for file " + photo.getImPath());
                Bitmap originalBitmap = BitmapFactory.decodeFile(photo.getImPath());
                thumbnailBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true);

                try (FileOutputStream out = new FileOutputStream(photo.getImThumbPath())) {
                    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                } catch (IOException e) {
                    System.err.println("Error writing thumbail to file: " + photo.getImThumbPath());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Thumbnail already built for file " + photo.getImPath());
            }

            /* cache thumbnail inside adapter to avoid reading from disk again */
            photo.setImThumbnail(thumbnailBitmap);
            return thumbnailBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holderAndPosition.holder.photoItemView.setImageBitmap(bitmap);
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