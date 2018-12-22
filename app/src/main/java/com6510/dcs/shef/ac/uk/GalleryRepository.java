package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.persistence.room.Room;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryRepository extends ViewModel {
    private LiveData<List<ImageElement>> imageElements;
    private ImageDatabase db;
    private Application application;

    public GalleryRepository(Application application) {
        this.application = application;
        db = Room.databaseBuilder(application.getApplicationContext(),
                ImageDatabase.class,
                "image-database").build();
        ImageDao dbDao = db.imageDao();
        //imageElements = dbDao.getAllImages();
        imageElements = new MutableLiveData<>();

        /* Thread to periodically scan images on startup and sync to db
         * according to last modified timestamps. Currently just load all
         * images in this thread.
         */
    }

    public LiveData<List<ImageElement>> getImageElements() {
        return imageElements;
    }

    public void scan() {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = MediaStore.Images.Media.query(application.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection);
        System.out.println("Found " + cursor.getCount() + " images.");
        List<ImageElement> foundImages = new ArrayList<ImageElement>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex("_data"));
            foundImages.add(new ImageElement(path));
            System.out.println(path);
            cursor.moveToNext();
        }
        /* add all to db */
        System.out.println("Adding images to db.");
        ImageDao dbDao = db.imageDao();
        dbDao.insertAll(foundImages);
    }
}