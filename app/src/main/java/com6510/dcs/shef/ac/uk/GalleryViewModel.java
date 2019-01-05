package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

public class GalleryViewModel extends AndroidViewModel {
    private GalleryRepository repository;
    private LiveData<List<Photo>> photos;
    private LiveData<List<Photo>> filteredPhotos;
    private String title;
    private String date;

    public GalleryViewModel(Application application) {
        super(application);
        /* create connection to repository */
        this.title="%%";
        this.date="%%";
        repository = new GalleryRepository(application);
        /* subscribe to live data */
        photos = repository.getAllPhotos();
        filteredPhotos = repository.getFilteredPhotos(title, date);
    }

    public GalleryRepository getRepository() {
        return repository;
    }

    LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    LiveData<List<Photo>> getFilteredPhotos(String title, String date) {
        this.title=title;
        this.date=date;
        filteredPhotos = repository.getFilteredPhotos(title, date);
        return filteredPhotos;
    }

    List<Photo> getAllPhotosSync() { return repository.getAllPhotosSync(); }

    void insertPhoto(Photo photo) {
        repository.insertPhoto(photo);
    }

    void deletePhoto(String path) {
        repository.deletePhoto(path);
    }

    void deleteAll() {
        repository.deleteAll();
    }

    void refreshDatabase(Context context) {
        repository.refreshDatabase(context);
    }
}