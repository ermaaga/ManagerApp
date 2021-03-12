package it.uniba.di.sms2021.managerapp.notifications;

import android.content.Context;

import java.util.Comparator;

/**
 * Ordina le notifiche per data di invio in ordine decrescente
 */
public class NotifiableComparator implements Comparator<Notifiable> {
    /**
     * Ordina le notifiche per data di invio in ordine decrescente
     */
    public NotifiableComparator() {
    }

    @Override
    public int compare(Notifiable o1, Notifiable o2) {
        return (int) (o2.getNotificationSentTime().getTime() - o1.getNotificationSentTime().getTime());
    }
}
