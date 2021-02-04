package it.uniba.di.sms2021.managerapp.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.home.HomeActivity;
import it.uniba.di.sms2021.managerapp.lists.RecyclerViewArrayAdapter;

public class DegreeCoursesActivity extends AppCompatActivity {
    private static final String TAG = "DegreeCoursesActivity";
    private RecyclerView recyclerView;

    private int userRole;
    private List<String> userDepartments;

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
        userDepartments = getIntent().getStringArrayListExtra(DepartmentActivity.USER_DEPARTMENTS);

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
        GoogleSignInAccount accountGoogle = GoogleSignIn.getLastSignedInAccount(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String id = mAuth.getCurrentUser().getUid();

        /*Se non ha effettuato l'accesso con Google l'utente è presente sia in Authentication
        * che in Realtime Database senza ruolo, dipartimento e corso quindi deve solo aggiornare quest'ultimi
        */
        if(mAuth.getCurrentUser()!=null && accountGoogle == null){

            HashMap childUpdates = new HashMap();
            childUpdates.put("/ruolo/", userRole);
            childUpdates.put("/dipartimenti/", userDepartments);
            childUpdates.put("/corso/", course);

            usersReference.child(id).updateChildren(childUpdates);

        }else{
            /*se ha effettuato l'accesso con Google l'utente è presente solo in Authentication
            * quindi salva l'utente in Realtime Database
            */
            if(mAuth.getCurrentUser()!=null && accountGoogle != null) {
                User user = new User(id, accountGoogle.getGivenName(), accountGoogle.getFamilyName(),
                        accountGoogle.getEmail(), userRole, userDepartments, course);
                usersReference.child(id).setValue(user);

            }
        }


    }

    private int getUserCourseFromString (String course) {
        if (course.equals(getString(R.string.list_degree_courses_informatica))) {
            return User.COURSE_INFORMATICA;
        } else if (course.equals(getString(R.string.list_degree_courses_itps))) {
            return User.COURSE_ITPS;
        } else {
            throw new IllegalStateException("Aggiungere i corsi nel metodo, rispettando quanti" +
                    "corsi possono essere scelti nell'app.");
        }
    }
}