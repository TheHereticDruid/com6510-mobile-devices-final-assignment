package com6510.dcs.shef.ac.uk;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {Photo.class}, version = 1, exportSchema = false)
public abstract class PhotoRoomDatabase extends RoomDatabase {
    public abstract PhotoDao photoDao();
    private static PhotoRoomDatabase INSTANCE;
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    public static PhotoRoomDatabase getDatabase(final Context context) {
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

        PopulateDbAsync(PhotoRoomDatabase db) {
            mDao = db.photoDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            /* find all photos on phone and populate */
            return null;
        }
    }
}
