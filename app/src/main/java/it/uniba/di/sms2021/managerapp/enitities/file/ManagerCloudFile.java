package it.uniba.di.sms2021.managerapp.enitities.file;

import androidx.annotation.NonNull;

import com.google.firebase.storage.StorageReference;

import java.util.Objects;

/**
 * Entità che rappresenta un file caricato usando l'app
 */
public class ManagerCloudFile extends AbstractManagerFile {
    @NonNull
    private final StorageReference reference;

    /**
     *
     * @param reference il riferimento del file in firebase storage
     * @param name il nome completo del file inclusa l'estensione
     * @param type il tipo mime del file
     * @param size dimensione del file in byte
     * @param lastUpdateTime tempo dall'ultimo aggiornamento in ms
     */
    public ManagerCloudFile(@NonNull StorageReference reference, String name, String type, Long size, Long lastUpdateTime) {
        super(name, type, size, lastUpdateTime);
        this.reference = reference;
    }

    @NonNull
    public StorageReference getReference() {
        return reference;
    }

    @Override
    public boolean isLocalFile() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ManagerCloudFile that = (ManagerCloudFile) o;
        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reference);
    }
}
