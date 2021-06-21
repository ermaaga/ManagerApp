package it.uniba.di.sms2021.managerapp.utility;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;

import it.uniba.di.sms2021.managerapp.BuildConfig;
import it.uniba.di.sms2021.managerapp.R;

public class FileUtil {
    private static final String TAG = "FileUtil";

    private FileUtil () { }

    public static String getFileNameFromURI (Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         * Got from https://developer.android.com/training/secure-file-sharing/retrieve-info.html
         * todo rimuovere commmento o tradurlo
         */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }

    /**
     * Ottiene la dimensione di un file a partire dal suo content uri
     * @param uri uri della forma "content:"
     * @return dimensione in byte del file
     */
    public static Long getFileSizeFromURI (Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         * Got from https://developer.android.com/training/secure-file-sharing/retrieve-info.html
         * todo rimuovere commento o tradurlo
         */
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        Long fileSize = returnCursor.getLong(sizeIndex);
        returnCursor.close();
        return fileSize;
    }

    public static String getMimeTypeFromUri (Context context, Uri uri) {
        /*
         * Get the file's content URI from the incoming Intent, then
         * get the file's MIME type
         */
        return context.getContentResolver().getType(uri);
    }

    public static String getFormattedSize (Context context, Long size) {
        return Formatter.formatFileSize(context, size);
    }

    /*Questo metodo era in FilesRecyclerAdapter ed è stato spostato qui perchè
     * potrebbe essere utile in altre classi.
     */
    public static void setTypeImageView (Context context, ImageView imageView, String fileType) {
        if (fileType.contains("image/")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_image));
        } else if (fileType.equals("application/pdf")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pdf));
        } else if (fileType.contains("audio/")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_audio));
        } else if (fileType.contains("video/")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_video));
        } else if (fileType.contains("text/plain")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_text));
        } else if (fileType.contains("application/vnd.android.package-archive")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_apk));
        } else {
            Log.i(TAG, "Tipo " + fileType + " non supportato.");
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file));
        }
    }

    /**
     * Ritorna l'uri del file se presente nella memoria interna dell'app o esterna.
     * Per far sì che accetti altre locazioni, modificare il file provider_paths.xml passato al
     * provider nel manifest.
     */
    public static Uri getUriFromFile (Context context, File localFile) {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", localFile);
    }

    /**
     * Apre un file usando una della applicazioni installate.
     * @param uri l'uri del file in memoria da aprire
     * @param mimeType il tipo mime del file
     * @throws FileException ha errorCode "NO_INTENT_FOUND" se non è stato trovato alcun intent
     *      capace di aprire la tipologia di file.<br>
     *          ha errorCode "NO_STORAGE_ACCESS" se l'app non dispone dei permessi di storage
     */
    public static void openFileWithViewIntent(Context context, Uri uri, String mimeType) throws FileException {
        if (requiresStorageAccess(context)) {
            throw new FileException(FileException.NO_STORAGE_ACCESS);
        }

        if (mimeType.equals(("application/vnd.android.package-archive"))) {
            openApkWithPackageManager(context, uri);
            return;
        }

        try {
            Intent intent = getFileViewIntent(context, uri, mimeType);
            context.startActivity(intent);
        } catch (FileException e) {
            throw e;
        }
    }

    private static void openApkWithPackageManager(Context context, Uri uri) {
        Intent installationIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installationIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installationIntent.setData(uri);
            installationIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            installationIntent = new Intent(Intent.ACTION_VIEW);
            installationIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            installationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(installationIntent);
    }

    @Nullable
    public static Intent getFileViewIntent (Context context, Uri uri, String mimeType) throws FileException {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //Crea un intent chooser per permettere all'utente di scegliere l'app per visualizzare il file
        String title = context.getString(R.string.chooser_title_preview);
        Intent chooser = Intent.createChooser(intent, title);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return chooser;
        } else {
            throw new FileException(FileException.NO_INTENT_FOUND);
        }
    }

    public static boolean requiresStorageAccess (Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;
        } else {
            return false;
        }
    }

}