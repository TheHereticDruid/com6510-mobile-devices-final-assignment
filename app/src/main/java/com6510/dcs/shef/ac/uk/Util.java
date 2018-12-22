package com6510.dcs.shef.ac.uk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
}