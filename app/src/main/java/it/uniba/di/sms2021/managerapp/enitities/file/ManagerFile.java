package it.uniba.di.sms2021.managerapp.enitities.file;

public interface ManagerFile {
    String getName();
    String getNameWithoutExtension();
    String getType();
    Long getSize();
    Long getLastUpdateTime();
    boolean equals(Object o);
    boolean isLocalFile();
}
