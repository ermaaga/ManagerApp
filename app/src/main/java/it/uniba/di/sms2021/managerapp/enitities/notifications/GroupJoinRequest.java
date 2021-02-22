package it.uniba.di.sms2021.managerapp.enitities.notifications;

import java.util.Objects;

public class GroupJoinRequest{
    private String requestId;
    private String requestSenderId;
    private String groupId;
    private String groupOwnerId;
    private Long sentTime;

    public GroupJoinRequest(String requestId, String requestSender, String groupId, String groupOwnerId) {
        this.requestId = requestId;
        this.requestSenderId = requestSender;
        this.groupId = groupId;
        this.groupOwnerId = groupOwnerId;
        sentTime = System.currentTimeMillis();
    }

    public GroupJoinRequest() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestSenderId() {
        return requestSenderId;
    }

    public void setRequestSenderId(String requestSenderId) {
        this.requestSenderId = requestSenderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
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
        GroupJoinRequest that = (GroupJoinRequest) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(requestSenderId, that.requestSenderId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(groupOwnerId, that.groupOwnerId) &&
                Objects.equals(sentTime, that.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, requestSenderId, groupId, groupOwnerId, sentTime);
    }

    @Override
    public String toString() {
        return "GroupJoinRequest{" +
                "requestId='" + requestId + '\'' +
                ", requestSender='" + requestSenderId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupOwnerId='" + groupOwnerId + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
