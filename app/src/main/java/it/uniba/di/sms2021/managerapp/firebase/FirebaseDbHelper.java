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

    private static FirebaseDatabase INSTANCE;
    public static FirebaseDatabase getDBInstance() {
        if (INSTANCE == null) {
            INSTANCE = FirebaseDatabase.getInstance();
            INSTANCE.setPersistenceEnabled(true);
        }
        return INSTANCE;
    }

    public static DatabaseReference getGroupJoinRequestReference (String uid) {
        return getDBInstance().getReference(TABLE_GROUP_REQUESTS).child(uid);
    }

    public static DatabaseReference getUserJoinNoticeReference (String uid) {
        return getDBInstance().getReference(TABLE_GROUP_JOIN_NOTICE).child(uid);
    }
}
