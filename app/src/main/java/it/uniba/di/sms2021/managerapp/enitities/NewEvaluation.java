package it.uniba.di.sms2021.managerapp.enitities;

import java.util.List;
import java.util.Objects;

public class NewEvaluation {

    private String evaluationId;
    private String evaluationSenderId;
    private String groupId;
    private Long sentTime;

    public NewEvaluation(){

    }

    public NewEvaluation(String evaluationId, String evaluationSenderId, String groupId) {
        this.evaluationId = evaluationId;
        this.evaluationSenderId = evaluationSenderId;
        this.groupId = groupId;
        this.sentTime = System.currentTimeMillis();
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getEvaluationSenderId() {
        return evaluationSenderId;
    }

    public void setEvaluationSenderId(String evaluationSenderId) {
        this.evaluationSenderId = evaluationSenderId;
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
        NewEvaluation that = (NewEvaluation) o;
        return Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(evaluationSenderId, that.evaluationSenderId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(sentTime, that.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationId, evaluationSenderId, groupId, sentTime);
    }

    @Override
    public String toString() {
        return "NewEvaluation{" +
                "evaluationId='" + evaluationId + '\'' +
                ", evaluationSenderId='" + evaluationSenderId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
