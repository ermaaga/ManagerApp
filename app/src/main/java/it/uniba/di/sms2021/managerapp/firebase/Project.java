package it.uniba.di.sms2021.managerapp.firebase;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

/**
 * Classe di utility che popolerà tutte le informazioni utili per un progetto a partire da query
 * in firebase
 */
public class Project implements Parcelable {
    private Group group;
    private String studyCaseName;
    private String examName;

    public static final String KEY = "project";

    public static abstract class Initialiser {
        Project project;

        /**
         * Definire cosa fare una volta che il progetto è stato inizializzato
         */
        public abstract void onProjectInitialised (Project project);

        /**
         * Initializza il progetto con tutti i campi necessari a partire dal gruppo
         * @param group il gruppo associato al progetto
         */
        public void initialiseProject(Group group) {
            project = new Project(group);

            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean found = false;
                            for (DataSnapshot child: snapshot.getChildren()) {
                                if (child.getKey().equals(group.getExam())) {
                                    Exam exam = child.getValue(Exam.class);
                                    project.examName = exam.getName();
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                //TODO testare per fare in modo che questa eccezione non venga mai lanciata
                                throw new RuntimeException("Impossibile trovare l'esame con l'id "
                                        + group.getExam());
                            }

                            if (project.isInitialisationDone()) {
                                onProjectInitialised(project);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean found = false;
                            for (DataSnapshot child: snapshot.getChildren()) {
                                if (child.getKey().equals(group.getStudyCase())) {
                                    StudyCase studyCase = child.getValue(StudyCase.class);
                                    project.studyCaseName = studyCase.getNome();
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                //TODO testare per fare in modo che questa eccezione non venga mai lanciata
                                throw new RuntimeException("Impossibile trovare l'esame con l'id "
                                        + group.getExam());
                            }

                            if (project.isInitialisationDone()) {
                                onProjectInitialised(project);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private Project (Group group) {
        this.group = group;
    }

    public String getId() {
        return group.getId();
    }

    public void setId(String id) {
        group.setId(id);
    }

    public String getName() {
        return group.getName();
    }

    public void setName(String name) {
        group.setName(name);
    }

    public String getStudyCase() {
        return group.getStudyCase();
    }

    public void setStudyCase(String studyCase) {
        group.setStudyCase(studyCase);
    }

    public String getExam() {
        return group.getExam();
    }

    public void setExam(String exam) {
        group.setExam(exam);
    }

    public List<String> getMembri() {
        return group.getMembri();
    }

    public void setMembri(List<String> membri) {
        group.setMembri(membri);
    }

    public String getStudyCaseName() {
        return studyCaseName;
    }

    public String getExamName() {
        return examName;
    }

    public void setStudyCaseName(String studyCaseName) {
        this.studyCaseName = studyCaseName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    /**
     * Ritorna true se tutti i campi sono stati inizializzati, false altrimenti
     */
    private boolean isInitialisationDone () {
        return studyCaseName != null && examName != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(group, project.group) &&
                Objects.equals(studyCaseName, project.studyCaseName) &&
                Objects.equals(examName, project.examName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, studyCaseName, examName);
    }

    @Override
    public String toString() {
        return "Project{" +
                "group=" + group +
                ", caseStudyName='" + studyCaseName + '\'' +
                ", examName='" + examName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) group, 0);
        dest.writeString(studyCaseName);
        dest.writeString(examName);
    }

    public static final Parcelable.Creator<Project> CREATOR
            = new Parcelable.Creator<Project>() {
        public Project createFromParcel(Parcel in) {
            Project project = new Project(in.readParcelable(Group.class.getClassLoader()));
            project.setStudyCaseName(in.readString());
            project.setExamName(in.readString());

            return project;
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
}
