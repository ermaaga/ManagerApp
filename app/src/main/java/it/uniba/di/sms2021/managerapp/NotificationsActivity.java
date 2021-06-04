package it.uniba.di.sms2021.managerapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.enitities.notifications.ExamJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinNotice;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewEvaluation;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReplyReportNotice;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReportNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.lists.NotificationRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.notifications.EvaluationNotification;
import it.uniba.di.sms2021.managerapp.notifications.GroupJoinRequestNotification;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;
import it.uniba.di.sms2021.managerapp.notifications.ReplyNotification;
import it.uniba.di.sms2021.managerapp.notifications.ReportNotification;
import it.uniba.di.sms2021.managerapp.utility.AbstractBaseActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class NotificationsActivity extends AbstractBaseActivity {

    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerView;
    private NotificationRecyclerAdapter adapter;

    private List<Notifiable> notifications;

    private Set<DataSnapshot> workingSnapshots;

    private List<ExamJoinRequest> examJoinRequests;
    private List<GroupJoinRequestNotification> groupJoinRequests;
    private List<GroupJoinNotice> groupJoinNotices;
    private List<EvaluationNotification> newEvaluations;
    private List<ReplyNotification> newReplyReportNotices;
    private List<ReportNotification> newReportNotices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        MenuUtil.setIncludedToolbar(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.notifications_recycler_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        notifications = new ArrayList<>();

        adapter = new NotificationRecyclerAdapter(this, new NotificationRecyclerAdapter.OnUpdateDataListener() {
            @Override
            public void onUpdateData() {
                if (!notificationsAlreadyInElaboration()) {
                    updateNotifications();
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        updateNotifications();
    }

    private boolean notificationsAlreadyInElaboration() {
        return workingSnapshots != null && !workingSnapshots.isEmpty();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void updateNotifications () {
        if (LoginHelper.getCurrentUser().getAccountId() == null) {
            return;
        }

        workingSnapshots = new HashSet<>();
        DatabaseReference notificationsReference = FirebaseDbHelper.getNotifications(
                LoginHelper.getCurrentUser().getAccountId()
        );

        ValueEventListener notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    executeUpdate(false);
                    return;
                }

                initialiseExamJoinRequests(snapshot);
                initialiseGroupJoinNotices(snapshot);
                initialiseGroupJoinRequests(snapshot);
                initialiseNewEvaluations(snapshot);
                initialiseNewReplyReportNotice(snapshot);
                initialiseNewReportNotices(snapshot);
                executeUpdate(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        };

        notificationsReference.addListenerForSingleValueEvent(notificationsListener);
    }

    public void initialiseExamJoinRequests(DataSnapshot snapshot) {
        examJoinRequests = new ArrayList<>();
        DataSnapshot examJoinRequestSnapshot = snapshot.child(FirebaseDbHelper.TABLE_EXAM_JOIN_REQUESTS);
        Log.d(TAG,"ExamJoinRequests: " + examJoinRequestSnapshot.getChildrenCount());
        for (DataSnapshot child : examJoinRequestSnapshot.getChildren()) {
            ExamJoinRequest request = child.getValue(ExamJoinRequest.class);
            examJoinRequests.add(request);
        }
    }

    public void initialiseGroupJoinNotices(DataSnapshot snapshot) {
        groupJoinNotices = new ArrayList<>();
        DataSnapshot groupJoinNoticesSnapshot = snapshot.child(FirebaseDbHelper.TABLE_GROUP_JOIN_NOTICE);
        Log.d(TAG,  "GroupJoinNotices: " + groupJoinNoticesSnapshot.getChildrenCount());
        for (DataSnapshot child : groupJoinNoticesSnapshot.getChildren()) {
            GroupJoinNotice notice = child.getValue(GroupJoinNotice.class);
            groupJoinNotices.add(notice);
        }
    }

    public void initialiseGroupJoinRequests(DataSnapshot snapshot) {
        DataSnapshot groupJoinRequestsSnapshot = snapshot.child(FirebaseDbHelper.TABLE_GROUP_JOIN_REQUESTS);
        groupJoinRequests = new ArrayList<>();
        Log.d(TAG, "GroupJoinRequests: " + groupJoinRequestsSnapshot.getChildrenCount());
        if (groupJoinRequestsSnapshot.getChildrenCount() != 0) {
            workingSnapshots.add(groupJoinRequestsSnapshot);

            // Inizializza le notifiche e non appena sono tutte inizializzate aggiorna la ui
            Set<DataSnapshot> elaboratingChildren = new HashSet<>();
            for (DataSnapshot child : groupJoinRequestsSnapshot.getChildren()) {
                GroupJoinRequest request = child.getValue(GroupJoinRequest.class);
                if (request.getGroupOwnerId().equals(LoginHelper.getCurrentUser().getAccountId())) {
                    elaboratingChildren.add(child);
                    new GroupJoinRequestNotification.Initialiser() {
                        @Override
                        public void onNotificationInitialised(GroupJoinRequestNotification notification) {
                            groupJoinRequests.add(notification);
                            elaboratingChildren.remove(child);

                            if (elaboratingChildren.isEmpty()) {
                                workingSnapshots.remove(groupJoinRequestsSnapshot);
                                executeUpdate(true);
                            }
                        }
                    }.initialiseNotification(request);
                }
            }
        }
    }

    public void initialiseNewEvaluations(DataSnapshot snapshot) {
        DataSnapshot newEvaluationsSnapshot = snapshot.child(FirebaseDbHelper.TABLE_NEW_EVALUATION);
        newEvaluations = new ArrayList<>();
        Log.d(TAG,  "NewEvaluations: " + newEvaluationsSnapshot.getChildrenCount());

        if (newEvaluationsSnapshot.getChildrenCount() != 0) {
            workingSnapshots.add(newEvaluationsSnapshot);

            Set<DataSnapshot> elaboratingChildren = new HashSet<>();
            for (DataSnapshot child : newEvaluationsSnapshot.getChildren()) {

                elaboratingChildren.add(child);
                NewEvaluation evaluation = child.getValue(NewEvaluation.class);
                new EvaluationNotification.Initialiser() {
                    @Override
                    public void onNotificationInitialised(EvaluationNotification notification) {
                        newEvaluations.add(notification);
                        elaboratingChildren.remove(child);

                        if (elaboratingChildren.isEmpty()) {
                            workingSnapshots.remove(newEvaluationsSnapshot);
                            executeUpdate(true);
                        }
                    }
                }.initialiseNotification(evaluation);
            }
        }
    }

    public void initialiseNewReplyReportNotice(DataSnapshot snapshot) {
        DataSnapshot newReplyReportNoticesSnapshot = snapshot.child(
                FirebaseDbHelper.TABLE_NEW_REPLY_REPORT
        );
        newReplyReportNotices = new ArrayList<>();
        Log.d(TAG, "NewReplyReportNotices: " + newReplyReportNoticesSnapshot.getChildrenCount());

        if (newReplyReportNoticesSnapshot.getChildrenCount() != 0) {
            workingSnapshots.add(newReplyReportNoticesSnapshot);

            Set<DataSnapshot> elaboratingChildren = new HashSet<>();
            for (DataSnapshot child : newReplyReportNoticesSnapshot.getChildren()) {
                elaboratingChildren.add(child);

                NewReplyReportNotice replyReportNotice = child.getValue(NewReplyReportNotice.class);
                new ReplyNotification.Initialiser() {
                    @Override
                    public void onNotificationInitialised(ReplyNotification notification) {
                        newReplyReportNotices.add(notification);
                        elaboratingChildren.remove(child);

                        if (elaboratingChildren.isEmpty()) {
                            workingSnapshots.remove(newReplyReportNoticesSnapshot);
                            executeUpdate(true);
                        }
                    }
                }.initialiseNotification(replyReportNotice);
            }
        }
    }

    public void initialiseNewReportNotices(DataSnapshot snapshot) {
        DataSnapshot newReportNoticeSnapshot = snapshot.child(
                FirebaseDbHelper.TABLE_NEW_REPORT
        );
        newReportNotices = new ArrayList<>();
        Log.d(TAG, "NewReportNotices: " + newReportNoticeSnapshot.getChildrenCount());

        if (newReportNoticeSnapshot.getChildrenCount() != 0) {
            workingSnapshots.add(newReportNoticeSnapshot);

            Set<DataSnapshot> elaboratingChildren = new HashSet<>();
            for (DataSnapshot child : newReportNoticeSnapshot.getChildren()) {

                elaboratingChildren.add(child);
                NewReportNotice reportNotice = child.getValue(NewReportNotice.class);
                new ReportNotification.Initialiser() {
                    @Override
                    public void onNotificationInitialised(ReportNotification notification) {
                        newReportNotices.add(notification);
                        elaboratingChildren.remove(child);

                        if (elaboratingChildren.isEmpty()) {
                            workingSnapshots.remove(newReportNoticeSnapshot);
                            executeUpdate(true);
                        }
                    }
                }.initialiseNotification(reportNotice);
            }
        }
    }

    /**
     * Aggiorna la lista delle notifiche e la mostra nella recyclerView ma solo se tutte le
     * notifiche sono state elaborate
     */
    private void executeUpdate(boolean notificationsPresent) {
        if (!notificationsAlreadyInElaboration()) {
            notifications = new ArrayList<>();

            if (notificationsPresent) {
                notifications.addAll(groupJoinRequests);
                notifications.addAll(groupJoinNotices);
                notifications.addAll(newEvaluations);
                notifications.addAll(examJoinRequests);
                notifications.addAll(newReportNotices);
                notifications.addAll(newReplyReportNotices);
            }

            adapter.submitList(notifications);
            adapter.notifyDataSetChanged();
        }
    }
}