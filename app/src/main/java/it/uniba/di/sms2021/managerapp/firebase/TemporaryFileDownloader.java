package it.uniba.di.sms2021.managerapp.firebase;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;

/**
 * Classe di utility che astrae il download di un file temporaneo e permette di fare un'azione con
 * il file scaricato.
 * Nel caso il file sia già stato scaricato precedentemente, verrà usato il file
 * in memoria interna.
 */
public abstract class TemporaryFileDownloader {
    // Si possono scaricare file temporanei grandi massimo 10MB
    private static final int MAX_TEMP_FILE_SIZE = 1024*1024*10;

    /**
     * Mostra un messaggio qualora non sia possibile scaricare il file come file temporaneo.
     * @param messageRes il messaggio da mostrare
     */
    protected abstract void showDownloadSuggestion(int messageRes);

    /**
     * Specifica l'azione da compiere quando il file è stato scaricato.
     */
    protected abstract void onSuccessAction(File localFile);

    private Context context;

    /**
     * Classe di utility che astrae il download di un file temporaneo e permette di fare un'azione con
     * il file scaricato.
     * Nel caso il file sia già stato scaricato precedentemente, verrà usato il file
     * in memoria interna.
     */
    public TemporaryFileDownloader (Context context) {
        this.context = context;
    }

    /**
     * Scarica il file in una cartella interna e quando finisce chiama onSuccessAction usando
     * il file scaricato o il file in memoria se già era presente.
     *
     * @param file il file da scaricare
     * @param internalFolderName nome della cartella in cui salvare il file
     */
    public void downloadTempFile (ManagerFile file, String internalFolderName) {
        if (file.getSize() > MAX_TEMP_FILE_SIZE) {
            showDownloadSuggestion(R.string.text_message_temp_file_too_big);
            return;
        }

        File path = new File(context.getFilesDir(), internalFolderName); // project.getId()
        if (!path.exists()) {
            path.mkdirs();
        }
        File localFile = new File(path, file.getName());
        if (localFile.exists()) {
            onSuccessAction(localFile);
        } else {
            file.getReference().getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    onSuccessAction(localFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, R.string.text_message_preview_open_failed,
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
