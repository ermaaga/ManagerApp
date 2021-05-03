package it.uniba.di.sms2021.managerapp.firebase;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

/**
 * Classe di utility che astrae il download di un file temporaneo e permette di fare un'azione con
 * il file scaricato.
 * Nel caso il file sia già stato scaricato precedentemente, verrà usato il file
 * in memoria interna.
 */
public abstract class TemporaryFileDownloader {
    // Si possono scaricare file temporanei grandi massimo 10MB
    private static final int MAX_TEMP_FILE_SIZE = 1024*1024*10;
    private static final String TAG = "TemporaryFileDownloader";

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
     * il file scaricato o il file in memoria interna se già era presente.
     * Inoltre controlla se il file è stato già scaricato in precedenza nella cartella dei downloads
     * in memoria esterna, e se è presente lo utilizza.
     *
     * @param file il file da scaricare
     * @param internalFolderName nome della cartella in cui salvare il file
     */
    public void downloadTempFile (ManagerFile file, String internalFolderName) {
        File downloadedFile = FileDownloader.getDownloadedFile(file.getName());
        if (downloadedFile.exists() &&
                downloadedFile.length() == file.getSize()) {
            Log.i(TAG, "Using downloaded file");
            onSuccessAction(downloadedFile);
            return;
        }

        if (file.getSize() > MAX_TEMP_FILE_SIZE) {
            showDownloadSuggestion(R.string.text_message_temp_file_too_big);
            return;
        }

        File path = new File(context.getFilesDir(), internalFolderName);
        if (!path.exists()) {
            path.mkdirs();
        }
        File localFile = new File(path, file.getName());
        if (localFile.exists()) {
            Log.i(TAG, "Using temporary file");
            onSuccessAction(localFile);
        } else {
            FileDownloadTask fileDownloadTask = file.getReference().getFile(localFile);
            fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "Using downloaded temporary file");
                    onSuccessAction(localFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, R.string.text_message_preview_open_failed,
                            Toast.LENGTH_LONG).show();
                }
            });

            // Se dopo 5 secondi il file non è stato aperto, cancella l'operazione di download
            new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!fileDownloadTask.isComplete()) {
                        fileDownloadTask.cancel();
                        showDownloadSuggestion(R.string.text_message_preview_taking_too_long);
                    }
                }
            }, 5000);
        }
    }
}
