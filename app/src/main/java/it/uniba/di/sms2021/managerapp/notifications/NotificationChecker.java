package it.uniba.di.sms2021.managerapp.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Se avviato, ogni X minuti, assegnato in MINUTE_CHECKED, avvia NotificationService
 */
public class NotificationChecker extends BroadcastReceiver {

    // Indica ogni quanti minuti vengono controllate le notifiche
    private static final int MINUTE_CHECKED = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);
    }

    public static void subscribeCheckForNotifications(Context context) {
        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationChecker.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),
                MINUTE_CHECKED * 60 * 1000,
                pendingIntent);
    }

    public static void unsubscribeCheckForNotifications (Context context) {
        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationChecker.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
        context.stopService(new Intent(context, NotificationService.class));
    }
}