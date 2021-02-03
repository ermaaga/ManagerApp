package it.uniba.di.sms2021.managerapp.enitities;

import androidx.annotation.NonNull;

import com.google.firebase.storage.StorageReference;

import java.util.Objects;

/**
 * Entità che rappresenta un file caricato usando l'app
 */
public class ManagerFile {
    @NonNull
    StorageReference reference;
    String name;
    String type;
    Long size;
    Long lastUpdateTime;

    /**
     *
     * @param reference il riferimento del file in firebase storage
     * @param name il nome completo del file inclusa l'estensione
     * @param type il tipo mime del file
     * @param size dimensione del file in byte
     * @param lastUpdateTime tempo dall'ultimo aggiornamento in ms
     */
    public ManagerFile(@NonNull StorageReference reference, String name, String type, Long size, Long lastUpdateTime) {
        this.reference = reference;
        this.name = name;
        this.type = type;
        this.size = size;
        this.lastUpdateTime = lastUpdateTime;
    }

    @NonNull
    public StorageReference getReference() {
        return reference;
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutExtension() {
        return name.replaceAll("[.].{2,4}$", "");
    }

    public String getType() {
        return type;
    }

    public Long getSize() {
        return size;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerFile that = (ManagerFile) o;
        return reference.equals(that.reference) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(size, that.size) &&
                Objects.equals(lastUpdateTime, that.lastUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, name, type, size, lastUpdateTime);
    }

    @Override
    public String toString() {
        return "ManagerFile{" +
                "reference=" + reference +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", creationTime=" + lastUpdateTime +
                '}';
    }
}
