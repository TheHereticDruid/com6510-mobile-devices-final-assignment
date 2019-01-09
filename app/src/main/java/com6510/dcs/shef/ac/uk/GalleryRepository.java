package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GalleryRepository extends ViewModel {
    private PhotoDao dbDao;
    private LiveData<List<Photo>> photos;
    private LiveData<List<Photo>> filteredPhotos;
    private PhotoRoomDatabase db;

    public GalleryRepository(Application application) {
        db = PhotoRoomDatabase.getDatabase(application);
        dbDao = db.photoDao();
        /* live photos from db */
        photos = dbDao.getAllPhotos();
    }

    /* ------------ DB wrappers ------------- */

    public Photo getPhoto(String path) {
        Photo photo = new Photo(path, "", 0, "", "", 0, 0, false, "");
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

    public LiveData<List<Photo>> getFilteredPhotos(String title, String description, String date) {
        return dbDao.getFilteredPhotos(title, description, date);
    }

    public List<Photo> getAllPhotosSync() {
        return dbDao.getAllPhotosSync();
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(dbDao).execute();
    }

    public void refreshDatabase(Context context) {
        System.out.println("Starting scan async task");
        new ScanAsyncTask(db, context).execute();
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

    private static class ScanAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private PhotoDao mDao;
        PhotoRoomDatabase db;

        ScanAsyncTask(PhotoRoomDatabase db, Context context) {
            this.db = db;
            this.mDao = db.photoDao();
            this.context = context;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            /* current db photos */
            List<Photo> db_photos = mDao.getAllPhotosSync();
            Map<String, Photo> db_photos_map = new HashMap<String, Photo>();
            for (Photo p : db_photos) {
                db_photos_map.put(p.getImPath(), p);
            }
            System.out.println("Found " + db_photos_map.size() + " photos in db.");

            /* find all photos using mediastore */
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection);
            System.out.println("Found " + cursor.getCount() + " photos on phone.");
            Set<String> pathsToBeIndexed = new HashSet<String>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String path = cursor.getString(cursor.getColumnIndex("_data"));
                pathsToBeIndexed.add(path);
                cursor.moveToNext();
            }

            /* delete photos from db that do not exist anymore */
            for (Photo photo : db_photos) {
                if (pathsToBeIndexed.contains(photo.getImPath()) == false) {
                    /* delete this photo from db */
                    System.out.println("Db entry does not exist anymore, deleting: " + photo.getImPath());
                    mDao.deletePhoto(photo.getImPath());
                }
            }

            /* create thumbnail dir if not already created */
            File thumbnailDir = new File(context.getCacheDir(), "thumbnails");
            thumbnailDir.mkdir();
            System.out.println("Thumbnail dir: " + thumbnailDir.getAbsolutePath());
            System.out.println("Number of thumbnails on disk: " + thumbnailDir.listFiles().length);

            // delete stale thumbnails TODO - move to async thread
            /*
            Map<String, Photo> thumb_photos_map = new HashMap<String, Photo>();
            for (Photo p : db_photos) {
                thumb_photos_map.put(p.getImThumbPath(), p);
            }
            for (File f : thumbnailDir.listFiles()) {
                if (thumb_photos_map.containsKey(f.getAbsolutePath()) == false) {
                    f.delete();
                }
            }
            */

            List<Photo> photosToInsert = new ArrayList<>();

            /* read files, create thumbnails and store in db */
            for (String path : pathsToBeIndexed) {
                File file = new File(path);
                /* photo already exists in db */
                if (db_photos_map.containsKey(path)) {
                    //System.out.println("Photo already exists in db: " + path);
                } else {
                    /* create Photo object to insert in db */
                    Photo photo = new Photo(
                            path,
                            Util.getNewThumbnailPath(context),
                            file.lastModified(),
                            file.getName(),
                            "Add a description!",
                            0,
                            0,
                            false,
                            "");
                    /* timestamp is set so that order of photos remains invariant as they appear on grid */
                    photosToInsert.add(photo);
                }
            }

            /* insert all photos at once, resulting in only one onChanged callback */
            System.out.println("Inserting " + photosToInsert.size() + " scanned photos in db");
            mDao.insertAllPhotos(photosToInsert);
            return null;
        }
    }
}
