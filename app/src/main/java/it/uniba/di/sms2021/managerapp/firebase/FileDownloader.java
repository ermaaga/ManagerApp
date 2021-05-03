package it.uniba.di.sms2021.managerapp.firebase;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;

import java.io.File;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;
import it.uniba.di.sms2021.managerapp.utility.NotificationUtil;

/**
 * Classe di utility che astrae il download di un file e permette di fare un'azione con
 * il file scaricato.
 */
public abstract class FileDownloader {
    /**
     * Specifica l'azione da compiere quando il file è stato scaricato.
     */
    protected abstract void onSuccessAction(File localFile);

    /**
     * Specifica la modalità con cui avvertire l'utente quando qualcosa va storto.
     * @param message la risorse del messaggio da visualizzare all'utente
     */
    protected abstract void showErrorMessage(@StringRes int message);

    private final Context context;

    /**
     * Classe di utility che astrae il download di un file e permette di fare un'azione con
     * il file scaricato.
     */
    public FileDownloader(Context context) {
        this.context = context;
    }

    /**
     * Scarica il file nella cartella dei download e quando finisce chiama onSuccessAction usando
     * il file scaricato
     *
     * @param file il file da scaricare
     */
    public void downloadFile(ManagerFile file) {
        if (!isExternalStorageWritable()) {
            showErrorMessage(R.string.text_message_external_storage_not_found);
        }

        File path = getDownloadPathDeprecated();
        if (path == null || !path.exists()) {
            showErrorMessage(R.string.text_message_download_path_not_usable);
        }

        // Crea la notifica del download
        NotificationUtil.createNotificationChannel(context);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotificationUtil.DEFAULT_CHANNEL_ID);
        builder.setContentTitle(context.getString(R.string.text_notification_title_file_download))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);

        // Issue the initial notification with zero progress
        int PROGRESS_MAX = 100;
        builder.setProgress(PROGRESS_MAX, 0, false);
        notificationManager.notify(NotificationUtil.DOWNLOAD_NOTIFICATION_ID, builder.build());

        // Avvia il processo di download
        File localFile = new File(path, file.getName());
        FileDownloadTask downloadTask = file.getReference().getFile(localFile);
        downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                int progress = (int) Math.round((100.0 *
                        snapshot.getBytesTransferred()) / snapshot.getTotalByteCount()
                        * 100.0) / 100;

                builder.setProgress(PROGRESS_MAX, progress, false)
                        .setContentText(context.getString(R.string.text_message_download_progress,
                                FileUtil.getFormattedSize(context, snapshot.getBytesTransferred()),
                                FileUtil.getFormattedSize(context, snapshot.getTotalByteCount())));
                notificationManager.notify(NotificationUtil.DOWNLOAD_NOTIFICATION_ID, builder.build());

            }
        }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Intent intent = FileUtil.getFileViewIntent(context,
                        FileUtil.getUriFromFile(context, localFile), file.getType());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                int uniqueRequestCode = file.hashCode(); // Se il request code è uguale ad un'activity già
                                                         // aperta la riusa.
                PendingIntent pendingIntent = PendingIntent.getActivity(context, uniqueRequestCode, intent, 0);

                // Aggiorna la notifica dopo 500ms per non avere problemi con aggiornamenti troppo frequenti
                // come specificato in https://developer.android.com/training/notify-user/build-notification#Updating
                new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        builder.setContentText(context.getString(R.string.text_message_download_complete))
                                .setProgress(0,0,false)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        notificationManager.notify(NotificationUtil.DOWNLOAD_NOTIFICATION_ID, builder.build());
                    }
                }, 500);

                onSuccessAction(localFile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                builder.setProgress(0, 0, false)
                        .setContentText(context.getString(R.string.text_message_download_failed));
                notificationManager.notify(NotificationUtil.DOWNLOAD_NOTIFICATION_ID, builder.build());
                showErrorMessage(R.string.text_message_download_failed);
            }
        });
    }

    /**
     * Ritorna true se lo storage esterno è scrivibile
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Metodo di utility per ottenere un file scaricato in precedenza da questa classe
     * @param fileName il nome del file da ottenere
     */
    public static File getDownloadedFile (String fileName) {
        return new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
    }

    private File getDownloadPathDeprecated() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * Codice non funzionante preso da
     * https://stackoverflow.com/questions/56468539/getexternalstoragepublicdirectory-deprecated-in-android-q
     * per adempire alla deprecazione di "getExternalStoragePublicDirectory".
     * Avendolo testato e modificato in vari modi, accade che:
     * - o insert da un errore
     * - o non da nessun errore ma uri risulta null.
     * Ho consultato stackoverflow perchè la documentazione di Mediastore è molto scarsa e
     * l'uso delle altre soluzioni (Context.getExternalFilesDir(String), Intent.ACTION_OPEN_DOCUMENT)
     * non sono adatte al contesto poichè una conserva il contenuto in una cartella che viene
     * cancellata insieme all'app, e l'altra richiede azioni aggiuntive dall'utente.
     *
     * @param file file da scaricare con le informazioni per il file temporaneo da creare
     * @return path del file temporaneo
     */
    private File getTempFile(ManagerFile file) {
        File path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, file.getType());
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            path = new File(uri.getPath());
        } else {
            path = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    file.getName());
        }
        return path;
    }


}
