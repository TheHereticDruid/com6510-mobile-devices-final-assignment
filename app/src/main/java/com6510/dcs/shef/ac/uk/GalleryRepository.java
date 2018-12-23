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
    private Application application;

    public GalleryRepository(Application application) {
        this.application = application;
        db = PhotoRoomDatabase.getDatabase(application);
        dbDao = db.photoDao();
        photos = dbDao.getAllPhotos(); /* fetch all photos from db */
        System.out.println("Got " + photos.getValue().size() + " from db");
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    public void insert(Photo image) {
        new InsertAsyncTask(dbDao).execute(image);
    }

    private static class InsertAsyncTask extends AsyncTask<Photo, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        InsertAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Photo... params) {
            mAsyncTaskDao.insert(params[0]);
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

    /*
    public void scan() {
        String[] projection = {MediaStore.Photos.Media.DATA};
        Cursor cursor = MediaStore.Photos.Media.query(application.getContentResolver(),
                MediaStore.Photos.Media.EXTERNAL_CONTENT_URI,
                projection);
        System.out.println("Found " + cursor.getCount() + " photos.");
        List<Photo> foundPhotos = new ArrayList<Photo>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex("_data"));
            foundPhotos.add(new Photo(path));
            System.out.println(path);
            cursor.moveToNext();
        }
        */
    /* add all to db *//*

        System.out.println("Adding photos to db.");
        PhotoDao dbDao = db.imageDao();
        dbDao.insertAllPhotos(foundPhotos);
    }
*/
}