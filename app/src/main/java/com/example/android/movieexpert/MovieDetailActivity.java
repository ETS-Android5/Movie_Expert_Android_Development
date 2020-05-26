package com.example.android.movieexpert;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;

import androidx.core.app.ShareCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.android.movieexpert.data.MovieRepo;
import com.example.android.movieexpert.utils.MovieUtils;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView mImageView;
    private ImageView mImagePoster;
    private TextView mImageText;

    private boolean saved = false;

    private View mView;

    private TextView mTitle;
    private TextView mRating;
    private TextView mOverview;
    private TextView mExtra;



    private MovieRepoViewModel mMovieRepoViewModel;
    private MovieRepo mRepo;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_detail);

        mTitle = findViewById(R.id.tv_title);
        mRating = findViewById(R.id.tv_rating);
        mOverview = findViewById(R.id.tv_overview);
        mExtra = findViewById(R.id.tv_extra);
        mImageView = findViewById(R.id.poster_img);
        mImagePoster = findViewById(R.id.poster_background);
        mImageText = findViewById(R.id.No_Image);

        mMovieRepoViewModel = ViewModelProviders.of(this).get(MovieRepoViewModel.class);

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Details");

        mRepo = null;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MovieUtils.EXTRA_MOVIE_REPO)) {
            mRepo = (MovieRepo) intent.getSerializableExtra(MovieUtils.EXTRA_MOVIE_REPO);
            String rating = "Rating: " + String.valueOf(mRepo.vote_average) + "/10     Votes: " + String.valueOf(mRepo.vote_count);
            String extra = "Release Date: " + mRepo.release_date + "     Language: " + mRepo.original_language;
            mTitle.setText(mRepo.title);
            mRating.setText(rating);
            mOverview.setText(mRepo.overview);
            mExtra.setText(extra);

            if(mRepo.poster_path != null && mRepo.backdrop_path != null) {
                String posterURL = MovieUtils.buildMoviePosterURL(mRepo.backdrop_path);
                String iconURL = MovieUtils.buildMoviePosterURL(300, mRepo.poster_path);

                Log.d("iconURL", iconURL);
                Log.d("posterURL", posterURL);

                mImageView.setVisibility(View.VISIBLE);
                mImagePoster.setVisibility(View.VISIBLE);
                mImageText.setVisibility(View.INVISIBLE);

                Glide.with(this).load(posterURL).transition(DrawableTransitionOptions.withCrossFade()).into(mImagePoster);
                Glide.with(this).load(iconURL).transition(DrawableTransitionOptions.withCrossFade()).into(mImageView);
            } else if(mRepo.poster_path != null){
                String iconURL = MovieUtils.buildMoviePosterURL(300, mRepo.poster_path);

                Log.d("iconURL", iconURL);

                mImageView.setVisibility(View.VISIBLE);
                mImagePoster.setVisibility(View.INVISIBLE);
                mImageText.setVisibility(View.INVISIBLE);
                Glide.with(this).load(iconURL).transition(DrawableTransitionOptions.withCrossFade()).into(mImageView);
            } else {
                mImageView.setVisibility(View.INVISIBLE);
                mImagePoster.setVisibility(View.VISIBLE);
              //  String posterURL = MovieUtils.buildMoviePosterURL(mRepo.backdrop_path);
                Glide.with(this).load("https://www.mememaker.net/api/bucket?path=static/img/memes/full/2017/Apr/19/16/no-worries-its-all-good.jpg").transition(DrawableTransitionOptions.withCrossFade()).into(mImagePoster);
                //mImagePoster.setVisibility(View.INVISIBLE);
                mImageText.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repo_detail, menu);
        menuItem = menu.findItem(R.id.action_save);
        if(mRepo.saved == true){
            menuItem.setIcon(R.drawable.ic_bookmark_black_24dp);
        } else {
            menuItem.setIcon(R.drawable.ic_bookmark_border_black_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                shareRepo();
                return true;
            case R.id.action_save:
                saveRepo();
                if(saved == true) {
                    item.setIcon(R.drawable.ic_bookmark_black_24dp);
                }else{
                    item.setIcon(R.drawable.ic_bookmark_border_black_24dp);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareRepo() {
        if (mRepo != null) {
            String shareText = getString(R.string.share_repo_text, mRepo.title, mRepo.overview);
            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setText(shareText)
                    .setChooserTitle(R.string.share_chooser_title)
                    .startChooser();
        }
    }

    public void saveRepo() {
        if (mRepo != null) {
            if(mRepo.saved == false){
                mRepo.saved = true;
                mMovieRepoViewModel.insertMovieRepo(mRepo);
                saved = true;
            }else{
                mRepo.saved = false;
                mMovieRepoViewModel.insertMovieRepo(mRepo);
                saved = false;
            }
        }
    }
}
