package com.example.android.movieexpert;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.preference.PreferenceManager;

import com.example.android.movieexpert.data.MovieRepo;
import com.example.android.movieexpert.utils.MovieUtils;

import java.util.ArrayList;

public class MovieSearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MovieSearchAdapter.OnSearchItemClickListener, LoaderManager.LoaderCallbacks<String>, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = MovieSearchActivity.class.getSimpleName();
    private static final String REPOS_ARRAY_KEY = "movieRepos";
    private static final String SEARCH_URL_KEY = "movieSearchURL";

    private static final int MOVIE_SEARCH_LOADER_ID = 0;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
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
        setContentView(R.layout.activity_search);

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

        if (savedInstanceState != null && savedInstanceState.containsKey(REPOS_ARRAY_KEY)) {
            mRepos = (ArrayList<MovieRepo>) savedInstanceState.getSerializable(REPOS_ARRAY_KEY);
            mMovieSearchAdapter.updateSearchResults(mRepos);
        }

        getSupportLoaderManager().initLoader(MOVIE_SEARCH_LOADER_ID, null, this);

        Button searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                if (!TextUtils.isEmpty(searchQuery)) {
                    doMovieSearch(searchQuery);
                }
            }
        });
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
                getString(R.string.pref_sort_default)
        );
        String year = preferences.getString(
                getString(R.string.pref_release_year_key),
                getString(R.string.pref_release_year_default)
        );

        String votecount = preferences.getString(
                getString(R.string.pref_vote_count_key),
                getString(R.string.pref_vote_count_default)
        );

        String vote = preferences.getString(
                getString(R.string.pref_release_year_key),
                getString(R.string.pref_release_year_default)
        );


        String url = MovieUtils.buildMovieDiscoverURL(sort, Integer.parseInt(year), 1,votecount, "", "");
        Log.d(TAG, "querying search URL: " + url);

        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(MOVIE_SEARCH_LOADER_ID, args, this);
    }


    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doMovieSearch(String query) {
        String url = MovieUtils.buildMovieSearchURL(query);
        Log.d(TAG, "querying search URL: " + url);

        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(MOVIE_SEARCH_LOADER_ID, args, this);
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
        Log.d(TAG, "loader finished");
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

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
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