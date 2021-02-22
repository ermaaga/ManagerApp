package it.uniba.di.sms2021.managerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.lists.NotificationRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.notifications.GroupJoinRequestNotification;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerView;
    private NotificationRecyclerAdapter adapter;

    private List<Notifiable> notifications;

    private List<GroupJoinRequestNotification> groupJoinRequests;
    private List<GroupJoinNotice> groupJoinNotices;
    private Set<DatabaseReference> workingReferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    private void updateNotifications() {
        workingReferences = new HashSet<>();

        DatabaseReference groupRequestsReference =
                FirebaseDbHelper.getGroupJoinRequestReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(groupRequestsReference);
        ValueEventListener groupRequestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupJoinRequests = new ArrayList<>();

                Log.d(TAG, snapshot.getChildrenCount() + "");

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

            adapter.submitList(notifications);
            adapter.notifyDataSetChanged();
        }
    }
}