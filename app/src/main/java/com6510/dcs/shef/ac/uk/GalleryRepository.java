package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.List;

public class GalleryRepository extends ViewModel {
    private PhotoDao dbDao;
    private LiveData<List<Photo>> photos;
    private PhotoRoomDatabase db;

    public GalleryRepository(Application application) {
        db = PhotoRoomDatabase.getDatabase(application);
        dbDao = db.photoDao();

        /* fetch all photos from db */
        photos = dbDao.getAllPhotos();



        /* this is the order in which data is shown */
    }

    /* ------------ DB wrappers ------------- */

    public Photo getPhoto(String path) {
        Photo photo = new Photo(path);
        new GetAsyncTask(dbDao).execute(photo);
        return photo;
    }

    public void insertPhoto(Photo photo) {
        new InsertAsyncTask(dbDao).execute(photo);
    }

    public void deletePhoto(String path) {
        new DeleteAsyncTask(dbDao).execute(path);
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(dbDao).execute();
    }

    /* --------- Async DB implementations -----------*/

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

    private static class DeleteAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        DeleteAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deletePhoto(params[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        DeleteAllAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAllPhotos();
            return null;
        }
    }
}