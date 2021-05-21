package it.uniba.di.sms2021.managerapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Course;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.exams.ExamsActivity;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.lists.CourseRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBaseActivity;

public class DegreeCoursesActivity extends AbstractBaseActivity {
    private static final String TAG = "DegreeCoursesActivity";
    private RecyclerView recyclerView;
    private CourseRecyclerAdapter adapter;

    private int userRole;
    private List<String> userDepartments;
    private List<String> listcourses;

    private FirebaseDatabase database;
    private DatabaseReference usersReference;

    private FloatingActionButton buttonnext;

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

        buttonnext = (FloatingActionButton) findViewById(R.id.floatingActionButtonNext);

        //viene visualizzato solo se il ruolo è PROFESSOR
        buttonnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listcourses = adapter.selectedCourses();
                submitUser();
                goToHome();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

         adapter = new CourseRecyclerAdapter(DegreeCoursesActivity.this, userRole, new CourseRecyclerAdapter.OnActionListener() {
             @Override
             public void onSelectionActionProfessor(Boolean isSelected) {
                 if(isSelected){
                     buttonnext.setVisibility(View.VISIBLE);
                 }else{
                     buttonnext.setVisibility(View.GONE);
                 }
             }

             @Override
             public void onSelectionActionStudent(String idCourse) {
                 listcourses = new ArrayList<>();
                 listcourses.add(idCourse);
                 submitUser();
                 goToHome();
             }
         });

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_COURSES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Course> courses = new ArrayList<>();

                        for (DataSnapshot child: snapshot.getChildren()) {
                            Course currentCourse = child.getValue(Course.class);
                            if( userDepartments.contains(currentCourse.getDepartment())){
                                courses.add(currentCourse);
                            }

                        }

                        adapter.submitList(courses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void submitUser () {
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
            childUpdates.put("/corsi/", listcourses);

            usersReference.child(id).updateChildren(childUpdates);

            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS).child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Setta l'utente attuale in una variabile accessibile nel resto dell'applicazione
                            LoginHelper.setCurrentUser(snapshot.getValue(User.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }else{
            /*se ha effettuato l'accesso con Google l'utente è presente solo in Authentication
            * quindi salva l'utente in Realtime Database
            */
            if(mAuth.getCurrentUser()!=null && accountGoogle != null) {
                User user = new User(id, accountGoogle.getGivenName(), accountGoogle.getFamilyName(),
                        accountGoogle.getEmail(), userRole, userDepartments, listcourses);
                usersReference.child(id).setValue(user);

                // Setta l'utente attuale in una variabile accessibile nel resto dell'applicazione
                LoginHelper.setCurrentUser(user);
            }
        }

    }

    private void goToHome(){
        Intent intent = new Intent(DegreeCoursesActivity.this, ExamsActivity.class);

        // Pulisce il backstack delle activity (un utente non dovrebbe navigare indietro
        // fino al login)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //Uso la classe esplicitamente perchè "this" si riferirebbe al listener
        DegreeCoursesActivity.this.finish();
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

    public void test () {
        DatabaseReference courseRef = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_COURSES);

        for(int i=0; i<5; i++){
            String id = courseRef.push().getKey();
            courseRef.child(id).setValue(new Course(id,"course"+i,"department"+i));
        }
    }
}