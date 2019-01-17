package com6510.oak.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

/**
 * Gallery View Model, one layer above the Repo
 */
public class GalleryViewModel extends AndroidViewModel {
    private GalleryRepository repository;
    private LiveData<List<Photo>> photos;

    /**
     * Constructor. New Repo object is created
     * @param application Application
     */
    public GalleryViewModel(Application application) {
        super(application);
        /* create connection to repository */
        repository = new GalleryRepository(application);
        /* subscribe to live data */
        photos = repository.getAllPhotos();
    }

    /**
     * Getter for Repo
     * @return Repository Object
     */
    public GalleryRepository getRepository() {
        return repository;
    }

    /**
     * Get all photos, previously retrieved. This is a LiveData method
     * @return LiveData object observing all photos
     */
    LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    /**
     * Get filtered photos. This is also a LiveData method
     * @param title Title
     * @param description Description
     * @param date Date
     * @param artist Artist
     * @param make Make
     * @param model Model
     * @return LiveData object observing the filtered set of photos
     */
    LiveData<List<Photo>> getFilteredPhotos(String title, String description, String date, String artist, String make, String model) {
        return repository.getFilteredPhotos(title, description, date, artist, make, model);
    }

    /**
     * Get all photos in Synchronous mode
     * @return All photos
     */
    List<Photo> getAllPhotosSync() { return repository.getAllPhotosSync(); }

    /**
     * Insert a Photo
     * @param photo Photo in question
     */
    void insertPhoto(Photo photo) {
        repository.insertPhoto(photo);
    }

    /**
     * Delete Photo
     * @param path Primary Key
     */
    void deletePhoto(String path) {
        repository.deletePhoto(path);
    }

    /**
     * Delete all photos in DB
     */
    void deleteAll() {
        repository.deleteAll();
    }

    /**
     * Refresh the DB. Handled in the Repo object
     * @param context Context
     */
    void refreshDatabase(Context context) {
        repository.refreshDatabase(context);
    }
}