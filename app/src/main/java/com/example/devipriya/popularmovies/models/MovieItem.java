package com.example.devipriya.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Devipriya on 07-02-2016.
 */
public class MovieItem implements Parcelable {
    private String poster_path;
    private String backdrop_path;
    private String title;
    private String overview;
    private String release_date;
    private int isFavorite; // 0 - not fav, 1 - fav
    private double vote_average;
    private long movie_id;

    private String[] trailer_path;
    private Review review;

    public MovieItem() {
    }

    public MovieItem(String poster_path, String backdrop_path, String title, String overview, String release_date, double vote_average, long movie_id) {
        this.poster_path = poster_path;
        this.backdrop_path = backdrop_path;
        this.title = title;
        this.overview = overview;
        this.release_date = release_date;
        this.vote_average = vote_average;
        this.movie_id = movie_id;
        this.isFavorite = 0;
    }

    public MovieItem(String[] trailer_path, Review review) {
        this.trailer_path = trailer_path;
        this.review = review;
    }

    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }

    public void setFavorite(int favorite) {
        isFavorite = favorite;
    }

    public void setVoteAverage(double vote_average) {
        this.vote_average = vote_average;
    }

    public void setTrailerPath(String[] trailer_path) {
        this.trailer_path = trailer_path;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public void setMovieId(long movie_id) {
        this.movie_id = movie_id;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public int isFavorite() {
        return isFavorite;
    }

    public double getVoteAverage() {
        return vote_average;
    }

    public String[] getTrailerPath() {
        return trailer_path;
    }

    public Review getReview() {
        return review;
    }

    public long getMovieId() {
        return movie_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(poster_path);
        dest.writeString(backdrop_path);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(release_date);
        dest.writeInt(isFavorite);
        dest.writeDouble(vote_average);

        dest.writeStringArray(trailer_path);
        dest.writeLong(movie_id);
        // Add inner class
        dest.writeParcelable(review, flags);
    }

    // Creator
    public static final Parcelable.Creator<MovieItem> CREATOR
            = new Parcelable.Creator<MovieItem>() {
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    //De-parcel object
    public MovieItem(Parcel in) {
        this.poster_path = in.readString();
        this.backdrop_path = in.readString();
        this.title = in.readString();
        this.overview = in.readString();
        this.release_date = in.readString();
        this.isFavorite = in.readInt();
        this.vote_average = in.readDouble();

        this.trailer_path = in.createStringArray();
        this.movie_id = in.readLong();
        this.review = in.readParcelable(Review.class.getClassLoader());
    }

    //Inner class
    public static class Review implements Parcelable {
        String author;
        String content;
        String review_url;

        public Review() {
        }

        public Review(String author, String content, String review_url) {
            this.author = author;
            this.content = content;
            this.review_url = review_url;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setReview_url(String review_url) {
            this.review_url = review_url;
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }

        public String getReview_url() {
            return review_url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(author);
            dest.writeString(content);
            dest.writeString(review_url);
        }

        // Creator
        public static final Parcelable.Creator<Review> CREATOR
                = new Parcelable.Creator<Review>() {
            public Review createFromParcel(Parcel in) {
                return new Review(in);
            }

            public Review[] newArray(int size) {
                return new Review[size];
            }
        };

        //De-parcel object
        public Review(Parcel in) {
            this.author = in.readString();
            this.content = in.readString();
            this.review_url = in.readString();
        }

    }

}
