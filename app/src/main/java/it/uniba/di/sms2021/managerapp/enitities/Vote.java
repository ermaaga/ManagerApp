package it.uniba.di.sms2021.managerapp.enitities;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vote implements Parcelable {
    private float vote;
    private String comment;

    public Vote() {

    }

    public Vote(float vote, String comment) {
        this.vote = vote;
        this.comment = comment;
    }

    public float getVote() {
        return vote;
    }

    public void setVote(float vote) {
        this.vote = vote;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote1 = (Vote) o;
        return Float.compare(vote1.vote, vote) == 0 &&
                Objects.equals(comment, vote1.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vote, comment);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(vote);
        dest.writeString(comment);
    }

    public static final Parcelable.Creator<Vote> CREATOR
            = new Parcelable.Creator<Vote>() {
        public Vote createFromParcel(Parcel in) {
            Vote vote = new Vote();
            vote.setVote(in.readFloat());
            vote.setComment(in.readString());

            return vote;
        }

        public Vote[] newArray(int size) {
            return new Vote[size];
        }
    };
}