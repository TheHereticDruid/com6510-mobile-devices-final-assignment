package com6510.oak.shef.ac.uk;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Photo Data Access Object, to manage database functionality. Some specific queries are marked as LiveData as well.
 * The last one is for filtering, which comes with the added wildcard chars before and after each element in the filter.
 */
@Dao
public interface PhotoDao {
    @Query("SELECT * FROM Photo WHERE im_path LIKE :path")
    LiveData<Photo> getPhoto(String path);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertPhoto(Photo photo);

    @Query("DELETE FROM Photo WHERE im_path LIKE :path")
    void deletePhoto(String path);

    @Query("DELETE FROM Photo WHERE im_path IN (:paths)")
    void deletePhotos(List<String> paths);

    @Query("SELECT * FROM Photo ORDER BY im_timestamp DESC")
    LiveData<List<Photo>> getAllPhotos();

    @Query("SELECT * FROM Photo ORDER BY im_timestamp DESC")
    List<Photo> getAllPhotosSync();

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertAllPhotos(List<Photo> photos);

    @Query("DELETE FROM Photo")
    void deleteAllPhotos();

    @Query("SELECT * FROM Photo " +
            "WHERE im_title LIKE :title " +
            "AND im_description like :description " +
            "AND im_datetime LIKE :date " +
            "AND im_artist LIKE :artist " +
            "AND im_make LIKE :make " +
            "AND im_model LIKE :model")
    LiveData<List<Photo>> getFilteredPhotos(String title, String description, String date, String artist, String make, String model);
}
