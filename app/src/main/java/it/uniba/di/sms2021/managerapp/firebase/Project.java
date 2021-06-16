package it.uniba.di.sms2021.managerapp.firebase;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.uniba.di.sms2021.managerapp.enitities.Evaluation;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.ProjectPermissions;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.projects.ProjectFilesFragment;

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

    public Evaluation getEvaluation(){
        return group.getEvaluation();
    }

    public void setEvaluation(Evaluation v){
        group.setEvaluation(v);
    }

    public List<String> getReleaseNames() {
        return group.getReleaseNames();
    }

    public void setReleaseNames(List<String> releaseNames) {
        group.setReleaseNames(releaseNames);
    }

    public boolean hasReleases() {
        return !group.getReleaseNames().isEmpty();
    }

    /**
     * Aggiunge un file marcato come rilascio ed aggiorna l'informazione sul db
     * @param releaseName nome del file
     */
    public void addReleaseName (String releaseName) {
        group.getReleaseNames().add(releaseName);
        updateReleaseNames(group.getReleaseNames());
    }

    /**
     * Rimuove un file marcato come rilascio ed aggiorna l'informazione sul db
     * @param releaseName nome del file
     */
    public void removeReleaseName (String releaseName) {
        group.getReleaseNames().remove(releaseName);
        updateReleaseNames(group.getReleaseNames());
    }

    /**
     * Ritorna il numero di rilascio del file
     * @param releaseName il nome del file
     * @return il numero di rilascio o 0 se non esiste
     */
    public int getReleaseNumber (String releaseName) {
        return group.getReleaseNames().indexOf(releaseName) + 1;
    }

    /**
     * Ritorna il nome dell'ultimo file marcato come release o una stringa vuota se non ci sono file
     * marcati come release.
     */
    public String getCurrentReleaseName () {
        if (!group.getReleaseNames().isEmpty()) {
            return group.getReleaseNames().get(group.getReleaseNames().size() - 1);
        } else {
            return "";
        }

    }

    private void updateReleaseNames (List<String> releaseNames) {
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS).child(group.getId())
                .child(Group.Keys.RELEASE_NAMES).setValue(releaseNames);
    }

    public List<String> getWhoPrefers() {
        return group.getWhoPrefers();
    }

    public void setWhoPrefers(List<String> whoPrefers) {
        group.setWhoPrefers(whoPrefers);
    }

    public boolean isEvaluated() {
        return getEvaluation() != null;
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
     * Ritorna true se l'utente corrente è il creatore del progetto
     */
    public boolean isCreator() {
        return getMembri().get(0).contains(LoginHelper.getCurrentUser().getAccountId());
    }

    /**
     * Ritorna true se l'utente corrente preferisce il progetto
     */
    public boolean isPreferred () {
       return getWhoPrefers().contains(LoginHelper.getCurrentUser().getAccountId());
    }

    /**
     * Ritorna true se l'utente corrente può aggiungere file al progetto
     * Il leader del gruppo può sempre aggiungere files
     */
    public boolean canAddFiles () {
        return LoginHelper.getCurrentUser().getAccountId().equals(getMembri().get(0)) ||
                getPermissions().getCanAddFiles().contains(LoginHelper.getCurrentUser().getAccountId());
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
        dest.writeParcelable(group, 0);
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
