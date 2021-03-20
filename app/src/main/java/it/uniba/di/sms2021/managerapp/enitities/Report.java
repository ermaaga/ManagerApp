package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Report implements Parcelable {
    private String reportId;
    private String userId;
    private String date;
    String groupId;
    private String comment;
    public static final String KEY = "report";

    public Report() {
    }

    public Report(String reportId, String userId, String date, String groupId, String comment) {
        this.reportId = reportId;
        this.userId = userId;
        this.date = date;
        this.groupId = groupId;
        this.comment = comment;
    }

    protected Report(Parcel in) {
        reportId = in.readString();
        userId = in.readString();
        date = in.readString();
        groupId = in.readString();
        comment = in.readString();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reviewId) {
        this.reportId = reviewId;
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
        return Objects.equals(reportId, report.reportId) &&
                Objects.equals(userId, report.userId) &&
                Objects.equals(date, report.date) &&
                Objects.equals(groupId, report.groupId) &&
                Objects.equals(comment, report.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, userId, date, groupId, comment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reportId);
        dest.writeString(userId);
        dest.writeString(date);
        dest.writeString(groupId);
        dest.writeString(comment);
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };
}
