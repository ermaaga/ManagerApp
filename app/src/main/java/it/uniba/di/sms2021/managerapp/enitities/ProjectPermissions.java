package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProjectPermissions implements Parcelable {
    private boolean accessible;
    private boolean joinable;
    private int maxMembers;
    private boolean fileAccessible;
    // Insieme dei membri che possono aggiungere file
    private List<String> canAddFiles;

    public ProjectPermissions() {
        // Setta i valori di default
        this.accessible = false;
        this.joinable = true;
        this.maxMembers = 0;
        canAddFiles = new ArrayList<>();
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public boolean isJoinable() {
        return joinable;
    }

    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    /**
     * Ritorna il numero massimo dei membri o 0 se illimitato.
     */
    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public boolean isFileAccessible() {
        return fileAccessible;
    }

    public void setFileAccessible(boolean fileAccessible) {
        this.fileAccessible = fileAccessible;
    }

    public List<String> getCanAddFiles() {
        return canAddFiles;
    }

    public void setCanAddFiles(List<String> canAddFiles) {
        this.canAddFiles = canAddFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectPermissions that = (ProjectPermissions) o;
        return accessible == that.accessible &&
                joinable == that.joinable &&
                maxMembers == that.maxMembers &&
                fileAccessible == that.fileAccessible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessible, joinable, maxMembers, fileAccessible);
    }

    @Override
    public String toString() {
        return "ProjectPermissions{" +
                "accessible=" + accessible +
                ", joinable=" + joinable +
                ", maxMembers=" + maxMembers +
                ", fileAccessible=" + fileAccessible +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Uso writeInt perchè writeBoolean è disponibile solo dall'api 29
        dest.writeInt(accessible ? 1 : 0);
        dest.writeInt(joinable ? 1 : 0);
        dest.writeInt(maxMembers);
        dest.writeInt(fileAccessible ? 1 : 0);
        dest.writeList(canAddFiles);
    }

    public static final Parcelable.Creator<ProjectPermissions> CREATOR
            = new Parcelable.Creator<ProjectPermissions>() {
        public ProjectPermissions createFromParcel(Parcel in) {
            ProjectPermissions permissions = new ProjectPermissions();
            permissions.setAccessible(in.readInt() == 1);
            permissions.setJoinable(in.readInt() == 1);
            permissions.setMaxMembers(in.readInt());
            permissions.setFileAccessible(in.readInt() == 1);

            List<String> canAddFiles = new ArrayList<>();
            in.readList(canAddFiles, String.class.getClassLoader());
            permissions.setCanAddFiles(canAddFiles);

            return permissions;
        }

        public ProjectPermissions[] newArray(int size) {
            return new ProjectPermissions[size];
        }
    };
}
