package com.example.android.movieexpert.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MovieRepoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieRepo repo);

    @Delete
    void delete(MovieRepo repo);

    @Query("SELECT * FROM savedmovies WHERE saved = 1")
    LiveData<List<MovieRepo>> getAllRepos();

    @Query("SELECT * FROM savedmovies WHERE id = :id LIMIT 1")
    LiveData<MovieRepo> getRepoByName(int id);

}
