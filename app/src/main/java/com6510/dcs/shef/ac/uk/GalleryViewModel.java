package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

public class GalleryViewModel extends AndroidViewModel {
    private final GalleryRepository repository;

    LiveData<List<ImageElement>> imageElements;

    public GalleryViewModel(Application application) {
        super(application);
        /* create connection to repository */
        repository = new GalleryRepository(application);
        /* subscribe to live data */
        imageElements = repository.getImageElements();
    }

    LiveData<List<ImageElement>> getImageElements() {
        if (imageElements == null) {
            imageElements = new MutableLiveData<List<ImageElement>>();
        }
        return imageElements;
    }

    public void scan() {
        repository.scan();
    }
}