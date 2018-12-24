package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.util.List;

public class GalleryRepository extends ViewModel {
    private PhotoDao dbDao;
    private LiveData<List<Photo>> photos;
    private PhotoRoomDatabase db;

    public GalleryRepository(Application application) {
        db = PhotoRoomDatabase.getDatabase(application);
        dbDao = db.photoDao();
        photos = dbDao.getAllPhotos(); /* fetch all photos from db */
        int num_photos = 0;
        if (photos.getValue() != null) {
            num_photos = photos.getValue().size();
        } else {
            System.out.println("photos.getValue is null");
        }
        System.out.println("Got " + num_photos + " from db");
    }

    /* DB wrappers */

    public Photo getPhoto(String path) {
        Photo photo = new Photo(path);
        new GetAsyncTask(dbDao).execute(photo);
        return photo;
    }

    public void insertPhoto(Photo photo) {
        new InsertAsyncTask(dbDao).execute(photo);
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    /* Async implementations */

    private static class GetAsyncTask extends AsyncTask<Photo, Void, LiveData<Photo>> {
        private PhotoDao mAsyncTaskDao;

        GetAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected LiveData<Photo> doInBackground(final Photo... params) {
            Photo photo = params[0];
            return mAsyncTaskDao.getPhoto(photo.getImPath());
        }
    }

    private static class InsertAsyncTask extends AsyncTask<Photo, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        InsertAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Photo... params) {
            mAsyncTaskDao.insertPhoto(params[0]);
            return null;
        }
    }

    private static class GetAllAsyncTask extends AsyncTask<Void, Void, LiveData<List<Photo>>> {
        private PhotoDao mAsyncTaskDao;

        GetAllAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected LiveData<List<Photo>> doInBackground(final Void... params) {
            return mAsyncTaskDao.getAllPhotos();
        }
    }
}