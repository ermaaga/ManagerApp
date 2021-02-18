package it.uniba.di.sms2021.managerapp.firebase;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.ProjectPermissions;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.enitities.Vote;

/**
 * Classe di utility che popolerà tutte le informazioni utili per un progetto a partire da query
 * in firebase
 */
public class Project implements Parcelable {
    private Group group;
    private String studyCaseName;
    private String examName;
    private List<String> professorsId;

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

            //Inizializza il campo del nome dell'esame a cui appartiene il progetto
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean found = false;

                            for (DataSnapshot child: snapshot.getChildren()) {
                                if (child.getKey().equals(group.getExam())) {
                                    Exam exam = child.getValue(Exam.class);
                                    project.examName = exam.getName();
                                    project.professorsId = exam.getProfessors();
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                //TODO testare per fare in modo che questa eccezione non venga mai lanciata
                                throw new RuntimeException("Impossibile trovare l'esame con l'id "
                                        + group.getExam() + " nel progetto di id " + project.getId());
                            }

                            if (project.isInitialisationDone()) {
                                onProjectInitialised(project);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //Inizializza il campo del nome del caso di studio del progetto
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                throw new RuntimeException("Impossibile trovare il caso di studio con l'id "
                                        + group.getStudyCase() + " nel progetto di id " + project.getId());
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

    public Group getGroup () {
        return group;
    }

    public List<String> getProfessorsId() {
        return professorsId;
    }

    public void setProfessorsId(List<String> professorsId) {
        this.professorsId = professorsId;
    }

    public ProjectPermissions getPermissions() {
        return group.getPermissions();
    }

    public void setPermissions(ProjectPermissions permissions) {
        group.setPermissions(permissions);
    }

    public Vote getVote(){
        return group.getVote();
    }

    public void  setVote(Vote v){
        group.setVote(v);
    }

    @Exclude
    public boolean isGroupFull() {
        return group.isGroupFull();
    }

    /**
     * Ritorna true se l'utente corrente è il professore dell'esame che contiene il progetto
     */
    public boolean isProfessor () {
        return getProfessorsId().contains(LoginHelper.getCurrentUser().getAccountId());
    }

    /**
     * Ritorna true se l'utente corrente è un membro del progetto
     */
    public boolean isMember () {
        return getMembri().contains(LoginHelper.getCurrentUser().getAccountId());
    }

    /**
     * Ritorna true se l'utente corrente può aggiungere file al progetto
     */
    public boolean canAddFiles () {
        return getPermissions().getCanAddFiles().contains(LoginHelper.getCurrentUser().getAccountId());
    }

    /**
     * Ritorna true se tutti i campi sono stati inizializzati, false altrimenti
     */
    private boolean isInitialisationDone () {
        return studyCaseName != null && examName != null && professorsId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(group, project.group) &&
                Objects.equals(studyCaseName, project.studyCaseName) &&
                Objects.equals(examName, project.examName) &&
                Objects.equals(professorsId, project.professorsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, studyCaseName, examName, professorsId);
    }

    @Override
    public String toString() {
        return "Project{" +
                "group=" + group +
                ", studyCaseName='" + studyCaseName + '\'' +
                ", examName='" + examName + '\'' +
                ", professorsId=" + professorsId +
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
        dest.writeList(professorsId);
    }

    public static final Parcelable.Creator<Project> CREATOR
            = new Parcelable.Creator<Project>() {
        public Project createFromParcel(Parcel in) {
            Project project = new Project(in.readParcelable(Group.class.getClassLoader()));
            project.setStudyCaseName(in.readString());
            project.setExamName(in.readString());

            List<String> professorsId = new ArrayList<>();
            in.readList(professorsId, String.class.getClassLoader());
            project.setProfessorsId(professorsId);

            return project;
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
}
