package it.uniba.di.sms2021.managerapp.enitities;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Evaluation implements Parcelable {
    private float vote;
    private String comment;

    public Evaluation() {

    }

    public Evaluation(float vote, String comment) {
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
        Evaluation evaluation1 = (Evaluation) o;
        return Float.compare(evaluation1.vote, vote) == 0 &&
                Objects.equals(comment, evaluation1.comment);
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

    public static final Parcelable.Creator<Evaluation> CREATOR
            = new Parcelable.Creator<Evaluation>() {
        public Evaluation createFromParcel(Parcel in) {
            Evaluation evaluation = new Evaluation();
            evaluation.setVote(in.readFloat());
            evaluation.setComment(in.readString());

            return evaluation;
        }

        public Evaluation[] newArray(int size) {
            return new Evaluation[size];
        }
    };
}