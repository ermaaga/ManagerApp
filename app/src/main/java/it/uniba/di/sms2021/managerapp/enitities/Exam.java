package it.uniba.di.sms2021.managerapp.enitities;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;

public class Exam {
    private String name;
    private List<String> professors;
    private List<String> students;
    private int year;

    public Exam() {
    }

    public Exam(String name, List<String> professors, List<String> students, int year) {
        this.name = name;
        this.professors = professors;
        this.students = students;
        this.year = year;
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

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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
                Objects.equals(students, exam.students);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, professors, students, year);
    }
}
