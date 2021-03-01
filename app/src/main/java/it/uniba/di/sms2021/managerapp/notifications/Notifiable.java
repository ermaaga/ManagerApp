package it.uniba.di.sms2021.managerapp.notifications;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.Date;

/**
 * Interfaccia che indica che un oggetto può essere visualizzato come notifica nel centro notifiche
 * dell'app (ed eventualmente nella barra delle notifiche del dispositivo)
 */
public interface Notifiable {
    Date getNotificationSentTime ();

    String getNotificationTitle (Context context);

    /**
     * Ritorna il contenuto della notifica
     * @param context il contesto attuale
     */
    String getNotificationMessage(Context context);

    /**
     * Azione da eseguire quando si clicca sull'intera notifica
     * @param listener specifica l'azione da compiere quando l'azione è stata completata con successo
     */
    void onNotificationClick (Context context, @Nullable OnActionDoneListener listener);

    /**
     * Ritorna l'etichetta che deve avere la prima azione nella notifica.
     * Nel caso il bottone non debba apparire, settare a null
     * @return l'etichetta del bottone o null
     */
    String getAction1Label (Context context);

    /**
     * L'azione da eseguire quando viene premuto il bottone per la prima azione nella notifica
     * @param listener specifica l'azione da compiere quando l'azione è stata completata con successo
     */
    void onNotificationAction1Click (Context context, @Nullable OnActionDoneListener listener);

    /**
     * Ritorna l'etichetta che deve avere la seconda azione nella notifica.
     * Nel caso il bottone non debba apparire, settare a null
     * @return l'etichetta del bottone o null
     */
    String getAction2Label (Context context);

    /**
     * L'azione da eseguire quando viene premuto il bottone per la prima azione nella notifica
     * @param listener specifica l'azione da compiere quando l'azione è stata completata con successo
     */
    void onNotificationAction2Click (Context context, @Nullable OnActionDoneListener listener);

    boolean equals(Object o);
}
