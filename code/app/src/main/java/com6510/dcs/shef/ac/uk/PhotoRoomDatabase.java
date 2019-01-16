package com6510.dcs.shef.ac.uk;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

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
                    // doesn't do anything
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
