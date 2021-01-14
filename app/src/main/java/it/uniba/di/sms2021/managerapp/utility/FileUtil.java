package it.uniba.di.sms2021.managerapp.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.format.Formatter;

public class FileUtil {
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
        return returnCursor.getString(nameIndex);
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
        return returnCursor.getLong(sizeIndex);
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

}
