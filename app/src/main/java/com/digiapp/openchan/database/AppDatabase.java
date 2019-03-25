package com.digiapp.openchan.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.digiapp.openchan.database.dao.ServersDAO;

/**
 * Created by artembogomaz on 3/8/2018.
 */

@Database(entities = {ServerDetails.class}, version = 2,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ServersDAO ServersDAO();
}
