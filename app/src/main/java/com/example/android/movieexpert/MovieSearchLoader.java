package com.example.android.movieexpert;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.movieexpert.utils.NetworkUtils;

import java.io.IOException;

public class MovieSearchLoader extends AsyncTaskLoader<String> {

    private final static String TAG = MovieSearchLoader.class.getSimpleName();

    private String mSearchResultsJSON;
    private String mURL;

    MovieSearchLoader(Context context, String url) {
        super(context);
        mURL = url;
    }

    @Override
    protected void onStartLoading() {
        if (mURL != null) {
            if (mSearchResultsJSON != null) {
                MainActivity.GrabPageNum = false;
                deliverResult(mSearchResultsJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Nullable
    @Override
    public String loadInBackground() {
        if (mURL != null) {
            Log.d(TAG, "Loading results from Movie API: " + mURL);
            String results = null;
            try {
                results = NetworkUtils.doHTTPGet(mURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        } else {
            return null;
        }
    }

    @Override
    public void deliverResult(@Nullable String data) {
        mSearchResultsJSON = data;
        super.deliverResult(data);
    }
}
