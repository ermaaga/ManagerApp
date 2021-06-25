package it.uniba.di.sms2021.managerapp.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReplyReportNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.projects.ProjectReportsActivity;


public class ReplyNotification implements Notifiable {
    private static final String TAG = "ReplyNotification";

    private NewReplyReportNotice reply;
    private User sender;
    private Group group;
    private Report report;
    private Date sentTime;

    public ReplyNotification(NewReplyReportNotice reply) {
        this.reply = reply;
    }

    public String getReplyId() {
        return reply.getReplyId();
    }

    public void setReplyId(String requestId) {
        reply.setReplyId(requestId);
    }

    public String getReplySenderId() {
        return reply.getReplySenderId();
    }

    public void setReplySenderId(String requestSenderId) {
        reply.setReplySenderId(requestSenderId);
    }

    public String getGroupId() {
        return reply.getGroupId();
    }

    public void setGroupId(String groupId) {
        reply.setGroupId(groupId);
    }

    public Long getSentTime() {
        return reply.getSentTime();
    }

    public void setSentTime(Long sentTime) {
        reply.setSentTime(sentTime);
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

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplyNotification that = (ReplyNotification) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(group, that.group) &&
                Objects.equals(report, that.report);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, group, report);
    }

    public boolean isInitialised () {
        return sender != null && group != null && report!= null;
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
        return context.getString(R.string.text_notification_title_reply);
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Exclude
    @Override
    public String getNotificationMessage(Context context) {
        String ellipsis;
        if(report.getComment().length()>30){
            ellipsis="...";
        }else{
            ellipsis="";
        }
        return context.getString(R.string.text_notification_message_reply,
                    sender.getFullName(), report.getComment().substring(0,29), ellipsis, group.getName());
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public void onNotificationClick(Context context, @Nullable OnActionDoneListener listener) {
        new Project.Initialiser() {
            @Override
            public void onProjectInitialised(Project project) {
                String uid=LoginHelper.getCurrentUser().getAccountId();

                FirebaseDbHelper.getNewReplyReportReference(uid).child(reply.getReplyId()).removeValue();

                Intent intent = new Intent(context, ProjectReportsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Project.KEY, project);
                intent.putExtra(Report.KEY, report);
                boolean needReeply = true;
                intent.putExtra(Reply.KEY, needReeply);
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

        FirebaseDbHelper.getNewReplyReportReference(uid).child(reply.getReplyId()).removeValue();

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
        private ReplyNotification notification;

        /**
         * Definire cosa fare una volta che la notifica è stata inizializzata
         */
        public abstract void onNotificationInitialised (ReplyNotification notification);

        /**
         * Initializza la notifica con tutti i campi necessari a partire dalla richiesta
         * @param reply la risposta da cui creare la notifica
         */
        public void initialiseNotification (NewReplyReportNotice reply) {
            notification = new ReplyNotification(reply);

            notification.sentTime = new Date(reply.getSentTime());


            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS)
                    .child(reply.getReplySenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.sender = snapshot.getValue(User.class);
                            if (notification.sender == null) {
                                Log.e(TAG, "Impossibile trovare l'utente con l'id "
                                        + reply.getReplySenderId() +
                                        " nella nuova risposta di id " +
                                        reply.getReplyId());
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
                    .child(reply.getGroupId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.group = snapshot.getValue(Group.class);
                            if (notification.group == null) {
                                Log.e(TAG, "Impossibile trovare il gruppo con l'id "
                                        + reply.getGroupId() +
                                        " nella nuova risposta di id " +
                                        reply.getReplyId());
                                return;
                            }

                            if (notification.isInitialised()) {
                                onNotificationInitialised(notification);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REPORTS)
                    .child("reportsList")
                    .child(reply.getReportId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.report = snapshot.getValue(Report.class);
                            if (notification.report == null) {
                                Log.e(TAG, "Impossibile trovare la segnalazione con l'id "
                                        + reply.getReportId() +
                                        " nella nuova risposta di id " +
                                        reply.getReplyId());
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
