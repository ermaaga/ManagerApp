package it.uniba.di.sms2021.managerapp.enitities;

import java.util.Objects;

public class Report {
    private String reviewId;
    private String userId;
    private String date;
    String groupId;
    private String comment;

    public Report(String reviewId, String userId, String date, String groupId, String comment) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.date = date;
        this.groupId = groupId;
        this.comment = comment;
    }

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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
        Report report = (Report) o;
        return Objects.equals(reviewId, report.reviewId) &&
                Objects.equals(userId, report.userId) &&
                Objects.equals(date, report.date) &&
                Objects.equals(groupId, report.groupId) &&
                Objects.equals(comment, report.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, userId, date, groupId, comment);
    }
}
