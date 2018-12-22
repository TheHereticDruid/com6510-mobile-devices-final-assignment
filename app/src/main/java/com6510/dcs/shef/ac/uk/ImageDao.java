package com6510.dcs.shef.ac.uk;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ImageDao {
    @Query("SELECT * FROM ImageElement")
    LiveData<List<ImageElement>> getAllImages();

    @Insert
    void insertAll(List<ImageElement> images);
}
