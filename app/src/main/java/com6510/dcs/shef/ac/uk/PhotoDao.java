package com6510.dcs.shef.ac.uk;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PhotoDao {
    @Query("SELECT * FROM Photo WHERE im_path LIKE :path")
    LiveData<Photo> getPhoto(String path);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertPhoto(Photo photo);

    @Query("DELETE FROM Photo WHERE im_path LIKE :path")
    void deletePhoto(String path);

    @Query("SELECT * FROM Photo ORDER BY im_timestamp DESC")
    LiveData<List<Photo>> getAllPhotos();

    @Query("SELECT * FROM Photo ORDER BY im_timestamp DESC")
    List<Photo> getAllPhotosSync();

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertAllPhotos(List<Photo> photos);

    @Query("DELETE FROM Photo")
    void deleteAllPhotos();

    @Query("SELECT * FROM Photo WHERE im_title LIKE :title AND im_description like :description AND im_datetime LIKE :date")
    LiveData<List<Photo>> getFilteredPhotos(String title, String description, String date);
}
