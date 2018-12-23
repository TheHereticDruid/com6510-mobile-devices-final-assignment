package com6510.dcs.shef.ac.uk;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {
    @Query("SELECT * FROM Photo WHERE im_path LIKE :path")
    LiveData<Photo> getPhoto(String path);

    @Insert
    void insert(Photo photo);

    @Update
    void update(Photo photo);

    @Query("SELECT * FROM Photo ORDER BY im_timestamp DESC")
    LiveData<List<Photo>> getAllPhotos();

    @Insert
    void insertAllPhotos(List<Photo> photos);

    @Update
    void updateAllPhotos(List<Photo> photos);
}
