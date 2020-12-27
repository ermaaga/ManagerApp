package it.uniba.di.sms2021.managerapp.db;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

public class FirebaseDbHelper {
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXAMS = "exams";
    public static final String TABLE_STUDYCASES = "studycases";

    private static FirebaseDatabase INSTANCE;
    public static FirebaseDatabase getDBInstance() {
        if (INSTANCE == null) {
            INSTANCE = FirebaseDatabase.getInstance();
            INSTANCE.setPersistenceEnabled(true);
        }
        return INSTANCE;
    }
}
