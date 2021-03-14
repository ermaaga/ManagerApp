package it.uniba.di.sms2021.managerapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReportNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.lists.NotificationRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.notifications.EvaluationNotification;
import it.uniba.di.sms2021.managerapp.notifications.GroupJoinRequestNotification;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;
import it.uniba.di.sms2021.managerapp.notifications.ReportNotification;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerView;
    private NotificationRecyclerAdapter adapter;

    private List<Notifiable> notifications;

    private List<GroupJoinRequestNotification> groupJoinRequests;
    private List<GroupJoinNotice> groupJoinNotices;
    private List<ExamJoinRequest> examJoinRequests;
    private Set<DatabaseReference> workingReferences;

    private List<EvaluationNotification> newEvaluations;

    private List<ReportNotification> newReports;


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
                updateNotifications();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        updateNotifications();
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

    //TODO ristrutturare db e fare un solo valueEventListener
    private void updateNotifications() {
        workingReferences = new HashSet<>();

        DatabaseReference groupRequestsReference =
                FirebaseDbHelper.getGroupJoinRequestReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(groupRequestsReference);

        ValueEventListener groupRequestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupJoinRequests = new ArrayList<>();

                Log.d(TAG,  "groupJoinRequests: "+snapshot.getChildrenCount() );

                // Se non ci sono figli aggiorna subito la ui
                if (snapshot.getChildrenCount() == 0) {
                    workingReferences.remove(groupRequestsReference);
                    executeUpdate();
                    return;
                }

                // Inizializza le notifiche e non appena sono tutte inizializzate aggiorna la ui
                Set<DataSnapshot> elaboratingChildren = new HashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    GroupJoinRequest request = child.getValue(GroupJoinRequest.class);
                    if (request.getGroupOwnerId().equals(LoginHelper.getCurrentUser().getAccountId())) {
                        elaboratingChildren.add(child);
                        new GroupJoinRequestNotification.Initialiser() {
                            @Override
                            public void onNotificationInitialised(GroupJoinRequestNotification notification) {
                                groupJoinRequests.add(notification);
                                elaboratingChildren.remove(child);

                                if (elaboratingChildren.isEmpty()) {
                                    workingReferences.remove(groupRequestsReference);
                                    executeUpdate();
                                }
                            }
                        }.initialiseNotification(request);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        };
        groupRequestsReference.addListenerForSingleValueEvent(groupRequestsListener);

        DatabaseReference userJoinNoticeReference = FirebaseDbHelper.getUserJoinNoticeReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(userJoinNoticeReference);

        ValueEventListener userJoinNoticeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupJoinNotices = new ArrayList<>();

                Log.d(TAG,  "userJoinNotice: "+snapshot.getChildrenCount() );

                for (DataSnapshot child : snapshot.getChildren()) {
                    GroupJoinNotice notice = child.getValue(GroupJoinNotice.class);
                    groupJoinNotices.add(notice);
                }

                workingReferences.remove(userJoinNoticeReference);
                executeUpdate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userJoinNoticeReference.addListenerForSingleValueEvent(userJoinNoticeListener);

        DatabaseReference newEvaluationReference = FirebaseDbHelper.getNewEvaluationReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(newEvaluationReference);

        ValueEventListener newEvaluationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newEvaluations = new ArrayList<>();

                Log.d(TAG,  "newEvaluation: "+snapshot.getChildrenCount() );

                if (snapshot.getChildrenCount() == 0) {
                    workingReferences.remove(newEvaluationReference);
                    executeUpdate();
                    return;
                }

                Set<DataSnapshot> elaboratingChildren = new HashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) {

                    elaboratingChildren.add(child);
                    NewEvaluation evaluation = child.getValue(NewEvaluation.class);
                        new EvaluationNotification.Initialiser() {
                            @Override
                            public void onNotificationInitialised(EvaluationNotification notification) {
                                newEvaluations.add(notification);
                                elaboratingChildren.remove(child);

                                if (elaboratingChildren.isEmpty()) {
                                    workingReferences.remove(newEvaluationReference);
                                    executeUpdate();
                                }
                                Log.d(TAG,"size listener" + newEvaluations.size());
                            }
                        }.initialiseNotification(evaluation);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        };
        newEvaluationReference.addListenerForSingleValueEvent(newEvaluationListener);

        DatabaseReference newReportReference = FirebaseDbHelper.getNewReportReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(newReportReference);

        ValueEventListener newReportListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newReports = new ArrayList<>();

                if (snapshot.getChildrenCount() == 0) {
                    workingReferences.remove(newReportReference);
                    executeUpdate();
                    return;
                }

                Set<DataSnapshot> elaboratingChildren = new HashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) {

                    elaboratingChildren.add(child);
                    NewReportNotice reportNotice = child.getValue(NewReportNotice.class);
                    new ReportNotification.Initialiser() {
                        @Override
                        public void onNotificationInitialised(ReportNotification notification) {
                            newReports.add(notification);
                            elaboratingChildren.remove(child);

                            if (elaboratingChildren.isEmpty()) {
                                workingReferences.remove(newReportReference);
                                executeUpdate();
                            }
                        }
                    }.initialiseNotification(reportNotice);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        };
        newReportReference.addListenerForSingleValueEvent(newReportListener);

        DatabaseReference examJoinRequestsReference = FirebaseDbHelper.getExamJoinRequestReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(examJoinRequestsReference);

        ValueEventListener examJoinRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                examJoinRequests = new ArrayList<>();

                Log.d(TAG,  "examJoinRequests: "+snapshot.getChildrenCount() );

                for (DataSnapshot child : snapshot.getChildren()) {
                    ExamJoinRequest request = child.getValue(ExamJoinRequest.class);
                    examJoinRequests.add(request);
                }

                workingReferences.remove(examJoinRequestsReference);
                executeUpdate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        examJoinRequestsReference.addListenerForSingleValueEvent(examJoinRequestListener);
    }

    /**
     * Aggiorna la lista delle notifiche e la mostra nella recyclerView ma solo se tutte le
     * notifiche sono state elaborate
     */
    private void executeUpdate() {
        if (workingReferences.isEmpty()) {
            notifications = new ArrayList<>();

            notifications.addAll(groupJoinRequests);
            notifications.addAll(groupJoinNotices);
            notifications.addAll(newEvaluations);
            notifications.addAll(examJoinRequests);
            notifications.addAll(newReports);

            adapter.submitList(notifications);
            adapter.notifyDataSetChanged();
        }
    }
}