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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import it.uniba.di.sms2021.managerapp.NotificationsActivity;
import it.uniba.di.sms2021.managerapp.R;
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

    private ServiceHandler serviceHandler;

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
        Looper serviceLooper = thread.getLooper();
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
        notificationsFound = 0;

        DatabaseReference notificationsReference = FirebaseDbHelper.getNotifications(
                LoginHelper.getCurrentUser().getAccountId()
        );
        notificationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsFound += snapshot.child(FirebaseDbHelper.TABLE_EXAM_JOIN_REQUESTS).getChildrenCount();
                notificationsFound += snapshot.child(FirebaseDbHelper.TABLE_GROUP_JOIN_NOTICE).getChildrenCount();
                notificationsFound += snapshot.child(FirebaseDbHelper.TABLE_GROUP_JOIN_REQUESTS).getChildrenCount();
                notificationsFound += snapshot.child(FirebaseDbHelper.TABLE_NEW_EVALUATION).getChildrenCount();
                notificationsFound += snapshot.child(FirebaseDbHelper.TABLE_NEW_REPLY_REPORT).getChildrenCount();
                notificationsFound += snapshot.child(FirebaseDbHelper.TABLE_NEW_REPORT).getChildrenCount();

                showNotification(msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showNotification (Message msg) {
        if (notificationsFound != 0) {
            NotificationUtil.createNotificationChannel(this);

            Intent intent = new Intent(this, NotificationsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtil.DEFAULT_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getResources().getQuantityString(R.plurals.text_notification_title_feed,
                            notificationsFound))
                    .setContentText(getResources().getQuantityString(R.plurals.numberOfNotifications,
                            notificationsFound, notificationsFound))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NotificationUtil.FEED_NOTIFICATION_ID, builder.build());
        }

        stopSelf(msg.arg1);
    }
}