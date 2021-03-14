package it.uniba.di.sms2021.managerapp.enitities.notifications;

import java.util.Objects;

public class NewReportNotice {
    private String ReportId;
    private String ReportSenderId;
    private String groupId;
    private Long sentTime;

    public NewReportNotice() {
    }

    public NewReportNotice(String reportId, String reportSenderId, String groupId) {
        ReportId = reportId;
        ReportSenderId = reportSenderId;
        this.groupId = groupId;
        this.sentTime = System.currentTimeMillis();
    }

    public String getReportId() {
        return ReportId;
    }

    public void setReportId(String reportId) {
        ReportId = reportId;
    }

    public String getReportSenderId() {
        return ReportSenderId;
    }

    public void setReportSenderId(String reportSenderId) {
        ReportSenderId = reportSenderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getSentTime() {
        return sentTime;
    }

    public void setSentTime(Long sentTime) {
        this.sentTime = sentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewReportNotice that = (NewReportNotice) o;
        return Objects.equals(ReportId, that.ReportId) &&
                Objects.equals(ReportSenderId, that.ReportSenderId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(sentTime, that.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ReportId, ReportSenderId, groupId, sentTime);
    }

    @Override
    public String toString() {
        return "NewReportNotice{" +
                "ReportId='" + ReportId + '\'' +
                ", ReportSenderId='" + ReportSenderId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
