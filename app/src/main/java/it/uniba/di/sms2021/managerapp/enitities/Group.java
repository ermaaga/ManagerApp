package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group implements Parcelable {
    private String id;
    private String name;
    private String studyCase;
    private String exam;
    private List<String> membri;

    public Group(String id, String name, String studyCase, String exam, List<String> membri) {
        this.id = id;
        this.name = name;
        this.studyCase = studyCase;
        this.exam = exam;
        this.membri = membri;
    }

    public Group() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudyCase() {
        return studyCase;
    }

    public void setStudyCase(String studyCase) {
        this.studyCase = studyCase;
    }

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public List<String> getMembri() {
        return membri;
    }

    public void setMembri(List<String> membri) {
        this.membri = membri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id) &&
                Objects.equals(name, group.name) &&
                Objects.equals(studyCase, group.studyCase) &&
                Objects.equals(exam, group.exam) &&
                Objects.equals(membri, group.membri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, studyCase, exam, membri);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", studyCase='" + studyCase + '\'' +
                ", exam='" + exam + '\'' +
                ", membri=" + membri +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(studyCase);
        dest.writeString(exam);
        dest.writeList(membri);
    }

    public static final Parcelable.Creator<Group> CREATOR
            = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            Group group = new Group();
            group.setId(in.readString());
            group.setName(in.readString());
            group.setStudyCase(in.readString());
            group.setStudyCase(in.readString());

            List<String> membri = new ArrayList<>();
            in.readList(membri, String.class.getClassLoader());
            group.setMembri(membri);

            return group;
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
    public interface Keys{
        String EXAM = "exam";
        String ID = "id";
        String NAME = "name";
        String STUDYCASE = "studyCase";
        String MEMBERS = "membri";
    }
}
