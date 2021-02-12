package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectPermissions implements Parcelable {
    private boolean accessible;
    private boolean joinable;
    private int maxMembers;

    public ProjectPermissions() {
        // Setta i valori di default
        this.accessible = false;
        this.joinable = true;
        this.maxMembers = 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Uso writeInt perchè writeBoolean è disponibile solo dall'api 29
        dest.writeInt(accessible ? 1 : 0);
        dest.writeInt(joinable ? 1 : 0);
    }

    public static final Parcelable.Creator<ProjectPermissions> CREATOR
            = new Parcelable.Creator<ProjectPermissions>() {
        public ProjectPermissions createFromParcel(Parcel in) {
            ProjectPermissions permissions = new ProjectPermissions();
            permissions.setAccessible(in.readInt() == 1);
            permissions.setJoinable(in.readInt() == 1);

            return permissions;
        }

        public ProjectPermissions[] newArray(int size) {
            return new ProjectPermissions[size];
        }
    };
}
