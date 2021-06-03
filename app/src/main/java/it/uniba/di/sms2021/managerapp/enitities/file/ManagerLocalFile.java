package it.uniba.di.sms2021.managerapp.enitities.file;

import androidx.annotation.NonNull;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

/**
 * Rappresenta un file scaricato in precedenza sulla memoria del dispositivo a partire da un file
 * presente sul cloud di firebase.<br>
 * Vengono usati per mostrare i file presenti sul dispositivo qualora la connessione mancasse.
 */
public class ManagerLocalFile extends AbstractManagerFile {
    @NonNull
    private final File localFile;

    /**
     * @param localFile il file nella memoria interna
     * @param name il nome completo del file inclusa l'estensione
     * @param type il tipo mime del file
     * @param size dimensione del file in byte
     * @param lastUpdateTime tempo dall'ultimo aggiornamento in ms
     */
    public ManagerLocalFile(@NonNull File localFile, String name, String type, Long size, Long lastUpdateTime) {
        super (name, type, size, lastUpdateTime);
        this.localFile = localFile;
    }

    @NonNull
    public File getLocalFile() {
        return localFile;
    }

    @Override
    public boolean isLocalFile() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ManagerLocalFile that = (ManagerLocalFile) o;
        return localFile.equals(that.localFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), localFile);
    }
}
