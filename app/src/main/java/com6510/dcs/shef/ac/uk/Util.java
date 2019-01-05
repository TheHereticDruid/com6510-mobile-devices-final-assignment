package com6510.dcs.shef.ac.uk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.aprilapps.easyphotopicker.EasyImage;

public class Util {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1357;
    private static final int REQUEST_INTERNET = 5731;
    private static final int REQUEST_ACCESS_NETWORK_STATE = 3715;

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
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(false)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
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
}
