package it.uniba.di.sms2021.managerapp.enitities.project;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;
import it.uniba.di.sms2021.managerapp.notifications.OnActionDoneListener;
import it.uniba.di.sms2021.managerapp.projects.ProjectDetailActivity;

public class GroupJoinNotice implements Notifiable {
    private String uid;
    private String noticeId;
    private User user;
    private Group group;
    private boolean isRequester;
    private long timeSent;

    public GroupJoinNotice() {
    }

    public GroupJoinNotice(String uid, String noticeId, User user, Group group, boolean isRequester, long timeSent) {
        this.uid = uid;
        this.noticeId = noticeId;
        this.user = user;
        this.group = group;
        this.isRequester = isRequester;
        this.timeSent = timeSent;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isRequester() {
        return isRequester;
    }

    public void setRequester(boolean requester) {
        isRequester = requester;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    @Override
    @Exclude
    public Date getNotificationSentTime() {
        return new Date(timeSent);
    }

    @Override
    public String getNotificationTitle(Context context) {
        return context.getString(R.string.text_notification_title_group_join_notice);
    }

    @Override
    public String getNotificationMessage(Context context) {
        if (isRequester) {
            return context.getString(R.string.text_notification_message_group_join_notice_requester,
                    group.getName());
        } else {
            return context.getString(R.string.text_notification_message_group_join_notice,
                    user.getFullName(), group.getName());
        }

    }

    @Override
    // Quando si clicca sulla notifica viene mostrato il gruppo nella sezione membri
    public void onNotificationClick(Context context, @Nullable OnActionDoneListener listener) {
        new Project.Initialiser() {
            @Override
            public void onProjectInitialised(Project project) {
                Intent intent = new Intent(context, ProjectDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Project.KEY, project);
                intent.putExtra(ProjectDetailActivity.INITIAL_TAB_POSITION_KEY,
                        ProjectDetailActivity.MEMBERS_TAB_POSITION);

                context.startActivity(intent);
            }
        }.initialiseProject(group);
    }

    @Override
    public String getAction1Label(Context context) {
        return context.getString(R.string.text_notification_action_dismiss);
    }

    @Override
    public void onNotificationAction1Click(Context context, @Nullable OnActionDoneListener listener) {
        FirebaseDbHelper.getUserJoinNoticeReference(uid).child(noticeId).removeValue();

        if (listener != null) {
            listener.onActionDone();
        }
    }

    @Override
    public String getAction2Label(Context context) {
        return null;
    }

    @Override
    public void onNotificationAction2Click(Context context, @Nullable OnActionDoneListener listener) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupJoinNotice that = (GroupJoinNotice) o;
        return isRequester == that.isRequester &&
                timeSent == that.timeSent &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(noticeId, that.noticeId) &&
                Objects.equals(user, that.user) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, noticeId, user, group, isRequester, timeSent);
    }

    @Override
    public String toString() {
        return "GroupJoinNotice{" +
                "uid='" + uid + '\'' +
                ", noticeId='" + noticeId + '\'' +
                ", user=" + user +
                ", group=" + group +
                ", isRequester=" + isRequester +
                ", timeSent=" + timeSent +
                '}';
    }
}
