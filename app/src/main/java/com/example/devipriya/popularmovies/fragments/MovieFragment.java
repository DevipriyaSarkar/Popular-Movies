package com.example.devipriya.popularmovies.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.example.devipriya.popularmovies.R;
import com.example.devipriya.popularmovies.activities.GridActivity;
import com.example.devipriya.popularmovies.activities.MovieActivity;
import com.example.devipriya.popularmovies.application.AppController;
import com.example.devipriya.popularmovies.models.MovieItem;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.devipriya.popularmovies.utilities.Utilities.TMDB_API_KEY;

/**
 * Created by Devipriya on 04-11-2016.
 */

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link GridActivity}
 * in two-pane mode (on tablets) or a {@link MovieActivity}
 * on handsets.
 */

public class MovieFragment extends Fragment implements MovieActivity.FabCallback, MovieActivity.BgCallback, GridActivity.TabFabCallback {

    private static String TAG = MovieFragment.class.getSimpleName();

    private static final int TRAILER_THUMB_WIDTH = 320;
    private static final int TRAILER_THUMB_HEIGHT = 180;    // youtube thumbnail dimensions loaded by mq default

    //The fragment argument representing the movie ID that this fragment represents.
    public static final String ARG_MOVIE_ID = "movie_id";

    //The fragment argument representing the internet availability.
    public static final String ARG_INTERNET = "internet";

    private MovieItem currentMovie;
    private ScrollView parentScrollLayout;
    private LinearLayout showIfInternetLayout;
    private ImageView posterImage;
    private ReadMoreTextView overviewText;
    private TextView releaseDateText;
    private TextView voteText;
    private TextView yearText;
    private TextView titleText;
    private ProgressDialog pDialog;
    private String urlCurrentMovie; //to fetch reviews and trailers
    private long movie_id;
    private ArrayList<MovieItem.Review> reviewArrayList;
    private ArrayList<String> trailerArray;
    private boolean internetAvailable;
    private Typeface notoRegular;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_MOVIE_ID)) {
            //get the movie clicked
            currentMovie = getArguments().getParcelable(ARG_MOVIE_ID);
            if (currentMovie != null) {
                movie_id = currentMovie.getMovieId();
            }
            //check whether internet available
            internetAvailable = getArguments().getBoolean(ARG_INTERNET, false);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_movie, container, false);
        notoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/NotoSans-Regular.ttf");
        Typeface notoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/NotoSans-Bold.ttf");

        parentScrollLayout = (ScrollView) rootView.findViewById(R.id.parentScrollLayout);
        parentScrollLayout.setVisibility(View.INVISIBLE);

        showIfInternetLayout = (LinearLayout) rootView.findViewById(R.id.showIfInternetLayout);
        showIfInternetLayout.setVisibility(View.GONE);

        urlCurrentMovie = "https://api.themoviedb.org/3/movie/" + movie_id + "?api_key=" + TMDB_API_KEY
                + "&append_to_response=reviews,trailers";

        reviewArrayList = new ArrayList<>();
        trailerArray = new ArrayList<>();

        //initialize progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.progress_dialog_message));
        pDialog.setCancelable(false);

        //finding views by ID
        posterImage = (ImageView) rootView.findViewById(R.id.posterImage);
        overviewText = (ReadMoreTextView) rootView.findViewById(R.id.overviewText);
        releaseDateText = (TextView) rootView.findViewById(R.id.releaseDateText);
        voteText = (TextView) rootView.findViewById(R.id.voteText);
        yearText = (TextView) rootView.findViewById(R.id.yearText);
        titleText = (TextView) rootView.findViewById(R.id.titleText);
        TextView plotSynopsisTV = (TextView) rootView.findViewById(R.id.string_plot_synopsis);
        TextView reviewsTV = (TextView) rootView.findViewById(R.id.string_reviews);
        TextView trailersTV = (TextView) rootView.findViewById(R.id.string_trailers);

        //setting font
        overviewText.setTypeface(notoRegular);
        releaseDateText.setTypeface(notoRegular);
        voteText.setTypeface(notoRegular);
        yearText.setTypeface(notoRegular);
        titleText.setTypeface(notoBold);
        plotSynopsisTV.setTypeface(notoBold);
        reviewsTV.setTypeface(notoBold);
        trailersTV.setTypeface(notoBold);

        if (internetAvailable) {
            makeJsonObjectRequest();    // fetch reviews and trailers
        } else {
            updateUI();     // display only the data stored
        }

        return rootView;
    }


    //Method to make json object request
    private void makeJsonObjectRequest() {

        showPDialog();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlCurrentMovie, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                //get reviews
                try {
                    JSONObject reviews = response.getJSONObject("reviews");
                    JSONArray results = reviews.getJSONArray("results");
                    try {
                        // Parsing json array response
                        // loop through each json object
                        for (int i = 0; i < results.length(); i++) {

                            JSONObject single_review = (JSONObject) results.get(i);

                            String author = single_review.getString("author");
                            String content = single_review.getString("content");
                            String review_url = single_review.getString("url");

                            MovieItem.Review review = new MovieItem.Review(author, content, review_url);
                            reviewArrayList.add(review);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                //get trailers
                try {
                    JSONObject trailers = response.getJSONObject("trailers");
                    JSONArray youtube = trailers.getJSONArray("youtube");
                    try {
                        // Parsing json array response
                        // loop through each json object
                        for (int i = 0; i < youtube.length(); i++) {

                            JSONObject single_trailer = (JSONObject) youtube.get(i);
                            String source = single_trailer.getString("source");
                            trailerArray.add(source);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                if (isAdded())
                    updateUI();
                hidePDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                hidePDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showPDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidePDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void updateUI() {
        //make UI visible
        parentScrollLayout.setVisibility(View.VISIBLE);

        //setting the poster image
        String posterPath = currentMovie.getPosterPath();
        posterPath = posterPath.startsWith("\\") ? posterPath.substring(1) : posterPath;
        String fullImageURL = "https://image.tmdb.org/t/p/w185" + posterPath;
        Picasso.with(getActivity())
                .load(fullImageURL)
                .error(R.drawable.null_movie)
                .placeholder(R.drawable.progress_animation)
                .into(posterImage);

        //getting movie the release date
        String initialDate = currentMovie.getReleaseDate();
        SimpleDateFormat initialFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date releaseDate = null;
        try {
            releaseDate = initialFormat.parse(initialDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //formatting the date to desired format
        SimpleDateFormat finalFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String finalDate = finalFormat.format(releaseDate);    //formatted date
        SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String year = getYearFormat.format(releaseDate);

        //checking if plot synopsis available
        String plot = currentMovie.getOverview();
        if (plot == null || plot.equals("")) {
            plot = getString(R.string.no_plot_message);
        }

        //applying to the views
        titleText.setText(currentMovie.getTitle());
        yearText.setText(year);
        overviewText.setText(plot);
        String releaseText = "<b>" + getString(R.string.movie_release_date) + "</b> " + finalDate;
        releaseDateText.setText(Html.fromHtml(releaseText));
        String vote = currentMovie.getVoteAverage() + getString(R.string.movie_rating_out_off_text);
        voteText.setText(vote);

        if (internetAvailable) {
            updateExtra();
        }

    }

    //load trailers and reviews - called only if internet available
    private void updateExtra() {
        //make trailers and review layout visible
        showIfInternetLayout.setVisibility(View.VISIBLE);

        //obtaining parent layout
        LinearLayout parentReviewLayout = (LinearLayout) getActivity().findViewById(R.id.reviewsListLayout);
        //layout params for the review text
        LinearLayout.LayoutParams reviewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        reviewLayoutParams.setMargins(5, 5, 5, 20);

        //building the reviews linear layout
        for (MovieItem.Review review : reviewArrayList) {
            String reviewString = "<b>" + getString(R.string.movie_review_by) + " " + review.getAuthor() + "</b><br><br>" + review.getContent() + "<br><br>";

            ReadMoreTextView singleReview = new ReadMoreTextView(getContext());
            singleReview.setTypeface(notoRegular);
            singleReview.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            singleReview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            singleReview.setText(Html.fromHtml(reviewString));
            singleReview.setTrimCollapsedText(getString(R.string.read_more_collapsed_text));
            singleReview.setTrimExpandedText(getString(R.string.read_more_expanded_text));
            singleReview.setTrimMode(0);    //trim mode - length = 1, line = 0
            singleReview.setTrimLines(5);
            singleReview.setColorClickableText(ContextCompat.getColor(getContext(), R.color.colorAccent));

            parentReviewLayout.addView(singleReview, reviewLayoutParams);
        }

        // if no review available
        if (reviewArrayList.isEmpty()) {
            String reviewString = getString(R.string.no_reviews_message);

            TextView noReview = new TextView(getContext());
            noReview.setTypeface(notoRegular);
            noReview.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            noReview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            noReview.setText(reviewString);

            parentReviewLayout.addView(noReview, reviewLayoutParams);
        }

        //obtaining parent layout
        LinearLayout parentTrailerLayout = (LinearLayout) getActivity().findViewById(R.id.trailersListLayout);
        //layout params for the ImageButton
        LinearLayout.LayoutParams viewsLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //building the trailer linear layout
        for (final String trailerID : trailerArray) {

            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            final ImageView trailer = new ImageView(getContext());
            final ImageView overLay = new ImageView(getContext());
            overLay.setAdjustViewBounds(true);
            overLay.setBackgroundResource(R.drawable.button_play);
            overLay.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // set minimum width and height of the views and layouts according to the mq default thumbnail dimensions
            relLayoutParams.width = TRAILER_THUMB_WIDTH;
            relLayoutParams.height = TRAILER_THUMB_HEIGHT;
            trailer.setMinimumWidth(TRAILER_THUMB_WIDTH);
            trailer.setMinimumHeight(TRAILER_THUMB_HEIGHT);
            overLay.setMinimumWidth(TRAILER_THUMB_WIDTH);
            overLay.setMinimumHeight(TRAILER_THUMB_HEIGHT);

            final String trailerURL = "https://www.youtube.com/watch?v=" + trailerID;
            String trailerThumb = "https://img.youtube.com/vi/" + trailerID + "/mqdefault.jpg";

            trailer.setContentDescription(getString(R.string.movie_trailer_content_desc_1) + trailerArray.indexOf(trailerID)
                    + getString(R.string.movie_trailer_content_desc_2) + currentMovie.getTitle());

            Target loadTarget = new Target() {
                @Override
                public void onBitmapFailed(Drawable arg0) {
                    trailer.setBackground(arg0);
                }

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    trailer.setBackground(drawable);
                }

                @Override
                public void onPrepareLoad(Drawable arg0) {
                    trailer.setBackground(arg0);
                }
            };

            try {
                Picasso picasso = Picasso.with(getContext());
                RequestCreator requestCreator = picasso.load(trailerThumb);
                requestCreator.placeholder(R.drawable.progress_animation);
                requestCreator.error(R.drawable.null_movie);
                requestCreator.into(loadTarget);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            trailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setType("video/*");
                    intent.setData(Uri.parse(trailerURL));
                    startActivity(Intent.createChooser(intent, getString(R.string.trailer_play_intent_chooser_message)));
                }
            });

            relativeLayout.addView(trailer, relLayoutParams);
            relativeLayout.addView(overLay, relLayoutParams);
            parentTrailerLayout.addView(relativeLayout, viewsLayoutParams);
        }

        //if no trailers available
        if (trailerArray.isEmpty()) {
            TextView noTrailer = new TextView(getActivity());
            noTrailer.setTypeface(notoRegular);
            noTrailer.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            noTrailer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            noTrailer.setText(getString(R.string.no_trailer_message));
            int dimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.activity_horizontal_margin), getResources().getDisplayMetrics());
            noTrailer.setPadding(dimen, 0, dimen, 0);

            parentTrailerLayout.addView(noTrailer, viewsLayoutParams);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    // share feature
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            if (internetAvailable) {
                if (trailerArray.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.share_action_no_trailer_toast_message, Toast.LENGTH_SHORT).show();
                } else {
                    String firstTrailer = "https://www.youtube.com/watch?v=" + trailerArray.get(0);
                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
                    share.setType("text/plain");

                    // Add data to the intent, the receiving app will decide
                    // what to do with it.

                    share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_action_extra_text)
                            + currentMovie.getTitle() + "\n" + firstTrailer);

                    startActivity(Intent.createChooser(share, getString(R.string.share_action_intent_chooser)));
                }
            } else {
                Toast.makeText(getActivity(), R.string.no_internet_toast_message, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFabClicked(final View favFab) {
        //setting on click listener on the add to favourite button
        favFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAdded()) {     // fragment is added
                    String curMovieID = String.valueOf(currentMovie.getMovieId());
                    SharedPreferences mPrefs = getActivity().getSharedPreferences("FAVOURITE_MOVIES", Context.MODE_APPEND);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();

                    String restoredText = mPrefs.getString(curMovieID, null);
                    if (restoredText != null) {     // movie exists in db
                        prefsEditor.remove(curMovieID);
                    }
                    // add movie to db if doesn't exist in db, update if already exists
                    Gson gson = new Gson();
                    String json = gson.toJson(currentMovie);
                    prefsEditor.putString(curMovieID, json);
                    prefsEditor.apply();

                    Log.d(TAG, "FAB clicked");
                    Toast.makeText(getActivity(), R.string.marked_fav_toast_message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPaletteLoaded(Palette palette) {
        //set the background color
        parentScrollLayout.setBackgroundColor(palette.getDarkMutedColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight)));
    }
}