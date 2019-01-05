package com6510.dcs.shef.ac.uk;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
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

    @ColumnInfo(name = "im_timestamp")
    private long imTimestamp;

    @ColumnInfo(name = "im_title")
    private String imTitle;

    @ColumnInfo(name = "im_description")
    private String imDescription;

    @ColumnInfo(name = "im_lat")
    float imLat;

    @ColumnInfo(name = "im_lng")
    float imLng;

    @ColumnInfo(name = "im_datetime")
    private String imDateTime;

    @Ignore
    private Bitmap imThumbnail;

    /* ------------ constructors -------------- */
    public Photo(String imPath, String imThumbPath) {
        this.imPath = imPath;
        this.imThumbPath = imThumbPath;
    }

    public Photo(Parcel in) {
        this.imPath = in.readString();
        this.imThumbPath = in.readString();
        this.imTimestamp = in.readLong();
        this.imTitle = in.readString();
        this.imLat = in.readFloat();
        this.imLng = in.readFloat();
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

    public String getImDateTime() {
        return imDateTime;
    }

    public void setImDateTime(String imDateTime) {
        this.imDateTime = imDateTime;
    }

    public String getImDescription() {
        return imDescription;
    }

    public void setImDescription(String imDescription) {
        this.imDescription = imDescription;
    }

    public float getImLat() {
        return imLat;
    }

    public void setImLat(float imLat) {
        this.imLat = imLat;
    }

    public float getImLng() {
        return imLng;
    }

    public void setImLng(float imLng) {
        this.imLng = imLng;
    }

    /* --------------------- parcels -------------------- */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imPath);
        dest.writeString(this.imThumbPath);
        dest.writeLong(this.imTimestamp);
        dest.writeString(this.imTitle);
        dest.writeFloat(this.imLat);
        dest.writeFloat(this.imLng);
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
