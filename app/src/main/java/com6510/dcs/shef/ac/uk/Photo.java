package com6510.dcs.shef.ac.uk;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(indices = {@Index(value = {"im_path"}, unique = true)})
public class Photo {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "im_path")
    private String imPath;

    @ColumnInfo(name = "im_thumb_path")
    private String imThumbPath;

    @ColumnInfo(name = "im_title")
    private String imTitle;

    @ColumnInfo(name = "im_gps")
    private String imGps;

    @ColumnInfo(name = "im_timestamp")
    private int imTimestamp;

    public Photo(String imPath) {
        this.imPath = imPath;
    }

    public String getImPath() {
        return imPath;
    }

    public void setImPath(String imPath) {
        this.imPath = imPath;
    }

    public String getImThumbPath() {
        return imThumbPath;
    }

    public void setImThumbPath(String imThumbPath) {
        this.imThumbPath = imThumbPath;
    }

    public String getImTitle() {
        return imTitle;
    }

    public void setImTitle(String imTitle) {
        this.imTitle = imTitle;
    }

    public String getImGps() {
        return imGps;
    }

    public void setImGps(String imGps) {
        this.imGps = imGps;
    }

    public int getImTimestamp() {
        return imTimestamp;
    }

    public void setImTimestamp(int imTimestamp) {
        this.imTimestamp = imTimestamp;
    }
}

