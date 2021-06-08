package it.uniba.di.sms2021.managerapp.utility;

import androidx.annotation.StringRes;

import java.security.PrivilegedActionException;

import it.uniba.di.sms2021.managerapp.R;

/**
 * Un errore generato durante l'uso di un file gestito da ManagerFile o uno dei suoi derivati.<br><br>
 * I codici di errori disponibili sono:
 *     <ul>NO_INTENT_FOUND: segnala che non è stato trovato un intent in grado di aprire il tipo di file</ul>
 */
public class FileException extends Exception {

    public static final int NO_INTENT_FOUND = 1;
    public static final int NO_STORAGE_ACCESS = 2;

    private final int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public FileException(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public FileException(int errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
}
