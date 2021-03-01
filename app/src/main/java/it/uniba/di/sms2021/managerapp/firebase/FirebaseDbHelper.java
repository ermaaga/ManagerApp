package it.uniba.di.sms2021.managerapp.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDbHelper {
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXAMS = "exams";
    public static final String TABLE_STUDYCASES = "studycases";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_DEPARTMENTS = "departments";
    public static final String TABLE_COURSES = "courses";

    private static final String TABLE_GROUP_REQUESTS = "group_requests";
    private static final String TABLE_GROUP_JOIN_NOTICE = "group_join_notice";
    private static final String TABLE_EXAM_REQUESTS = "exam_requests";
    private static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_NEW_EVALUATION = "new_evalutation";

    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_REPORTS = "reports";

    private static final String TABLE_PENDING_REQUESTS = "pending_requests";

    private static FirebaseDatabase INSTANCE;
    public static FirebaseDatabase getDBInstance() {
        if (INSTANCE == null) {
            INSTANCE = FirebaseDatabase.getInstance();
            INSTANCE.setPersistenceEnabled(true);
        }
        return INSTANCE;
    }

    public static DatabaseReference getGroupJoinRequestReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS + "/" + TABLE_GROUP_REQUESTS).child(uid);
    }

    public static DatabaseReference getUserJoinNoticeReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS + "/" +  TABLE_GROUP_JOIN_NOTICE).child(uid);
    }

    public static DatabaseReference getNewEvaluationReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS + "/" +  TABLE_NEW_EVALUATION).child(uid);
    }

    public static DatabaseReference getExamJoinRequestReference (String uid) {
        return getDBInstance().getReference(TABLE_NOTIFICATIONS + "/" + TABLE_EXAM_REQUESTS).child(uid);
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
}
