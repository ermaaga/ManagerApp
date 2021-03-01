package it.uniba.di.sms2021.managerapp.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.NewEvaluation;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.projects.ProjectDetailActivity;


public class EvaluationNotification  implements Notifiable {
    private static final String TAG = "EvaluationNotification";
    private NewEvaluation evaluation;
    private User sender;
    private Group group;
    private Date sentTime;

    public EvaluationNotification(NewEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public String getEvaluationId() {
        return evaluation.getEvaluationId();
    }

    public void setEvaluationId(String requestId) {
        evaluation.setEvaluationId(requestId);
    }

    public String getEvaluationSenderId() {
        return evaluation.getEvaluationSenderId();
    }

    public void setEvaluationSenderId(String requestSenderId) {
        evaluation.setEvaluationSenderId(requestSenderId);
    }

    public String getGroupId() {
        return evaluation.getGroupId();
    }

    public void setGroupId(String groupId) {
        evaluation.setGroupId(groupId);
    }

    public Long getSentTime() {
        return evaluation.getSentTime();
    }

    public void setSentTime(Long sentTime) {
        evaluation.setSentTime(sentTime);
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

    public Boolean isUpdate() {
       return evaluation.isUpdate();
    }

    public void setUpdate(Boolean update) {
        evaluation.setUpdate(update);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationNotification that = (EvaluationNotification) o;
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

        if (evaluation.isUpdate()) {
            return context.getString(R.string.text_notification_title_update_evaluation);
        } else {
            return context.getString(R.string.text_notification_title_new_evaluation);
        }
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Exclude
    @Override
    public String getNotificationMessage(Context context) {

        if (evaluation.isUpdate()) {
            return context.getString(R.string.text_notification_message_update_evaluation,
                    sender.getFullName(), group.getName());
        } else {
            return context.getString(R.string.text_notification_message_new_evaluation,
                    sender.getFullName(), group.getName());
        }
    }

    //metodo dell'interfaccia Notifiable da implementare
    @Override
    public void onNotificationClick(Context context, @Nullable OnActionDoneListener listener) {
        new Project.Initialiser() {
            @Override
            public void onProjectInitialised(Project project) {
                Intent intent = new Intent(context, ProjectDetailActivity.class);
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

       FirebaseDbHelper.getNewEvaluationReference(uid).child(evaluation.getEvaluationId()).removeValue();

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
        private EvaluationNotification notification;

        /**
         * Definire cosa fare una volta che la notifica è stata inizializzata
         */
        public abstract void onNotificationInitialised (EvaluationNotification notification);

        /**
         * Initializza la notifica con tutti i campi necessari a partire dalla richiesta
         * @param evaluation la valutazione da cui creare la notifica
         */
        public void initialiseNotification (NewEvaluation evaluation) {
            notification = new EvaluationNotification(evaluation);

            notification.sentTime = new Date(evaluation.getSentTime());


            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS)
                    .child(evaluation.getEvaluationSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.sender = snapshot.getValue(User.class);
                            if (notification.sender == null) {
                                throw new RuntimeException("Impossibile trovare l'utente con l'id "
                                        + evaluation.getEvaluationSenderId() +
                                        " nella richiesta di partecipazione gruppo di id " +
                                        evaluation.getEvaluationId());
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
                    .child(evaluation.getGroupId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.group = snapshot.getValue(Group.class);
                            if (notification.group == null) {
                                throw new RuntimeException("Impossibile trovare il gruppo con l'id "
                                        + evaluation.getGroupId() +
                                        " nella richiesta di partecipazione gruppo di id " +
                                        evaluation.getEvaluationId());
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
