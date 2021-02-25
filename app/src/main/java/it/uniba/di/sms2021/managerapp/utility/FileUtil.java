package it.uniba.di.sms2021.managerapp.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;

import it.uniba.di.sms2021.managerapp.BuildConfig;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;

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
         */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }

    public static Long getFileSizeFromURI (Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         * Got from https://developer.android.com/training/secure-file-sharing/retrieve-info.html
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
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.image));
        } else if (fileType.equals("application/pdf")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pdf));
        } else if (fileType.contains("audio/")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.audio));
        } else if (fileType.contains("video/")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.video));
        } else if (fileType.contains("text/plain")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.text));
        } else if (fileType.contains("application/vnd.android.package-archive")) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.apk));
        } else {
            Log.i(TAG, "Tipo " + fileType + " non supportato.");
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.file));
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
     * @return true se il file è apribile, false altrimenti
     */
    public static boolean openFileWithViewIntent(Context context, Uri uri, String mimeType) {
        // I file apk devono prima essere scaricati
        if (mimeType.equals(("application/vnd.android.package-archive"))) {
            return false;
        }

        Intent intent = getFileViewIntent(context, uri, mimeType);

        if (intent != null) {
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public static Intent getFileViewIntent (Context context, Uri uri, String mimeType) {
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
            return null;
        }
    }

}
