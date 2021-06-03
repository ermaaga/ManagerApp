package it.uniba.di.sms2021.managerapp.enitities.file;

import java.util.Comparator;

import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;

/**
 * Ordina i file di un progetto mettendo al primo posto la release corrente e successivamente tutti
 * gli altri file ordinati per nome
 */
public class FileComparator implements Comparator<ManagerFile> {
    private String currentReleaseName;

    /**
     * Ordina i file di un progetto mettendo al primo posto la release corrente e successivamente tutti
     * gli altri file ordinati per nome
     * @param currentReleaseName il nome della release corrente del progetto o una stringa vuota
     *                           se non presente
     */
    public FileComparator (String currentReleaseName) {
        this.currentReleaseName = currentReleaseName;
    }

    @Override
    public int compare(ManagerFile o1, ManagerFile o2) {
        if (o1.getName().equals(currentReleaseName)) {
            return -1;
        } else if (o2.getName().equals(currentReleaseName)) {
            return 1;
        }

        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
    }
}
