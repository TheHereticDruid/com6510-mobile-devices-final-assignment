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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
            System.out.println("Thumbnail already cached for file " + photo.getImPath());
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
            Photo photo = photos.get(holderAndPosition.position);

            File photoFile = new File(photo.getImPath());
            /* load file into memory */
            byte[] photoBytes = Util.fileToByteArray(photoFile.getAbsolutePath());
            ByteArrayInputStream photoStream = new ByteArrayInputStream(photoBytes);
            /* load thumbnail from disk if exists */
            Bitmap thumbnailBitmap = BitmapFactory.decodeFile(photo.getImThumbPath());

            /* update photo metadata in room db if:
               1. it has never been done (ts=0)
               2. file has been modified since it was last indexed
               3. thumbnail file does not exist
             */
            if (photo.getImTimestamp() == 0 ||
                    photoFile.lastModified() > photo.getImTimestamp() ||
                    thumbnailBitmap == null) {
                System.out.println("Need to index file: " + photo.getImPath());
                indexPhotoMetadata(photoFile, photoStream, photo);
                photoStream.reset();
                thumbnailBitmap = makeThumbnail(photo, photoStream);
            }

            /* cache thumbnail inside adapter to avoid reading from disk again */
            photo.setImThumbnail(thumbnailBitmap);
            return thumbnailBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holderAndPosition.holder.photoItemView.setImageBitmap(bitmap);
        }

        void indexPhotoMetadata(File photoFile, ByteArrayInputStream photoStream, Photo photo) {
            /* update timestamp */
            photo.setImTimestamp(photoFile.lastModified());
            /* update EXIF metadata in db */
            try {
                ExifInterface exifInterface = new ExifInterface(photoStream);
                // Coordinates
                float[] latlng = new float[2];
                if (exifInterface.getLatLong(latlng)) {
                    photo.setImLat(latlng[0]);
                    photo.setImLng(latlng[1]);
                }
                // Title
                String val;
                if ((val = exifInterface.getAttribute("Title")) != null) {
                    photo.setImTitle(val);
                }
                else {
                    photo.setImTitle(photoFile.getName());
                }
                // Description
                if ((val = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)) != null) {
                    photo.setImDescription(val);
                }
                else {
                    photo.setImDescription("");
                }
                // DateTime
                if ((val = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)) != null) {
                    photo.setImDateTime(val);
                }
                else {
                    photo.setImDateTime("");
                }
                /*
                // Artist
                if ((val = exifInterface.getAttribute(ExifInterface.TAG_ARTIST)) != null) {
                    photo.setImArtist(val);
                }
                // Make
                if ((val = exifInterface.getAttribute(ExifInterface.TAG_MAKE)) != null) {
                    photo.setImMake(val);
                }
                // Model
                if ((val = exifInterface.getAttribute(ExifInterface.TAG_MODEL)) != null) {
                    photo.setImModel(val);
                }
                */
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* update photo in db */
            viewModel.insertPhoto(photo);
        }

        Bitmap makeThumbnail(Photo photo, ByteArrayInputStream photoStream) {
            /* generate 100x100 thumbnail */
            System.out.println("Building thumbnail for file " + photo.getImPath());
            Bitmap originalBitmap = BitmapFactory.decodeStream(photoStream);
            Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true);
            try (FileOutputStream out = new FileOutputStream(photo.getImThumbPath())) {
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            } catch (IOException e) {
                System.err.println("Error writing thumbail to file: " + photo.getImThumbPath());
                e.printStackTrace();
            }
            return thumbnailBitmap;
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