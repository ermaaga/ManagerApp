package it.uniba.di.sms2021.managerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.enitities.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.lists.NotificationRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.notifications.GroupJoinRequestNotification;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerView;
    private NotificationRecyclerAdapter adapter;
    private ValueEventListener groupRequestsListener;
    private DatabaseReference groupRequestsReference;

    private List<Notifiable> notifications;
    List<GroupJoinRequestNotification> groupJoinRequests;

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

        adapter = new NotificationRecyclerAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        groupRequestsReference = FirebaseDbHelper.getDBInstance()
                .getReference(FirebaseDbHelper.TABLE_GROUP_REQUESTS);
        groupRequestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupJoinRequests = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    GroupJoinRequest request = child.getValue(GroupJoinRequest.class);
                    if (request.getGroupOwnerId().equals(LoginHelper.getCurrentUser().getAccountId())) {
                        new GroupJoinRequestNotification.Initialiser() {
                            @Override
                            public void onNotificationInitialised(GroupJoinRequestNotification notification) {
                                groupJoinRequests.add(notification);
                                updateNotifications();
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
        groupRequestsReference.addValueEventListener(groupRequestsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupRequestsReference.removeEventListener(groupRequestsListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    /**
     * Aggiorna la lista delle notifiche e la mostra nella recyclerView
     */
    private void updateNotifications () {
        notifications = new ArrayList<>();
        notifications.addAll(groupJoinRequests);

        adapter.submitList(notifications);
        adapter.notifyDataSetChanged();
    }
}