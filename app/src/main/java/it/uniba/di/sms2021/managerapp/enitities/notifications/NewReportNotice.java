package it.uniba.di.sms2021.managerapp.enitities.notifications;

import java.util.Objects;

public class NewReportNotice {
    private String reportId;
    private String reportSenderId;
    private String groupId;
    private Long sentTime;

    public NewReportNotice() {
    }

    public NewReportNotice(String reportId, String reportSenderId, String groupId) {
        this.reportId = reportId;
        this.reportSenderId = reportSenderId;
        this.groupId = groupId;
        this.sentTime = System.currentTimeMillis();
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportSenderId() {
        return reportSenderId;
    }

    public void setReportSenderId(String reportSenderId) {
        this.reportSenderId = reportSenderId;
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
        return Objects.equals(reportId, that.reportId) &&
                Objects.equals(reportSenderId, that.reportSenderId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(sentTime, that.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, reportSenderId, groupId, sentTime);
    }

    @Override
    public String toString() {
        return "NewReportNotice{" +
                "reportId='" + reportId + '\'' +
                ", reportSenderId='" + reportSenderId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
