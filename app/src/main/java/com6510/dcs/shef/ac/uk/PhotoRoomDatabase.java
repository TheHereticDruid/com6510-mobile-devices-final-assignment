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
}
