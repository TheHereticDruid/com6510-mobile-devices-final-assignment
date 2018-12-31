package com6510.dcs.shef.ac.uk;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(indices = {@Index(value = {"im_path"}, unique = true)})
public class Photo implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "im_path")
    private String imPath;

    @ColumnInfo(name = "im_thumb_path")
    private String imThumbPath;

    @ColumnInfo(name = "im_title")
    private String imTitle;

    @ColumnInfo(name = "im_lat")
    private int imLat;

    @ColumnInfo(name = "im_lng")
    private int imLng;

    @ColumnInfo(name = "im_timestamp")
    private long imTimestamp;

    @Ignore
    private Bitmap imThumbnail;

    /* ------------ constructors -------------- */
    public Photo(String imPath) {
        this.imPath = imPath;
    }

    public Photo(Parcel in) {
        this.imPath = in.readString();
        this.imThumbPath = in.readString();
        this.imTitle = in.readString();
        this.imLat = in.readInt();

        this.imLng = in.readInt();
        this.imTimestamp = in.readLong();
    }

    /* ------------ getters and setters ---------------*/
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

    public long getImTimestamp() {
        return imTimestamp;
    }

    public void setImTimestamp(long imTimestamp) {
        this.imTimestamp = imTimestamp;
    }

    public Bitmap getImThumbnail() {
        return imThumbnail;
    }

    public void setImThumbnail(Bitmap imThumbnail) {
        this.imThumbnail = imThumbnail;
    }

    public int getImLat() {
        return imLat;
    }

    public void setImLat(int imLat) {
        this.imLat = imLat;
    }

    public int getImLng() {
        return imLng;
    }

    public void setImLng(int imLng) {
        this.imLng = imLng;
    }


    /* parcels */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imPath);
        dest.writeString(this.imThumbPath);
        dest.writeString(this.imTitle);
        dest.writeInt(this.imLat);
        dest.writeInt(this.imLng);
        dest.writeLong(this.imTimestamp);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
