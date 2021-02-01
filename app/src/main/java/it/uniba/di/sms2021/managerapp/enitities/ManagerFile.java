package it.uniba.di.sms2021.managerapp.enitities;

import android.content.Context;
import android.net.Uri;
import android.text.format.Formatter;

import androidx.annotation.NonNull;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.utility.FileUtil;

/**
 * Entità che rappresenta un file caricato usando l'app
 */
public class ManagerFile {
    @NonNull
    StorageReference reference;
    String name;
    String type;
    Long size;

    /**
     *
     * @param reference il riferimento del file in firebase storage
     * @param name il nome completo del file inclusa l'estensione
     * @param type il tipo mime del file
     * @param size dimensione del file in byte
     */
    public ManagerFile(@NonNull StorageReference reference, String name, String type, Long size) {
        this.reference = reference;
        this.name = name;
        this.type = type;
        this.size = size;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerFile that = (ManagerFile) o;
        return reference.equals(that.reference) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, name, type, size);
    }

    @Override
    public String toString() {
        return "ManagerFile{" +
                "reference=" + reference +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                '}';
    }
}
