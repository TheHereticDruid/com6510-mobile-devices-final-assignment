package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GalleryRepository extends ViewModel {
    private PhotoDao dbDao;
    private LiveData<List<Photo>> photos;
    private PhotoRoomDatabase db;

    public GalleryRepository(Application application) {
        db = PhotoRoomDatabase.getDatabase(application);
        dbDao = db.photoDao();
        /* live photos from db */
        photos = dbDao.getAllPhotos();
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

    public static void indexFile(PhotoRoomDatabase db, String path, String thumbnailDir) {
        PhotoDao dao = db.photoDao();
        System.out.println("Putting into db: " + path);
        File sourceFile = new File(path);

        /* generate thumbnail */
        Bitmap original_bitmap = BitmapFactory.decodeFile(path); /* read photo from disk */
        Bitmap thumbnail_bitmap = Bitmap.createScaledBitmap(original_bitmap, 100, 100, true);
        File thumbnail_file = new File(thumbnailDir, sourceFile.getName() + "-" + sourceFile.lastModified());
        try (FileOutputStream out = new FileOutputStream(thumbnail_file.getAbsolutePath())) {
            thumbnail_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* create Photo object to insert in db */
        Photo photo = new Photo(path);
        photo.setImThumbPath(thumbnail_file.getAbsolutePath());
        photo.setImTimestamp(sourceFile.lastModified());
        photo.setImLat(0);
        photo.setImLng(0);
        photo.setImTitle("");

        /* delete old copy from db */
        //dao.deletePhoto(path);

        /* insert in db */
        dao.insertPhoto(photo);
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
            for (Photo p: db_photos) {
                db_photos_map.put(p.getImPath(), p);
            }
            System.out.println("Found " + db_photos_map.size() + " photos in db.");

            /* find all photos using mediastore */
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection);
            System.out.println("Indexing " + cursor.getCount() + " photos on phone.");
            Set<String> filesToBeIndexed = new HashSet<String>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String path = cursor.getString(cursor.getColumnIndex("_data"));
                filesToBeIndexed.add(new File(path).getAbsolutePath());
                //System.out.println("Indexing: " + path);
                cursor.moveToNext();
            }

            /* delete photos from db that do not exist anymore */
            for (Photo photo : db_photos) {
                if (filesToBeIndexed.contains(photo.getImPath()) == false) {
                    /* delete this photo from db */
                    //System.out.println("Db entry does not exist anymore, deleting: " + photo.getImPath());
                    mDao.deletePhoto(photo.getImPath());
                }
            }

            /* delete stale thumbnails */
            File thumbnailDirectory = new File(context.getCacheDir(), "thumbnails");
            Map<String, Photo> thumb_photos_map = new HashMap<String, Photo>();
            for (Photo p: db_photos) {
                thumb_photos_map.put(p.getImThumbPath(), p);
            }
            for (File f : thumbnailDirectory.listFiles()) {
                if (!thumb_photos_map.containsKey(f.getAbsolutePath())) {
                    f.delete();
                }
            }

            /* create thumbnail dir */
            File thumbnailDir = new File(context.getCacheDir(), "thumbnails");
            thumbnailDir.mkdir();

            /* read files, create thumbnails and store in db */
            for (String path : filesToBeIndexed) {
                /* photo already exists in db */
                if (db_photos_map.containsKey(path)) {
                    //System.out.println("Photo already exists in db: " + path);
                    long db_ts = db_photos_map.get(path).getImTimestamp();
                    long ix_ts = new File(path).lastModified();
                    /* check modified timestamp in db is old */
                    if (db_ts < ix_ts) {
                        System.out.println("Db entry old, needs updating: " + path);
                        /* need to update photo in db */
                        indexFile(db, path, thumbnailDir.getAbsolutePath());
                    }
                } else {
                    indexFile(db, path, thumbnailDir.getAbsolutePath());
                }
            }

            /* debug */
            System.out.println("Thumbnail dir: " + thumbnailDirectory.getAbsolutePath());
            for (File f : thumbnailDirectory.listFiles()) {
                //System.out.println(f.getAbsolutePath());
            }

            return null;
        }
    }
}