package it.uniba.di.sms2021.managerapp.firebase;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;

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
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;
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
     * il file scaricato.
     * Il file sarà inserito nella path "[CartellaDownload]/ManagerApp/[projectName]"
     *
     * @param file il file da scaricare
     * @param projectName il nome del progetto a cui appartiene il file
     */
    public void downloadFile(ManagerCloudFile file, String projectName) {
        if (!isExternalStorageWritable()) {
            showErrorMessage(R.string.text_message_external_storage_not_found);
        }

        File path = getDownloadPath(projectName);
        if (!path.exists()) {
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
    public static File getDownloadedFile (String fileName, String projectName) {
        return new File (getDownloadPath(projectName), fileName);
    }

    /**
     * Ritorna la cartella dei download di un specifico progetto.
     */
    public static File getDownloadPath(String projectName) {
        File downloadPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(),
                "ManagerApp/" + projectName);
        if (!downloadPath.exists()) {
            downloadPath.mkdirs();
        }

        return downloadPath;
    }
}
