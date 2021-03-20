package it.uniba.di.sms2021.managerapp.enitities.notifications;

import java.util.Objects;

public class NewReplyReportNotice {
    private String replyId;
    private String reportId;
    private String replySenderId;
    private String groupId;
    private Long sentTime;

    public NewReplyReportNotice() {
    }

    public NewReplyReportNotice(String replyId, String replySenderId, String reportId, String groupId) {
        this.replyId = replyId;
        this.reportId = reportId;
        this.replySenderId = replySenderId;
        this.groupId = groupId;
        this.sentTime = System.currentTimeMillis();
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getReplySenderId() {
        return replySenderId;
    }

    public void setReplySenderId(String replySenderId) {
        this.replySenderId = replySenderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
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
        NewReplyReportNotice that = (NewReplyReportNotice) o;
        return Objects.equals(replyId, that.replyId) &&
                Objects.equals(reportId, that.reportId) &&
                Objects.equals(replySenderId, that.replySenderId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(sentTime, that.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replyId, reportId, replySenderId, groupId, sentTime);
    }

    @Override
    public String toString() {
        return "NewReplyReportNotice{" +
                "replyId='" + replyId + '\'' +
                ", reportId='" + reportId + '\'' +
                ", replySenderId='" + replySenderId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
