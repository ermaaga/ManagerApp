package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectPermissions implements Parcelable {
    private boolean accessible;

    public ProjectPermissions() {
        this.accessible = false;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(accessible ? 1 : 0);
    }

    public static final Parcelable.Creator<ProjectPermissions> CREATOR
            = new Parcelable.Creator<ProjectPermissions>() {
        public ProjectPermissions createFromParcel(Parcel in) {
            ProjectPermissions permissions = new ProjectPermissions();
            permissions.setAccessible(in.readInt() == 1);

            return permissions;
        }

        public ProjectPermissions[] newArray(int size) {
            return new ProjectPermissions[size];
        }
    };
}
