package com6510.dcs.shef.ac.uk;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {ImageElement.class}, version = 1)
public abstract class ImageDatabase extends RoomDatabase {
    public abstract ImageDao imageDao();
}
