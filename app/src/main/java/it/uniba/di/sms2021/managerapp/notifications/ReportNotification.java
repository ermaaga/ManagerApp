package it.uniba.di.sms2021.managerapp.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReportNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.projects.ProjectReportsActivity;


public class ReportNotification  implements Notifiable {
    private static final String TAG = "ReportNotification";
    private NewReportNotice report;
    private User sender;
    private Group group;
    private Date sentTime;

    public ReportNotification(NewReportNotice report) {
        this.report = report;
    }

    public String getReportId() {
        return report.getReportId();
    }

    public void setReportId(String requestId) {
        report.setReportId(requestId);
    }

    public String getReportSenderId() {
        return report.getReportSenderId();
    }

    public void setReportSenderId(String requestSenderId) {
        report.setReportSenderId(requestSenderId);
    }

    public String getGroupId() {
        return report.getGroupId();
    }

    public void setGroupId(String groupId) {
        report.setGroupId(groupId);
    }

    public Long getSentTime() {
        return report.getSentTime();
    }

    public void setSentTime(Long sentTime) {
        report.setSentTime(sentTime);
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportNotification that = (ReportNotification) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, group);
    }

    public boolean isInitialised () {
        return sender != null && group != null;
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public Date getNotificationSentTime() {
        return sentTime;
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Exclude
    @Override
    public String getNotificationTitle(Context context) {
        return context.getString(R.string.text_notification_title_report);
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Exclude
    @Override
    public String getNotificationMessage(Context context) {
        return context.getString(R.string.text_notification_message_report,
                    sender.getFullName(), group.getName());
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public void onNotificationClick(Context context, @Nullable OnActionDoneListener listener) {
        new Project.Initialiser() {
            @Override
            public void onProjectInitialised(Project project) {
                String uid=LoginHelper.getCurrentUser().getAccountId();

                FirebaseDbHelper.getNewReportReference(uid).child(report.getReportId()).removeValue();

                Intent intent = new Intent(context, ProjectReportsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Project.KEY, project);
                context.startActivity(intent);
            }
        }.initialiseProject(group);
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public String getAction1Label(Context context) {
        return context.getString(R.string.text_notification_action_dismiss);
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public void onNotificationAction1Click(Context context, @Nullable OnActionDoneListener listener) {
        String uid=LoginHelper.getCurrentUser().getAccountId();

        FirebaseDbHelper.getNewReportReference(uid).child(report.getReportId()).removeValue();

        if (listener != null) {
            listener.onActionDone();
        }
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public String getAction2Label(Context context) {
        return null;
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public void onNotificationAction2Click(Context context, @Nullable OnActionDoneListener listener) {

    }

    public static abstract class Initialiser {
        private ReportNotification notification;

        /**
         * Definire cosa fare una volta che la notifica è stata inizializzata
         */
        public abstract void onNotificationInitialised (ReportNotification notification);

        /**
         * Initializza la notifica con tutti i campi necessari a partire dalla richiesta
         * @param report la segnalazione da cui creare la notifica
         */
        public void initialiseNotification (NewReportNotice report) {
            notification = new ReportNotification(report);

            notification.sentTime = new Date(report.getSentTime());


            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS)
                    .child(report.getReportSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.sender = snapshot.getValue(User.class);
                            if (notification.sender == null) {
                                throw new RuntimeException("Impossibile trovare l'utente con l'id "
                                        + report.getReportSenderId() +
                                        " nella nuova segnalazione di id " +
                                        report.getReportId());
                            }

                            if (notification.isInitialised()) {
                                onNotificationInitialised(notification);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                    .child(report.getGroupId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.group = snapshot.getValue(Group.class);
                            if (notification.group == null) {
                                throw new RuntimeException("Impossibile trovare il gruppo con l'id "
                                        + report.getGroupId() +
                                        " nella nuova segnalazione di id " +
                                        report.getReportId());
                            }

                            if (notification.isInitialised()) {
                                onNotificationInitialised(notification);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}
