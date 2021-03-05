package it.uniba.di.sms2021.managerapp.enitities;

import java.util.Objects;

public class Reply {

    private String replyId;
    private String userId;
    private String date;
    private String comment;
    private String originId;

    public Reply() {
    }

    public Reply(String replyId, String userId, String date, String comment, String originId) {
        this.replyId = replyId;
        this.userId = userId;
        this.date = date;
        this.comment = comment;
        this.originId = originId;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reply that = (Reply) o;
        return Objects.equals(replyId, that.replyId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(originId, that.originId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replyId, userId, date, comment, originId);
    }

    @Override
    public String toString() {
        return "Reply{" +
                "replyId='" + replyId + '\'' +
                ", userId='" + userId + '\'' +
                ", date='" + date + '\'' +
                ", comment='" + comment + '\'' +
                ", originId='" + originId + '\'' +
                '}';
    }
}
