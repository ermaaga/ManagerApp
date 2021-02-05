package it.uniba.di.sms2021.managerapp.firebase;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;

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

        File path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (path == null) {
            showErrorMessage(R.string.text_message_download_path_not_usable);
        }

        File localFile = new File(path, file.getName());
        file.getReference().getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                onSuccessAction(localFile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                showErrorMessage(R.string.text_message_preview_open_failed);
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

}
