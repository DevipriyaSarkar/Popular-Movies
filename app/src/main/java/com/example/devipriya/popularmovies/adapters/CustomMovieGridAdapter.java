package com.example.devipriya.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.devipriya.popularmovies.R;
import com.example.devipriya.popularmovies.models.MovieItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Devipriya on 07-02-2016.
 */
public class CustomMovieGridAdapter extends RecyclerView.Adapter<CustomMovieGridAdapter.CustomViewHolder> {

    private Context context;
    private ArrayList<MovieItem> movieArrayList;
    private static MyClickListener myClickListener;

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView moviePoster;
        TextView movieRating;

        public CustomViewHolder(View view) {
            super(view);
            this.moviePoster = (ImageView) view.findViewById(R.id.moviePoster);
            this.movieRating = (TextView) view.findViewById(R.id.movieRating);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            myClickListener.onItemClick(getAdapterPosition(), view);
        }

    }

    public CustomMovieGridAdapter(Context context, ArrayList<MovieItem> movieArrayList) {
        this.context = context;
        this.movieArrayList = movieArrayList;
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        CustomMovieGridAdapter.myClickListener = myClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_movie_item, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int position) {
        // Populate the data into the template view using the data object
        String posterPath = movieArrayList.get(position).getPosterPath();
        posterPath = posterPath.startsWith("\\") ? posterPath.substring(1) : posterPath;
        String fullImageURL = "https://image.tmdb.org/t/p/w185" + posterPath;
        Picasso.with(context)
                .load(fullImageURL)
                .error(R.drawable.null_movie)
                .placeholder(R.drawable.progress_animation)
                .into(customViewHolder.moviePoster);
        String rating = movieArrayList.get(position).getVoteAverage() + context.getString(R.string.movie_rating_out_off_text);
        customViewHolder.movieRating.setText(rating);
    }

    @Override
    public int getItemCount() {
        return (movieArrayList != null ? movieArrayList.size() : 0);
    }

    public void addItem(MovieItem item, int index) {
        movieArrayList.add(index, item);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        movieArrayList.remove(index);
        notifyItemRemoved(index);
    }

    public MovieItem getItem(int index) {
        return movieArrayList.get(index);
    }

    public interface MyClickListener {
        void onItemClick(int position, View view);
    }

}
