package it.uniba.di.sms2021.managerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.lists.RecyclerViewArrayAdapter;

public class DegreeCoursesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private int userRole;

    private FirebaseDatabase database;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_degree_courses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.degreeCoursesRecyclerView);

        userRole = getIntent().getIntExtra(UserRoleActivity.USER_ROLE, 0);

        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        usersReference.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        RecyclerViewArrayAdapter adapter = new RecyclerViewArrayAdapter(getResources()
                .getStringArray(R.array.list_degree_courses), new RecyclerViewArrayAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(String item) {
                submitUser(item);

                Intent intent = new Intent(DegreeCoursesActivity.this, HomeActivity.class);

                // Pulisce il backstack delle activity (un utente non dovrebbe navigare indietro
                // fino al login)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                //Uso la classe esplicitamente perchè "this" si riferirebbe al listener
                DegreeCoursesActivity.this.finish();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void submitUser (String userDegreeCourse) {
        int course = getUserCourseFromString(userDegreeCourse);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        User user = new User(account.getId(), account.getGivenName(), account.getFamilyName(),
                account.getEmail(), userRole, course);
        usersReference.child(account.getId()).setValue(user);
    }

    private int getUserCourseFromString (String course) {
        if (course.equals(getString(R.string.list_degree_courses_informatica))) {
            return User.COURSE_INFORMATICA;
        } else if (course.equals(getString(R.string.list_degree_courses_informatica))) {
            return User.COURSE_ITPS;
        } else {
            throw new IllegalStateException("Aggiungere i corsi nel metodo, rispettando quanti" +
                    "corsi possono essere scelti nell'app.");
        }
    }
}