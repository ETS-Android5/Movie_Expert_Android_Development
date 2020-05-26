package com.example.android.movieexpert;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.movieexpert.data.MovieRepo;
import com.example.android.movieexpert.data.MovieRepoRepository;
import com.example.android.movieexpert.data.Status;

import java.util.List;

public class MovieRepoViewModel extends AndroidViewModel {
    private MovieRepoRepository mMovieRepoRepository;

    public MovieRepoViewModel(Application application) {
        super(application);
        mMovieRepoRepository = new MovieRepoRepository(application);
    }

    public void insertMovieRepo(MovieRepo repo) {
        mMovieRepoRepository.insertMovieRepo(repo);
    }

    public void deleteMovieRepo(MovieRepo repo) {
        mMovieRepoRepository.deleteMovieRepo(repo);
    }



    public LiveData<MovieRepo> getMovieRepoByName(int id) {
        return mMovieRepoRepository.getMovieRepoByName(id);
    }

    public LiveData<List<MovieRepo>> getAllMovieRepos() {
        return mMovieRepoRepository.getAllMovieRepos();
    }
}
