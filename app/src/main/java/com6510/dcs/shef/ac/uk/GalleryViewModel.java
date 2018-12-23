package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

public class GalleryViewModel extends AndroidViewModel {
    private GalleryRepository repository;
    private LiveData<List<Photo>> photos;

    public GalleryViewModel(Application application) {
        super(application);
        /* create connection to repository */
        repository = new GalleryRepository(application);
        /* subscribe to live data */
        photos = repository.getAllPhotos();
    }

    LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    void insert(Photo photo) {
        repository.insert(photo);
    }

    /*
    public void scan() {
        repository.scan();
    }
    */
}