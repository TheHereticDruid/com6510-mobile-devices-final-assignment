package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.aprilapps.easyphotopicker.EasyImage;

public class Util {

    public static Set<String> getDeclaredPermissions(Context context) {
        try {
            String[] grantedPermissions = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions;
            return new HashSet<String>(Arrays.asList(grantedPermissions));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new HashSet<String>();
    }

    public static void initEasyImage(Context context) {
        EasyImage.configuration(context)
                .setImagesFolderName("TinyGallery")
                .setCopyTakenPhotosToPublicGalleryAppFolder(false)
                .setCopyPickedImagesToPublicGalleryAppFolder(false)
                .setAllowMultiplePickInGallery(true);
    }

    public static class MyDiffCallback extends DiffUtil.Callback {
        List<Photo> oldList;
        List<Photo> newList;

        public MyDiffCallback(List<Photo> oldList, List<Photo> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String old_path = oldList.get(oldItemPosition).getImPath();
            String new_path = newList.get(newItemPosition).getImPath();
            boolean res = old_path.equals(new_path);
            return res;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            long old_ts = oldList.get(oldItemPosition).getImTimestamp();
            long new_ts = newList.get(newItemPosition).getImTimestamp();
            return old_ts == new_ts;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

    public static String getNewThumbnailPath(Context context) {
        File thumbnailDir = new File(context.getCacheDir(), "thumbnails");
        File thumbnailFile = new File(thumbnailDir, UUID.randomUUID().toString());
        return thumbnailFile.getAbsolutePath();
    }

    public static byte[] fileToByteArray(String path) {
        File file = new File(path);
        byte[] bytes = new byte[(int)file.length()];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            System.err.println("Could not read photo in memory: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return bytes;
    }

    /* Check if this device has a camera https://developer.android.com/guide/topics/media/camera#java */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /* reads photo file and fills in metadata into passed object */
    public static void readPhotoMetadata(Photo photo) {
        File file = new File(photo.getImPath());
        /* update timestamp */
        photo.setImTimestamp(file.lastModified());
        /* update EXIF metadata in object */
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            // Coordinates
            float[] latlng = new float[2];
            if (exifInterface.getLatLong(latlng)) {
                photo.setImLat(latlng[0]);
                photo.setImLng(latlng[1]);
                photo.setImHasCoordinates(true);
            } else {
                photo.setImHasCoordinates(false);
            }
            // Title
            String val;
            if ((val = exifInterface.getAttribute("Title")) != null) {
                photo.setImTitle(val);
            }
            else {
                photo.setImTitle(file.getName());
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
    }

    public static Bitmap makeThumbnail(String photoPath, String thumbPath) {
        /* generate 100x100 thumbnail */
        System.out.println("Building thumbnail for file " + photoPath);
        Bitmap originalBitmap = BitmapFactory.decodeFile(photoPath);
        if (originalBitmap == null) {
            System.out.println("Error loading file " + photoPath + ", cannot build thumbnail");
            return null;
        }
        Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true);
        try (FileOutputStream out = new FileOutputStream(thumbPath)) {
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (IOException e) {
            System.err.println("Error writing thumbail to file: " + thumbPath);
            e.printStackTrace();
        }
        return thumbnailBitmap;
    }

    /* https://stackoverflow.com/questions/29867121/how-to-copy-programmatically-a-file-to-another-directory */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
