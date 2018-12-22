package com6510.dcs.shef.ac.uk;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"im_path"}, unique = true)})
public class ImageElement {
    @PrimaryKey
    private int imId;

    @ColumnInfo(name = "im_path")
    private String imPath;

    @ColumnInfo(name = "im_th_path")
    private String imThPath;

    @ColumnInfo(name = "im_title")
    private String imTitle;

    @ColumnInfo(name = "im_gps")
    private String imGps;

    @ColumnInfo(name = "im_timestamp")
    private int imTimestamp;

    public ImageElement(String imPath) {
        this.imPath = imPath;
    }

    public int getImId() {
        return imId;
    }

    public void setImId(int imId) {
        this.imId = imId;
    }

    public String getImPath() {
        return imPath;
    }

    public void setImPath(String imPath) {
        this.imPath = imPath;
    }

    public String getImThPath() {
        return imThPath;
    }

    public void setImThPath(String imThPath) {
        this.imThPath = imThPath;
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

