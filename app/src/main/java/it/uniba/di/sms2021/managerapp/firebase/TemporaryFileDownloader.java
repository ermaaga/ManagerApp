package it.uniba.di.sms2021.managerapp.firebase;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;

/**
 * Classe di utility che astrae il download di un file temporaneo e permette di fare un'azione con
 * il file scaricato.
 */
public abstract class TemporaryFileDownloader {
    // Si possono scaricare file temporanei grandi massimo 10MB
    private static final int MAX_TEMP_FILE_SIZE = 1024*1024*10;
    private static final String TAG = "TemporaryFileDownloader";

    /**
     * Mostra un messaggio qualora non sia possibile scaricare il file come file temporaneo.
     * @param messageRes il messaggio da mostrare
     */
    protected abstract void showDownloadFailedMessage(int messageRes);

    /**
     * Specifica l'azione da compiere quando il file è stato scaricato.
     */
    protected abstract void onSuccessAction(File localFile);

    private Context context;

    /**
     * Classe di utility che astrae il download di un file temporaneo e permette di fare un'azione con
     * il file scaricato.
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
     * @param project progetto a cui appartiene il file
     */
    public void downloadTempProjectFile(ManagerCloudFile file, Project project) {
        String internalFolderName = project.getId();
        File downloadedFile = FileDownloader.getDownloadedFile(file.getName(), project.getName());

        if (downloadedFile.exists() &&
                downloadedFile.length() == file.getSize()) {
            Log.i(TAG, "Using downloaded file");
            onSuccessAction(downloadedFile);
            return;
        }

        if (file.getSize() > MAX_TEMP_FILE_SIZE) {
            showDownloadFailedMessage(R.string.text_message_temp_file_too_big);
            return;
        }

        File path = new File(context.getFilesDir(), internalFolderName);
        if (!path.exists()) {
            path.mkdirs();
        }
        File localFile = new File(path, file.getName());

        downloadTempFile(file, localFile, 5000);
    }

    /**
     * Scarica il file in una cartella interna e quando finisce chiama onSuccessAction usando
     * il file scaricato o il file in memoria interna se già era presente.
     * Inoltre controlla se il file è stato già scaricato in precedenza nella cartella dei downloads
     * in memoria esterna, e se è presente lo utilizza.
     *
     * @param file il file da scaricare
     * @param studyCase caso di studio di cui il file fa parte
     */
    public void downloadTempStudyCaseFile (ManagerCloudFile file, StudyCase studyCase) {
        File path = new File(context.getFilesDir(), "studyCasesFile" + File.separator
                             + studyCase.getNome());
        if (!path.exists()) {
            path.mkdirs();
        }
        File localFile = new File(path, file.getName());

        downloadTempFile(file, localFile, 60 * 1000);
    }

    /**
     * @param maxWaitTime tempo in ms prima di cancellare il download e mostrare un consiglio
     *                   all'utente, settare a 0 per tempo infinito
     */
    private void downloadTempFile (ManagerCloudFile file, File localFile, int maxWaitTime) {
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

            if (maxWaitTime != 0) {
                // Se dopo la fine di maxWaitTime il file non è stato aperto, cancella l'operazione di download
                new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!fileDownloadTask.isComplete()) {
                            fileDownloadTask.cancel();
                            showDownloadFailedMessage(R.string.text_message_preview_taking_too_long);
                        }
                    }
                }, maxWaitTime);
            }
        }
    }
}
