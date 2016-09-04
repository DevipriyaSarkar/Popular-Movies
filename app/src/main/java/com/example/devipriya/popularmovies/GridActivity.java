package com.example.devipriya.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static com.example.devipriya.popularmovies.Utilities.TMDB_API_KEY;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */

public class GridActivity extends AppCompatActivity {

    static String TAG = GridActivity.class.getSimpleName();
    RecyclerView movieGrid;
    GridLayoutManager gridLayoutManager;
    CustomMovieGridAdapter customMovieGridAdapter;
    FloatingActionButton tabFavFab;
    TabFabCallback tabFabCallback;
    ArrayList<MovieItem> movieArrayList;
    ProgressDialog pDialog;
    TextView noFavText;
    String curDateString, prevDateString;
    String urlMovies;

    private boolean mTwoPane; //whether or not the activity is in two-pane mode, i.e. running on a tablet device


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(ContextCompat.getDrawable(getApplicationContext(), R.drawable.logo));

        //format date
        Calendar calendar = Calendar.getInstance();
        Date curDate = calendar.getTime();
        calendar.add(Calendar.MONTH, -1);
        Date prevDate = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        curDateString = format.format(curDate);
        prevDateString = format.format(prevDate);

        urlMovies = "https://api.themoviedb.org/3/discover/movie?primary_release_date.gte=" + prevDateString +
                "&primary_release_date.lte=" + curDateString + "&api_key=" + TMDB_API_KEY;

        movieArrayList = new ArrayList<>();
        movieGrid = (RecyclerView) findViewById(R.id.movieGrid);
        noFavText = (TextView) findViewById(R.id.noFavText);
        Typeface notoRegular = Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf");
        noFavText.setTypeface(notoRegular); // set custom font

        // default number of columns according to orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, 3);
            movieGrid.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager = new GridLayoutManager(this, 5);
            movieGrid.setLayoutManager(gridLayoutManager);
        }

        // to change number of columns dynamically
        movieGrid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        movieGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int viewWidth = movieGrid.getMeasuredWidth();
                        float cardViewWidth = getResources().getDimension(R.dimen.movie_cardview_layout_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        gridLayoutManager.setSpanCount(newSpanCount);
                        gridLayoutManager.requestLayout();
                    }
                });

        customMovieGridAdapter = new CustomMovieGridAdapter(this, movieArrayList);
        movieGrid.setAdapter(customMovieGridAdapter);

        //initialising progress dialog box
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.progress_dialog_message));
        pDialog.setCancelable(false);

        Log.d("INTERNET", isInternetAvailable() ? "yes" : "no");

        if (isInternetAvailable()) {
            makeJsonObjectRequest();    // retrieve data from API if internet available
        } else {
            retrieveFavFromDB();        // else retrieve from local db
        }

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            tabFavFab = (FloatingActionButton) findViewById(R.id.tabFavFab);
            tabFavFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(tabFabCallback != null) {
                        tabFabCallback.onFabClicked(tabFavFab);
                    }
                }
            });
        }

    }

    //Method to make json object request
    private void makeJsonObjectRequest() {

        //show the progress dialog
        showpDialog();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlMovies, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    movieArrayList = new ArrayList<>();
                    JSONArray results = response.getJSONArray("results");
                    try {
                        // Parsing json array response
                        // loop through each json object
                        for (int i = 0; i < results.length(); i++) {

                            JSONObject movie = (JSONObject) results.get(i);

                            String poster_path = movie.getString("poster_path");
                            String backdrop_path = movie.getString("backdrop_path");
                            String title = movie.getString("title");
                            String overview = movie.getString("overview");
                            String release_date = movie.getString("release_date");
                            double vote_average = movie.getDouble("vote_average");
                            long movie_id = movie.getLong("id");

                            MovieItem item = new MovieItem(poster_path, backdrop_path, title, overview, release_date, vote_average, movie_id);
                            movieArrayList.add(item);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                }
                //update the UI as the information is fetched
                updateUI();
                // hide the progress dialog
                hidepDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                hidepDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    //show dialog box
    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    //hide dialog box
    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //update UI
    public void updateUI() {
        movieGrid.setVisibility(View.VISIBLE);
        noFavText.setVisibility(View.GONE);
        customMovieGridAdapter = new CustomMovieGridAdapter(this, movieArrayList);
        movieGrid.setAdapter(customMovieGridAdapter);
        customMovieGridAdapter.notifyDataSetChanged();

        customMovieGridAdapter.setOnItemClickListener(new CustomMovieGridAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (mTwoPane) {     // tablet mode
                    MovieItem selectedMovie = customMovieGridAdapter.getItem(position);
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(MovieFragment.ARG_MOVIE_ID, selectedMovie);
                    arguments.putBoolean(MovieFragment.ARG_INTERNET, isInternetAvailable());
                    MovieFragment fragment = new MovieFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                } else {        // phone mode
                    MovieItem selectedMovie = customMovieGridAdapter.getItem(position);
                    Intent i = new Intent(getApplicationContext(), MovieActivity.class);
                    i.putExtra("curMovie", selectedMovie);
                    i.putExtra("internet", isInternetAvailable());
                    startActivity(i);
                }
            }
        });
    }

    //check for internet connectivity
    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    //retrieve favourite movies from db when offline
    public void retrieveFavFromDB() {
        //initialise the array list
        movieArrayList = new ArrayList<>();

        //get all the movies in the db
        SharedPreferences mPrefs = getSharedPreferences("FAVOURITE_MOVIES", MODE_PRIVATE);
        Map<String, ?> keys = mPrefs.getAll();

        if (!keys.isEmpty()) {
            //favourite movies exist
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                //extract the object, MovieItem from the string stored in db
                Gson gson = new Gson();
                String json = mPrefs.getString(entry.getKey(), "");
                MovieItem movieItem = gson.fromJson(json, MovieItem.class);

                //add the object to the array list
                movieArrayList.add(movieItem);
            }
            //update the UI
            updateUI();
        } else {
            //no favourite movies added
            //display message that no movies added to favourite db
            movieGrid.setVisibility(View.INVISIBLE);
            noFavText.setVisibility(View.VISIBLE);
        }
    }

    // to check the first menu item by default
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isInternetAvailable())
            menu.findItem(R.id.menu_sort_popularity).setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }

    // decide which menu to inflate
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isInternetAvailable()) {
            MenuInflater inflater = getMenuInflater();
            if (!mTwoPane) {
                if (isInternetAvailable()) {  //phone mode and internet available - enable filter feature
                    inflater.inflate(R.menu.menu_phone_grid, menu);
                    return true;
                } else {
                    return super.onCreateOptionsMenu(menu);
                }
            } else {
                if (isInternetAvailable()) {
                    inflater.inflate(R.menu.menu_tab_grid, menu);   //tablet mode and internet available - enable filter and share feature
                    return true;
                } else {
                    inflater.inflate(R.menu.menu_phone_movie, menu);    //tablet mode and internet not available - enable only share feature
                    return true;
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                // Not implemented here
                return false;
            case R.id.menu_sort_popularity:
                // sort by popularity
                urlMovies = "https://api.themoviedb.org/3/discover/movie?primary_release_date.gte=" + prevDateString +
                        "&primary_release_date.lte=" + curDateString + "&api_key=" + TMDB_API_KEY + "&sort_by=popularity.desc";
                makeJsonObjectRequest();
                item.setChecked(true);
                return true;
            case R.id.menu_sort_rating:
                // sort by rating
                urlMovies = "https://api.themoviedb.org/3/discover/movie?primary_release_date.gte=" + prevDateString +
                        "&primary_release_date.lte=" + curDateString + "&api_key=" + TMDB_API_KEY + "&sort_by=vote_average.desc";
                makeJsonObjectRequest();
                item.setChecked(true);
                return true;
            case R.id.menu_sort_favourite:
                // show the favourites
                retrieveFavFromDB();
                item.setChecked(true);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // callback when the the fab is clicked in tablet - implemented in MovieFragment
    public interface TabFabCallback {
        void onFabClicked(View favFab);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        tabFabCallback = (TabFabCallback) fragment;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        tabFabCallback = null;
    }

}
