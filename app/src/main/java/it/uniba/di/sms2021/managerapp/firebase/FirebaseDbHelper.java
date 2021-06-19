package it.uniba.di.sms2021.managerapp.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

public class FirebaseDbHelper {
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXAMS = "exams";
    public static final String TABLE_STUDYCASES = "studycases";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_DEPARTMENTS = "departments";
    public static final String TABLE_COURSES = "courses";

    public static final String TABLE_GROUP_JOIN_REQUESTS = "group_requests";
    public static final String TABLE_GROUP_JOIN_NOTICE = "group_join_notice";
    public static final String TABLE_EXAM_JOIN_REQUESTS = "exam_requests";
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_NEW_EVALUATION = "new_evalutation";

    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_REPORTS = "reports";
    public static final String TABLE_REPLIES_REVIEW = "replies_review";
    public static final String TABLE_REPLIES_REPORT = "replies_report";
    public static final String TABLE_NEW_REPORT = "new_report";
    public static final String TABLE_NEW_REPLY_REPORT = "new_reply_report";
    public static final String TABLE_PENDING_REQUESTS = "pending_requests";

    public static final String TABLE_LISTS_PROJECTS= "lists_projects";
    public static final String TABLE_RECEIVED_PROJECT_LISTS = "received_project_lists";
    public static final String TABLE_FAVOURITE_PROJECTS = "favourite_projects";
    public static final String TABLE_TRIED_PROJECTS = "tried_projects";
    public static final String TABLE_EVALUATED_PROJECTS = "evaluated_projects";

    public static final String GROUPS_FOLDER = "Groups";

    private static FirebaseDatabase INSTANCE;
    public static FirebaseDatabase getDBInstance() {
        if (INSTANCE == null) {
            INSTANCE = FirebaseDatabase.getInstance();
            INSTANCE.setPersistenceEnabled(true);
        }
        return INSTANCE;
    }

    public static DatabaseReference getNotifications (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid);
    }

    public static DatabaseReference getExamJoinRequestReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid).child(TABLE_EXAM_JOIN_REQUESTS);
    }

    public static DatabaseReference getGroupUserJoinNoticeReference(String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid).child(TABLE_GROUP_JOIN_NOTICE);
    }

    public static DatabaseReference getGroupJoinRequestReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid).child(TABLE_GROUP_JOIN_REQUESTS);
    }

    public static DatabaseReference getNewEvaluationReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid).child(TABLE_NEW_EVALUATION);
    }

    public static DatabaseReference getNewReplyReportReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid).child(TABLE_NEW_REPLY_REPORT);
    }

    public static DatabaseReference getNewReportReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS).child(uid).child(TABLE_NEW_REPORT);
    }

    public static DatabaseReference getListsProjectsReference (String uid) {
        return getDBInstance().getReference(TABLE_LISTS_PROJECTS).child(uid);
    }

    public static DatabaseReference getReceivedProjectListsReference (String uid) {
        return getListsProjectsReference(uid).child(TABLE_RECEIVED_PROJECT_LISTS);
    }

    public static DatabaseReference getFavouriteProjectsReference (String uid) {
        return getListsProjectsReference(uid).child(TABLE_FAVOURITE_PROJECTS);
    }

    public static DatabaseReference getTriedProjectsReference (String uid) {
        return getListsProjectsReference(uid).child(TABLE_TRIED_PROJECTS);
    }

    public static DatabaseReference getEvaluatedProjectsReference (String uid) {
        return getListsProjectsReference(uid).child(TABLE_EVALUATED_PROJECTS);
    }

    /**
     * Struttura: "pending_requests/[user id]/exams/([exam id], [true])
     * Uso la struttura (chiave, true) poichè mi interessa solo del valore della chiave, così come
     * consigliato in: https://firebase.google.com/docs/database/web/structure-data#fanout
     *
     * Serve per sapere se un'utente abbia già chiesto l'accesso ad un'esame ed attende risposta
     * dal professore.
     */
    public static DatabaseReference getPendingExamRequests (String uid) {
        return getDBInstance().getReference(TABLE_PENDING_REQUESTS).child(uid).child("exams");
    }

    public static StorageReference getStudyCaseFileReference(StudyCase studyCase,
                                                             String fileName) {
        return getOldStudyCasePathReference(studyCase).child(fileName);
    }

    //TODO rimuovere dopo aver aggiunto dati dummy
    public static StorageReference getOldStudyCasePathReference(StudyCase studyCase) {
        return FirebaseStorage.getInstance().getReference().child("Exam" + studyCase.getEsame())
                .child("StudyCase" + studyCase.getId());
    }

    public static StorageReference getStudyCasePathReference(StudyCase studyCase) {
        return FirebaseStorage.getInstance().getReference("StudyCases")
                .child("Exam" + studyCase.getEsame()).child("StudyCase" + studyCase.getId());
    }

    public static void addProjectToTriedProjects(Project project) {
        DatabaseReference reference = getTriedProjectsReference(LoginHelper.getCurrentUser().getAccountId());
        reference.child(project.getId()).setValue(true);
    }
}
