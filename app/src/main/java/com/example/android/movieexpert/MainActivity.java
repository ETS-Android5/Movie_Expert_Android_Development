package com.example.android.movieexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.android.movieexpert.data.MovieRepo;
import com.example.android.movieexpert.utils.MovieUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MovieSearchAdapter.OnSearchItemClickListener, LoaderManager.LoaderCallbacks<String>, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String REPOS_ARRAY_KEY = "movieRepos";
    private static final String SEARCH_URL_KEY = "movieSearchURL";

    private static final int MOVIE_SEARCH_LOADER_ID = 0;
    public static boolean GrabPageNum;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mTitle;
    private TextView mLoadingErrorTV;
    private ProgressBar mLoadingPB;
    private DrawerLayout mDrawerLayout;


    private MovieSearchAdapter mMovieSearchAdapter;

    private int NumberOfPages;
    private MovieUtils.MovieSearchResults mResults;
    private ArrayList<MovieRepo> mRepos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = findViewById(R.id.main_title);
        mTitle.setText("Top Movies");
        mSearchBoxET = findViewById(R.id.et_search_box);
        mSearchResultsRV = findViewById(R.id.rv_search_results);
        mLoadingErrorTV = findViewById(R.id.tv_loading_error);
        mLoadingPB = findViewById(R.id.pb_loading);
        mDrawerLayout = findViewById(R.id.drawer_layout_search);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mMovieSearchAdapter = new MovieSearchAdapter(this);
        mSearchResultsRV.setAdapter(mMovieSearchAdapter);

        NavigationView navigationView = findViewById(R.id.nv_nav_drawer_search);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        //actionBar.setTitle("Search");

        if (savedInstanceState != null && savedInstanceState.containsKey(REPOS_ARRAY_KEY)) {
            mRepos = (ArrayList<MovieRepo>) savedInstanceState.getSerializable(REPOS_ARRAY_KEY);
            mMovieSearchAdapter.updateSearchResults(mRepos);
        }

        getSupportLoaderManager().initLoader(MOVIE_SEARCH_LOADER_ID, null, this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        discoverMovies(preferences);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private void discoverMovies(SharedPreferences preferences) {
        String sort = preferences.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default_value)
        );

        Log.d(TAG, "sort: " + sort);
        String url = MovieUtils.buildMovieDiscoverURL(sort, 0, 1, "", "", "");
        Log.d(TAG, "querying search URL: " + url);

        if(sort.equals("popularity.desc")){
            Log.d(TAG, "popularity.desc");
            mTitle.setText("Top Popular Movies");
        }
        else if(sort.equals("release_date.desc")){
            Log.d(TAG, "release_date.desc");
            mTitle.setText("Top New Movies");
        }
        else if(sort.equals("vote_average.desc")){
            Log.d(TAG, "vote_average.desc");
            mTitle.setText("Top Rated Movies");
        }
        else if(sort.equals("vote_count.desc")){
            Log.d(TAG, "vote_count.desc");
            mTitle.setText("Top Voted Movies");
        }


        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(MOVIE_SEARCH_LOADER_ID, args, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, MainSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onSearchItemClick(MovieRepo repo) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieUtils.EXTRA_MOVIE_REPO, repo);
        startActivity(intent);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRepos != null) {
            outState.putSerializable(REPOS_ARRAY_KEY, mRepos);
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
        String url = null;
        if (bundle != null) {
            url = bundle.getString(SEARCH_URL_KEY);
        }
        return new MovieSearchLoader(this, url);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {
        Log.d(TAG, "loader finished loading");
        if (s != null) {
            mLoadingErrorTV.setVisibility(View.INVISIBLE);
            mSearchResultsRV.setVisibility(View.VISIBLE);
            mResults = MovieUtils.parseMovieResults(s);
            mRepos = mResults.results;
            NumberOfPages = mResults.total_pages;

            mMovieSearchAdapter.updateSearchResults(mRepos);
        } else {
            mLoadingErrorTV.setVisibility(View.VISIBLE);
            mSearchResultsRV.setVisibility(View.INVISIBLE);
        }
        mLoadingPB.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // Nothing to do here...
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_search:
                Intent movieSearchIntent = new Intent(this, MovieSearchActivity.class);
                startActivity(movieSearchIntent);
                return true;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.nav_saved:
                Intent savedReposIntent = new Intent(this, SavedMoviesActivity.class);
                startActivity(savedReposIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        discoverMovies(sharedPreferences);
    }
}