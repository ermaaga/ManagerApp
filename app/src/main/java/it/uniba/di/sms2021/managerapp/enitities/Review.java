package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.Objects;

public class Review implements Parcelable {

    private String reviewId;
    private String userId;
    private String date;
    private int rating;
    String groupId;
    @Nullable
    private String comment;

    public Review() {
    }

    public Review(String reviewId, String userId, String date, int rating, String groupId, @Nullable String comment) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.date = date;
        this.rating = rating;
        this.groupId = groupId;
        this.comment = comment;
    }

    protected Review(Parcel in) {
        reviewId = in.readString();
        userId = in.readString();
        date = in.readString();
        rating = in.readInt();
        groupId = in.readString();
        comment = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return rating == review.rating &&
                Objects.equals(reviewId, review.reviewId) &&
                Objects.equals(userId, review.userId) &&
                Objects.equals(date, review.date) &&
                Objects.equals(groupId, review.groupId) &&
                Objects.equals(comment, review.comment);
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId='" + reviewId + '\'' +
                ", userId='" + userId + '\'' +
                ", date='" + date + '\'' +
                ", rating=" + rating +
                ", groupId='" + groupId + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, userId, date, rating, groupId, comment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reviewId);
        dest.writeString(userId);
        dest.writeString(date);
        dest.writeInt(rating);
        dest.writeString(groupId);
        dest.writeString(comment);
    }
}
