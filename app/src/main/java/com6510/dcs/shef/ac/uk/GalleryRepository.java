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

/**
Gallery Repo object, which is a View Model Implementation as well
 */
public class GalleryRepository extends ViewModel {
    private PhotoDao dbDao;
    private LiveData<List<Photo>> photos;
    private LiveData<List<Photo>> filteredPhotos;
    private PhotoRoomDatabase db;

    /**
     * Constructor. Init the DAO here, along with the DB itself
     * @param application Application
     */
    public GalleryRepository(Application application) {
        db = PhotoRoomDatabase.getDatabase(application);
        dbDao = db.photoDao();
        /* live photos from db */
        photos = dbDao.getAllPhotos();
    }

    /* ------------ DB wrappers ------------- */

    /**
     * Get Photo from DB
     * @param path Primary Key
     * @return Photo in question
     */
    public Photo getPhoto(String path) {
        Photo photo = new Photo(path, "", 0, "", "", 0, 0, false, "", "", "", "");
        new GetAsyncTask(dbDao).execute(photo);
        return photo;
    }

    /**
     * Insert Photo into DB
     * @param photo Photo in question
     */
    public void insertPhoto(Photo photo) {
        new InsertAsyncTask(dbDao).execute(photo);
    }

    /**
     * Delete Photo from DB
     * @param path Primary Key
     */
    public void deletePhoto(String path) {
        new DeleteAsyncTask(dbDao).execute(path);
    }

    /**
     * Get all Photos in the DB using LiveData
     * @return All photos
     */
    public LiveData<List<Photo>> getAllPhotos() {
        return photos;
    }

    /**
     * Get Photos from the DB using some filtered requirements. Defaults to no filters
     * @param title Title
     * @param description Description
     * @param date Date
     * @param artist Artist
     * @param make Make
     * @param model Model
     * @return List of photos
     */
    public LiveData<List<Photo>> getFilteredPhotos(String title, String description, String date, String artist, String make, String model) {
        return dbDao.getFilteredPhotos(title, description, date, artist, make, model);
    }

    /**
     * Get all photos in the DB synchronously
     * @return List of photos
     */
    public List<Photo> getAllPhotosSync() {
        return dbDao.getAllPhotosSync();
    }

    /**
     * Delete all photos in DB
     */
    public void deleteAll() {
        new DeleteAllAsyncTask(dbDao).execute();
    }

    /**
     * Refresh the DB, locating all photos afresh.
     * @param context Context
     */
    public void refreshDatabase(Context context) {
        System.out.println("Starting scan async task");
        new ScanAsyncTask(db, context).execute();
    }

    /* --------- Async DB implementations -----------*/

    /**
     * Async class for Getting a Photo
     */
    private static class GetAsyncTask extends AsyncTask<Photo, Void, LiveData<Photo>> {
        private PhotoDao mAsyncTaskDao;

        /**
         * Constructor
         * @param dao DAO of Photo
         */
        GetAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        /**
         * Async method to retrieve one Photo
         * @param params Parameters
         * @return Photo
         */
        @Override
        protected LiveData<Photo> doInBackground(final Photo... params) {
            Photo photo = params[0];
            return mAsyncTaskDao.getPhoto(photo.getImPath());
        }
    }

    /**
     * Async class for Inserting a Photo
     */
    private static class InsertAsyncTask extends AsyncTask<Photo, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        /**
         * Constructor
         * @param dao DAO of Photo
         */
        InsertAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        /**
         * Async method to insert one photo
         * @param params Parameters
         * @return Void
         */
        @Override
        protected Void doInBackground(final Photo... params) {
            mAsyncTaskDao.insertPhoto(params[0]);
            return null;
        }
    }

    /**
     * Async Class for Deleting from DB
     */
    private static class DeleteAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        /**
         * Constructor
         * @param dao DAO of Photo
         */
        DeleteAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        /**
         * Async method to delete one photo
         * @param params Parameters
         * @return Void
         */
        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deletePhoto(params[0]);
            return null;
        }
    }

    /**
     * Async class for Deleting all from DB
     */
    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        /**
         * Constructor
         * @param dao DAO of Photo
         */
        DeleteAllAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        /**
         * Async method to delete all photos
         * @param params Parameters
         * @return Void
         */
        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAllPhotos();
            return null;
        }
    }

    /**
     * Async Class for Refreshing the DB
     */
    private static class ScanAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private PhotoDao mDao;
        PhotoRoomDatabase db;

        /**
         * Constructor
         * @param db Database Object
         * @param context Context
         */
        ScanAsyncTask(PhotoRoomDatabase db, Context context) {
            this.db = db;
            this.mDao = db.photoDao();
            this.context = context;
        }

        /**
         * Async method to scan the file system and retrieve and store all photos
         * @param params
         * @return
         */
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
            List<String> pathsToBeDeleted = new LinkedList<>();
            for (Photo photo : db_photos) {
                if (pathsToBeIndexed.contains(photo.getImPath()) == false) {
                    /* delete this photo from db */
                    System.out.println("Db entry does not exist anymore, deleting: " + photo.getImPath());
                    pathsToBeDeleted.add(photo.getImPath());
                }
            }
            mDao.deletePhotos(pathsToBeDeleted);

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
                            "", 0, 0,false,
                            "", "" , "", "");
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
