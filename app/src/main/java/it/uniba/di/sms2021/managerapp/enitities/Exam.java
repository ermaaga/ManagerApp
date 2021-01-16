package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exam implements Parcelable {
    //Aggiungo l'id per rendere più facile la comunicazione del dato tra activity
    private String id;

    private String name;
    private List<String> professors;
    @Nullable
    private List<String> students;
    @Nullable
    private List<String> studyCases;
    private int year;

    public Exam() {
    }

    public Exam(String id, String name, List<String> professors, @Nullable List<String> students, int year) {
        this.id = id;
        this.name = name;
        this.professors = professors;
        this.students = students;
        this.year = year;
    }

    public String getId() {
        return id;
    }

    //Deve esistere per firebase, ma non dovrebbe mai essere usato nell'applicazione
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getProfessors() {
        return professors;
    }

    public void setProfessors(List<String> professors) {
        this.professors = professors;
    }

    @Nullable
    public List<String> getStudents() {
        return students;
    }

    public void setStudents(@Nullable List<String> students) {
        this.students = students;
    }

    public void addStudent(String s) {
        if (students != null) {
            students.add(s);
        }
    }

    public void removeStudent(@Nullable String studentId) {
        if (students != null) {
            students.remove(studentId);
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Nullable
    public List<String> getStudyCases () {
        return studyCases;
    }

    public void addStudyCase (StudyCase studyCase) {
        if (studyCases == null) {
            studyCases = new ArrayList<>();
        }

        studyCases.add(studyCase.getId());
    }

    public void removeStudyCase (StudyCase studyCase) {
        if (studyCases != null) {
            studyCases.remove(studyCase.getId());
        }
    }

    public void emptyStudyCases () {
        if (studyCases != null) {
            studyCases.clear();
        }
    }

    public void setStudyCases (@Nullable List<String> studyCases) {
        this.studyCases = studyCases;
    }

    //TODO reimplementare equals con i parametri che servono
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return year == exam.year &&
                Objects.equals(name, exam.name) &&
                Objects.equals(professors, exam.professors) &&
                Objects.equals(students, exam.students) &&
                Objects.equals(studyCases, exam.studyCases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, professors, students, studyCases, year);
    }

    @Override
    public String toString() {
        return "Exam{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", professors=" + professors +
                ", students=" + students +
                ", studyCases=" + studyCases +
                ", year=" + year +
                ", describeContents=" + describeContents() +
                '}';
    }

    // Sezione con metodi di parcelizzazione (serializzazione ottimizzata per android)
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeList(professors);
        parcel.writeList(students);
        parcel.writeList(studyCases);
        parcel.writeInt(year);
    }

    public static final Parcelable.Creator<Exam> CREATOR
            = new Parcelable.Creator<Exam>() {
        public Exam createFromParcel(Parcel in) {
            Exam exam = new Exam();
            exam.setId(in.readString());
            exam.setName(in.readString());

            List<String> professors = new ArrayList<>();
            in.readList(professors, String.class.getClassLoader());
            exam.setProfessors(professors);

            List<String> students = new ArrayList<>();
            in.readList(students, String.class.getClassLoader());
            exam.setStudents(students);

            List<String> studyCases = new ArrayList<>();
            in.readList(studyCases, String.class.getClassLoader());
            exam.setStudyCases(studyCases);

            exam.setYear(in.readInt());

            return exam;
        }

        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };

    public interface Keys{
        String ID = "id";
        String NAME = "name";
        String PROFESSORS = "professors";
        String STUDENTS = "students";
        String YEAR = "year";
    }
}
