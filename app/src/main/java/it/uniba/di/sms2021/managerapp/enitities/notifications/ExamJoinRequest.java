package it.uniba.di.sms2021.managerapp.enitities.notifications;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;
import it.uniba.di.sms2021.managerapp.notifications.OnActionDoneListener;

public class ExamJoinRequest implements Notifiable {
    private String requestId;
    private String requestSenderId;
    private String requestSenderFullName;
    private Exam exam;
    private Long sentTime;

    public ExamJoinRequest() {
    }

    public ExamJoinRequest(String requestId, String requestSenderId, String requestSenderFullName, Exam exam, Long sentTime) {
        this.requestId = requestId;
        this.requestSenderId = requestSenderId;
        this.requestSenderFullName = requestSenderFullName;
        this.exam = exam;
        this.sentTime = sentTime;
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

    public String getRequestSenderFullName() {
        return requestSenderFullName;
    }

    public void setRequestSenderFullName(String requestSenderFullName) {
        this.requestSenderFullName = requestSenderFullName;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
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
        ExamJoinRequest request = (ExamJoinRequest) o;
        return Objects.equals(requestId, request.requestId) &&
                Objects.equals(requestSenderId, request.requestSenderId) &&
                Objects.equals(requestSenderFullName, request.requestSenderFullName) &&
                Objects.equals(exam, request.exam) &&
                Objects.equals(sentTime, request.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, requestSenderId, requestSenderFullName, exam, sentTime);
    }

    @Override
    public String toString() {
        return "ExamJoinRequest{" +
                "requestId='" + requestId + '\'' +
                ", requestSenderId='" + requestSenderId + '\'' +
                ", requestSenderFullName='" + requestSenderFullName + '\'' +
                ", exam=" + exam +
                ", sentTime=" + sentTime +
                '}';
    }

    @Override
    @Exclude
    public Date getNotificationSentTime() {
        return new Date(sentTime);
    }

    @Override
    public String getNotificationTitle(Context context) {
        return context.getString(R.string.text_notification_title_exam_join_request);
    }

    @Override
    public String getNotificationMessage(Context context) {
        return context.getString(R.string.text_notification_message_exam_join_request,
                requestSenderFullName, exam.getName());
    }

    @Override
    public void onNotificationClick(Context context, @Nullable OnActionDoneListener listener) {
        //TODO mostra profilo utente
        Toast.makeText(context, R.string.text_message_not_yet_implemented, Toast.LENGTH_LONG).show();

        if (listener != null) {
            listener.onActionDone();
        }
    }

    @Override
    public String getAction1Label(Context context) {
        return context.getString(R.string.text_button_request_accept);
    }

    @Override
    public void onNotificationAction1Click(Context context, @Nullable OnActionDoneListener listener) {
        List<String> students = exam.getStudents();
        if (students == null) {
            students = new ArrayList<>();
        }
        students.add(requestSenderId);

        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS)
                .child(exam.getId()).child(Exam.Keys.STUDENTS).setValue(students);

        removeNotification();

        if (listener != null) {
            listener.onActionDone();
        }
    }

    @Override
    public String getAction2Label(Context context) {
        return context.getString(R.string.text_button_request_refuse);
    }

    @Override
    public void onNotificationAction2Click(Context context, @Nullable OnActionDoneListener listener) {
        removeNotification();

        if (listener != null) {
            listener.onActionDone();
        }
    }

    /**
     * Rimuove la notifica da tutti i professori e dalle richieste in sospeso
     */
    public void removeNotification() {
        for (String professorId: exam.getProfessors()) {
            FirebaseDbHelper.getExamJoinRequestReference(professorId).child(requestId).removeValue();
        }

        FirebaseDbHelper.getPendingExamRequests(requestSenderId).child(exam.getId()).removeValue();
    }
}
