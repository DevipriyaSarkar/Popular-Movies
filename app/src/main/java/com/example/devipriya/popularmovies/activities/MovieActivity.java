package com.example.devipriya.popularmovies.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.devipriya.popularmovies.R;
import com.example.devipriya.popularmovies.fragments.MovieFragment;
import com.example.devipriya.popularmovies.models.MovieItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Devipriya on 11-02-2016.
 */

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link GridActivity}.
 */

public class MovieActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView backdropImage;
    private FabCallback fabCallback;
    private BgCallback bgCallback;
    private FloatingActionButton favFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_movie);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        backdropImage = (ImageView) findViewById(R.id.backdropImage);
        favFab = (FloatingActionButton) findViewById(R.id.favFab);

        MovieItem currentMovie = getIntent().getExtras().getParcelable("curMovie");
        if(currentMovie != null) {
            final String movieName = currentMovie.getTitle();

            // show movie title on collapsing toolbar only when collapsed
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = true;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        collapsingToolbarLayout.setTitle(movieName);
                        isShow = true;
                    } else if(isShow) {
                        collapsingToolbarLayout.setTitle(" ");  // careful there should a space between double quote otherwise it wont work
                        isShow = false;
                    }
                }
            });

            // setting the backdrop image on the collapsing toolbar
            String backdropPath = currentMovie.getBackdrop_path();
            backdropPath = backdropPath.startsWith("\\") ? backdropPath.substring(1) : backdropPath;
            String fullImageURL = "https://image.tmdb.org/t/p/w300" + backdropPath;
            Picasso.with(getApplicationContext())
                    .load(fullImageURL)
                    .error(R.drawable.null_movie)
                    .into(backdropImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) backdropImage.getDrawable()).getBitmap();
                            if (bitmap != null) {
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                                        collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));
                                        if(bgCallback != null) {
                                            bgCallback.onPaletteLoaded(palette);    // callback to change the background color
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

        favFab.setOnClickListener(new View.OnClickListener() {  // add movie to favourites
            @Override
            public void onClick(View v) {
                if(fabCallback != null) {
                    fabCallback.onFabClicked(favFab);
                }
            }
        });

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieFragment.ARG_MOVIE_ID, currentMovie);
            arguments.putBoolean(MovieFragment.ARG_INTERNET, getIntent().getBooleanExtra("internet", false));
            MovieFragment fragment = new MovieFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_phone_movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                // Not implemented here
                return false;
            default:
                break;
        }

        return false;
    }

    // callback when the the fab is clicked - implemented in MovieFragment
    public interface FabCallback {
        void onFabClicked(View favFab);
    }

    // callback when the palette from the backdrop movie image is generated
    public interface BgCallback {
        void onPaletteLoaded(Palette palette);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        fabCallback = (FabCallback) fragment;
        bgCallback = (BgCallback) fragment;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        fabCallback = null;
        bgCallback = null;
    }
}