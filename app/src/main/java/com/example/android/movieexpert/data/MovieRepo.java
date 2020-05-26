package com.example.android.movieexpert.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "SavedMovies")
public class MovieRepo implements Serializable {
    @NonNull
    @PrimaryKey
    public int id;

    public String poster_path;
    public String backdrop_path;
    public String overview;
    public String release_date;
    public String original_title;
    public String original_language;
    public String title;
    public Double popularity;
    public Double vote_average;
    public Integer vote_count;
    public boolean saved;
}