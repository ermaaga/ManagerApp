package it.uniba.di.sms2021.managerapp.enitities;

import java.util.Objects;

public class Vote {
    private float vote;
    private String comment;

    public Vote() {

    }

    public Vote(float vote, String comment) {
        this.vote = vote;
        this.comment = comment;
    }

    public float getRate() {
        return vote;
    }

    public void setRate(float vote) {
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
}
