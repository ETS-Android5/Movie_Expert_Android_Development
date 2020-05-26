package com.example.android.movieexpert;

import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import com.example.android.movieexpert.data.MovieRepo;
import com.example.android.movieexpert.utils.MovieUtils;

import java.util.List;

public class SavedMoviesActivity extends AppCompatActivity implements MovieSearchAdapter.OnSearchItemClickListener, NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_search_results);

        mDrawerLayout = findViewById(R.id.drawer_layout_saved);

        NavigationView navigationView = findViewById(R.id.nv_nav_drawer_saved);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.saved_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        actionBar.setTitle("Saved Movies");

        RecyclerView savedReposRV = findViewById(R.id.rv_saved_repos);
        savedReposRV.setLayoutManager(new LinearLayoutManager(this));
        savedReposRV.setHasFixedSize(true);

        final MovieSearchAdapter adapter = new MovieSearchAdapter(this);
        savedReposRV.setAdapter(adapter);

        MovieRepoViewModel viewModel = ViewModelProviders.of(this).get(MovieRepoViewModel.class);
        viewModel.getAllMovieRepos().observe(this, new Observer<List<MovieRepo>>() {
            @Override
            public void onChanged(@Nullable List<MovieRepo> movieRepos) {
                adapter.updateSearchResults(movieRepos);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.nav_search:
                Intent savedReposIntent = new Intent(this, MovieSearchActivity.class);
                startActivity(savedReposIntent);
                return true;
            default:
                return false;
        }
    }
}
