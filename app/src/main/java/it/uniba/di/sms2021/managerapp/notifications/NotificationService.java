package it.uniba.di.sms2021.managerapp.notifications;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.NotificationsActivity;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.utility.NotificationUtil;

/**
 * Servizio che cerca notifiche e le mostra nella barra delle notifiche.
 * Opera in un thread in background per non bloccare l'applicazione
 *
 * NOTA: per far sì che il servizio funzioni, l'utente in LoginHelper deve essere inizializzato
 * Codice preso e modificato da: https://developer.android.com/guide/components/services#ExtendingService
 */
public class NotificationService extends Service {
    private static final String TAG = "NotificationService";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private DatabaseReference groupRequestReference;
    private Set<DatabaseReference> workingReferences;
    private int notificationsFound;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Cerca se ci sono notifiche, le mostra e chiude il servizio
        @Override
        public void handleMessage(Message msg) {
            checkForNotifications (msg);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service starting");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service done");
    }

    private void checkForNotifications(Message msg) {
        Log.i(TAG, "Checking for Notifications");

        workingReferences = new HashSet<>();
        notificationsFound = 0;

        groupRequestReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUP_REQUESTS);
        workingReferences.add(groupRequestReference);

        groupRequestReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    GroupJoinRequest request = child.getValue(GroupJoinRequest.class);
                    if (request.getGroupOwnerId().equals(LoginHelper.getCurrentUser().getAccountId())) {
                        notificationsFound += 1;
                    }
                }

                workingReferences.remove(groupRequestReference);
                showNotification(msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference userJoinNoticeReference = FirebaseDbHelper.getUserJoinNoticeReference(LoginHelper.getCurrentUser().getAccountId());
        workingReferences.add(userJoinNoticeReference);

        userJoinNoticeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationsFound += snapshot.getChildrenCount();
                        workingReferences.remove(userJoinNoticeReference);
                        showNotification(msg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showNotification (Message msg) {
        if (workingReferences.isEmpty()) {
            if (notificationsFound != 0) {
                NotificationUtil.createNotificationChannel(this);

                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtil.DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.text_notification_title_feed))
                        .setContentText(getResources().getQuantityString(R.plurals.numberOfNotifications,
                                notificationsFound, notificationsFound))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(NotificationUtil.FEED_NOTIFICATION_ID, builder.build());
            }

            stopSelf(msg.arg1);
        }
    }
}