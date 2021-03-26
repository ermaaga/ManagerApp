package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListProjects implements Parcelable {
    private String idList;
    private String nameList;
    private List<String> idProjects;

    public static final String KEY = "ListProjects";

    public ListProjects() {

    }

    public ListProjects(String idList, String nameList, List<String> idProjects) {
        this.idList = idList;
        this.nameList = nameList;
        this.idProjects = idProjects;
    }

    public String getIdList() {
        return idList;
    }

    public void setIdList(String idList) {
        this.idList = idList;
    }

    public String getNameList() {
        return nameList;
    }

    public void setNameList(String nameList) {
        this.nameList = nameList;
    }

    public List<String> getIdProjects() {
        return idProjects;
    }

    public void setIdProjects(List<String> idProjects) {
        this.idProjects = idProjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListProjects that = (ListProjects) o;
        return Objects.equals(idList, that.idList) &&
                Objects.equals(nameList, that.nameList) &&
                Objects.equals(idProjects, that.idProjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idList, nameList, idProjects);
    }

    @Override
    public String toString() {
        return "ListProjects{" +
                "idList='" + idList + '\'' +
                ", nameList='" + nameList + '\'' +
                ", projects=" + idProjects +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idList);
        dest.writeString(nameList);
        dest.writeList(idProjects);
    }

    public static final Parcelable.Creator<ListProjects> CREATOR
            = new Parcelable.Creator<ListProjects>() {
        public ListProjects createFromParcel(Parcel in) {
            ListProjects list= new ListProjects();
            list.setIdList(in.readString());
            list.setNameList(in.readString());

            List<String> idProjects = new ArrayList<>();
            in.readList(idProjects, String.class.getClassLoader());
            list.setIdProjects(idProjects);

            return list;
        }

        public ListProjects[] newArray(int size) {
            return new ListProjects[size];
        }
    };
}
