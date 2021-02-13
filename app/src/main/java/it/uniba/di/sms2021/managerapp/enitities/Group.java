package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group implements Parcelable {
    private String id;
    private String name;
    /**
     * L'id del caso di studio a cui è associato
     */
    private String studyCase;

    /**
     * L'id dell'esame a cui è associato
     */
    private String exam;

    private List<String> membri;
    private ProjectPermissions permissions;

    // Campo duplicato per usarlo in ExamGroupsFragment senza rompere il resto del programma
    // TODO usare project al posto di questo campo
    private String studyCaseName;
    private Vote vote;

    public Group(String id, String name, String studyCase, String exam, List<String> membri) {
        this();
        this.id = id;
        this.name = name;
        this.studyCase = studyCase;
        this.exam = exam;
        this.membri = membri;
    }

    public Group() {
        setPermissions(null);
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

    @Exclude
    public String getStudyCaseName() {
        return studyCaseName;
    }

    @Exclude
    public void setStudyCaseName(String studyCaseName) {
        this.studyCaseName = studyCaseName;
    }

    public ProjectPermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(ProjectPermissions permissions) {
        if (permissions == null) {
            this.permissions = new ProjectPermissions();
        } else {
            this.permissions = permissions;
        }
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
    }

    @Exclude
    public boolean isGroupFull () {
        return permissions.getMaxMembers() != 0 && permissions.getMaxMembers() - membri.size() <= 0;
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
                Objects.equals(membri, group.membri) &&
                Objects.equals(permissions, group.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, studyCase, exam, membri, permissions);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", studyCase='" + studyCase + '\'' +
                ", exam='" + exam + '\'' +
                ", membri=" + membri +
                ", permissions=" + permissions +
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
        dest.writeParcelable(permissions, 0);
    }

    public static final Parcelable.Creator<Group> CREATOR
            = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            Group group = new Group();
            group.setId(in.readString());
            group.setName(in.readString());
            group.setStudyCase(in.readString());
            group.setExam(in.readString());

            List<String> membri = new ArrayList<>();
            in.readList(membri, String.class.getClassLoader());
            group.setMembri(membri);

            group.setPermissions(in.readParcelable(ProjectPermissions.class.getClassLoader()));

            return group;
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
    public interface Keys{
        String GROUP = "group";
        String EXAM = "exam";
        String ID = "id";
        String NAME = "name";
        String STUDYCASE = "studyCase";
        String MEMBERS = "membri";
    }
}
