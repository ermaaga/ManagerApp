package it.uniba.di.sms2021.managerapp.notifications;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.enitities.project.GroupJoinNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

/**
 * Classe wrapper di una richiesta di partecipazione ad un gruppo che inizializza tutti i campi
 * necessari nelle operazioni che riguardano l'oggetto.
 */
public class GroupJoinRequestNotification implements Notifiable {
    private GroupJoinRequest request;
    private User sender;
    private Group group;
    private Date sentTime;

    private GroupJoinRequestNotification(GroupJoinRequest request) {
        this.request = request;
    }

    public String getRequestId() {
        return request.getRequestId();
    }

    public void setRequestId(String requestId) {
        request.setRequestId(requestId);
    }

    public String getRequestSenderId() {
        return request.getRequestSenderId();
    }

    public void setRequestSenderId(String requestSenderId) {
        request.setRequestSenderId(requestSenderId);
    }

    public String getGroupId() {
        return request.getGroupId();
    }

    public void setGroupId(String groupId) {
        request.setGroupId(groupId);
    }

    public String getGroupOwnerId() {
        return request.getGroupOwnerId();
    }

    public void setGroupOwnerId(String groupOwnerId) {
        request.setGroupOwnerId(groupOwnerId);
    }

    public Long getSentTime() {
        return request.getSentTime();
    }

    public void setSentTime(Long sentTime) {
        request.setSentTime(sentTime);
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
    public String toString() {
        return "GroupJoinRequestNotification{" +
                "sender=" + sender +
                ", group=" + group +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupJoinRequestNotification that = (GroupJoinRequestNotification) o;
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

    @Override
    public Date getNotificationSentTime() {
        return sentTime;
    }

    @Exclude
    @Override
    public String getNotificationTitle(Context context) {
        return context.getString(R.string.text_notification_title_group_join_request);
    }

    @Exclude
    @Override
    public String getNotificationMessage(Context context) {
        return context.getString(R.string.text_notification_message_group_join_request,
                sender.getFullName(), group.getName());
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
    // Accetta l'utente nel gruppo
    public void onNotificationAction1Click(Context context, @Nullable OnActionDoneListener listener) {
        List<String> members = group.getMembri();
        if (members == null) {
            throw new RuntimeException("Questo non dovrebbe mai accadere");
        }
        members.add(sender.getAccountId());

        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                .child(group.getId()).child(Group.Keys.MEMBERS).setValue(members)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, R.string.text_message_group_request_accepted,
                                Toast.LENGTH_LONG).show();
                        removeNotification();
                        notifyMembers(group, sender);

                        // Usato per aggiornare ui in seguito all'aggiornamento
                        if (listener != null) {
                            listener.onActionDone();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public String getAction2Label(Context context) {
        return context.getString(R.string.text_button_request_refuse);
    }

    @Override
    // Rifiuta l'utente nel gruppo
    public void onNotificationAction2Click(Context context, @Nullable OnActionDoneListener listener) {
        removeNotification();

        // Usato per aggiornare ui in seguito all'aggiornamento
        if (listener != null) {
            listener.onActionDone();
        }
    }

    private void removeNotification () {
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUP_REQUESTS)
                .child(request.getRequestId()).removeValue();
    }

    /**
     * Notifica gli altri membri del gruppo che si è unito un nuovo membro
     * Per fare ciò aggiunge l'oggetto GroupJoinNotice nel database così che il gestore delle
     * notifiche possa prelevarne i valori.
     * @param group il gruppo da notificare
     * @param sender il membro che si è unito
     */
    private void notifyMembers(Group group, User sender) {
        for (String user: group.getMembri()) {
            if (!user.equals(group.getMembri().get(0))) {
                DatabaseReference pushReference = FirebaseDbHelper.getUserJoinNoticeReference(user).push();
                pushReference.setValue(
                        new GroupJoinNotice(user, pushReference.getKey(), sender, group,
                                user.equals(sender.getAccountId()), System.currentTimeMillis()));
            }
        }
    }

    public static abstract class Initialiser {
        private GroupJoinRequestNotification notification;

        /**
         * Definire cosa fare una volta che la notifica è stata inizializzata
         */
        public abstract void onNotificationInitialised (GroupJoinRequestNotification notification);

        /**
         * Initializza la notifica con tutti i campi necessari a partire dalla richiesta
         * @param request la richiesta da cui creare la notifica
         */
        public void initialiseNotification (GroupJoinRequest request) {
            notification = new GroupJoinRequestNotification(request);

            notification.sentTime = new Date(request.getSentTime());

            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS)
                    .child(request.getRequestSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    notification.sender = snapshot.getValue(User.class);
                    if (notification.sender == null) {
                        throw new RuntimeException("Impossibile trovare l'utente con l'id "
                                + request.getRequestSenderId() +
                                " nella richiesta di partecipazione gruppo di id " +
                                request.getRequestId());
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
                    .child(request.getGroupId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notification.group = snapshot.getValue(Group.class);
                            if (notification.group == null) {
                                throw new RuntimeException("Impossibile trovare il gruppo con l'id "
                                        + request.getGroupId() +
                                        " nella richiesta di partecipazione gruppo di id " +
                                        request.getRequestId());
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
