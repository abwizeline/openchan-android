package com.digiapp.openchan.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.digiapp.openchan.database.ServerDetails;

import java.util.List;

/**
 * Created by artembogomaz on 3/8/2018.
 */
@Dao
public interface ServersDAO {
    @Query("SELECT * FROM ServerDetails")
    List<ServerDetails> getAll();

    @Query("SELECT * FROM ServerDetails WHERE id = :id")
    ServerDetails getById(int id);

    @Query("SELECT * FROM ServerDetails WHERE geo = :geo")
    ServerDetails getByGeo(String geo);

    @Query("SELECT * FROM ServerDetails WHERE type = 'default'")
    ServerDetails getDefault();

    @Query("SELECT * FROM ServerDetails ORDER BY ping DESC LIMIT 1")
    ServerDetails getBestBySpeed();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ServerDetails... adClicks);

    @Delete
    void delete(ServerDetails adClicks);

    @Query("DELETE FROM ServerDetails")
    public void nukeTable();
}
