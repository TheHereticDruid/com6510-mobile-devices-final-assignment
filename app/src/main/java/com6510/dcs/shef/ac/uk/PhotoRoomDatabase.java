package com6510.dcs.shef.ac.uk;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

@Database(entities = {Photo.class}, version = 1, exportSchema = false)
public abstract class PhotoRoomDatabase extends RoomDatabase {
    public abstract PhotoDao photoDao();
    private static PhotoRoomDatabase INSTANCE;
    private static Context saved_context;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    //new PopulateDbAsync(INSTANCE, saved_context).execute();
                }
            };

    public static PhotoRoomDatabase getDatabase(final Context context) {
        saved_context = context;
        if (INSTANCE == null) {
            synchronized (PhotoRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        PhotoRoomDatabase.class,
                        "photo-database")
                        .fallbackToDestructiveMigration()
                        .addCallback(sRoomDatabaseCallback)
                        .build();
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private PhotoDao mDao;
        private Context context;

        PopulateDbAsync(PhotoRoomDatabase db, Context context) {
            mDao = db.photoDao();
            this.context = context;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            /* find all photos on phone and populate */
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
            System.out.println("Adding photos to db.");
            mDao.insertAllPhotos(foundPhotos);
            return null;
        }
    }
}
