package com6510.oak.shef.ac.uk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Utility methods
 */
public class Util {
    /**
     * Return a list of declared permissions
     * @param context Context
     * @return List of declared and granted permissions
     */
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

    /**
     * Initialize the EasyImage package
     * @param context Context
     */
    public static void initEasyImage(Context context) {
        EasyImage.configuration(context)
                .setImagesFolderName("TinyGallery")
                .setCopyTakenPhotosToPublicGalleryAppFolder(false)
                .setCopyPickedImagesToPublicGalleryAppFolder(false)
                .setAllowMultiplePickInGallery(true);
    }

    /**
     * Class to perform differentials
     */
    public static class MyDiffCallback extends DiffUtil.Callback {
        List<Photo> oldList;
        List<Photo> newList;

        /**
         * Constructor
         * @param oldList Old List
         * @param newList New List
         */
        public MyDiffCallback(List<Photo> oldList, List<Photo> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        /**
         * Get size of old list
         * @return Size of old List
         */
        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        /**
         * Get size of new list
         * @return Size of new List
         */
        @Override
        public int getNewListSize() {
            return newList.size();
        }

        /**
         * Check if two items are the same based on position
         * @param oldItemPosition Position of the old item
         * @param newItemPosition Position of the new item
         * @return True if same
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String old_path = oldList.get(oldItemPosition).getImPath();
            String new_path = newList.get(newItemPosition).getImPath();
            boolean res = old_path.equals(new_path);
            return res;
        }

        /**
         * Check if two items have the same contents
         * @param oldItemPosition Position of the old item
         * @param newItemPosition Position of the new item
         * @return True if same
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            long old_ts = oldList.get(oldItemPosition).getImTimestamp();
            long new_ts = newList.get(newItemPosition).getImTimestamp();
            return old_ts == new_ts;
        }

        /**
         * Get the Change Payload, if the items are the same, but the contents are not.
         * @param oldItemPosition Position of the old item
         * @param newItemPosition Position of the new item
         * @return Change Payload, if it exists, else Null
         */
        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

    /**
     * Generate a new Thumbnail path
     * @param context Context
     * @return New thumbnail path
     */
    public static String getNewThumbnailPath(Context context) {
        File thumbnailDir = new File(context.getCacheDir(), "thumbnails");
        File thumbnailFile = new File(thumbnailDir, UUID.randomUUID().toString());
        return thumbnailFile.getAbsolutePath();
    }

    /**
     * Check if the camera sensor exists
     * @param context Context
     * @return True if camera exists
     */
    /* https://developer.android.com/guide/topics/media/camera#java */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Read Metadata of the photo and retrieve it from the file itself
     * @param photo Photo in question
     */
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
            } else {
                photo.setImTitle(file.getName());
            }
            // Description
            if ((val = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)) != null) {
                photo.setImDescription(val);
            } else {
                photo.setImDescription("Add description!");
            }
            // DateTime
            if ((val = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)) != null) {
                photo.setImDateTime(val);
            } else {
                photo.setImDateTime("Add date taken!");
            }
            // Artist
            if ((val = exifInterface.getAttribute(ExifInterface.TAG_ARTIST)) != null) {
                photo.setImArtist(val);
            } else {
                photo.setImArtist("Add artist!");
            }
            // Make
            if ((val = exifInterface.getAttribute(ExifInterface.TAG_MAKE)) != null) {
                photo.setImMake(val);
            } else {
                photo.setImMake("Add camera manufacturer!");
            }
            // Model
            if ((val = exifInterface.getAttribute(ExifInterface.TAG_MODEL)) != null) {
                photo.setImModel(val);
            } else {
                photo.setImModel("Add camera model!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write Metadata to a Photo to the file itself
     * @param photo Photo in question
     */
    public static void writePhotoMetadata(Photo photo) {
        File file = new File(photo.getImPath());
        /* update timestamp */
        long newTimestamp = new Date().getTime();
        photo.setImTimestamp(newTimestamp);
        file.setLastModified(newTimestamp);
        /* update EXIF metadata in file */
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            // Coordinates
            if (photo.getImHasCoordinates()) {
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Float.toString(photo.getImLat()));
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, Float.toString(photo.getImLng()));
            }
            // Title
            exifInterface.setAttribute("Title", photo.getImTitle());
            // Description
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, photo.getImDescription());
            // DateTime
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, photo.getImDateTime());
            // Artist
            exifInterface.setAttribute(ExifInterface.TAG_ARTIST, photo.getImArtist());
            // Make
            exifInterface.setAttribute(ExifInterface.TAG_MAKE, photo.getImMake());
            // Model
            exifInterface.setAttribute(ExifInterface.TAG_MODEL, photo.getImModel());
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make a Thumbnail 100x100 in dimensions
     * @param photoPath Path of the photo
     * @param thumbPath Path of the thumbnail
     * @return Image
     */
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

    /**
     * Copy a file into another
     * @param sourceFile Source File
     * @param destFile Destination File
     * @throws IOException
     */
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

    /**
     * Check if the device this is running on is an emulator
     * @return True if it is
     */
    /* https://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in-the-emulator */
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    /**
     * Get a trimmed string
     * @param s String
     * @param limit Length of String requied
     * @return Trimmed String
     */
    public static String getPrettyTrimmedString(String s, int limit) {
        if (s.trim().length() > limit-3) {
            s = s.trim().substring(0, limit-3) + "...";
        }
        return s;
    }
}