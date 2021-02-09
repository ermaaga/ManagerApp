package it.uniba.di.sms2021.managerapp.exams;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.lists.ExamsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;


public class ExamsActivity extends AbstractBottomNavigationActivity {

    RecyclerView recyclerView;
    ExamsRecyclerAdapter adapter;

    FloatingActionButton btn_CreateNewExam;

    private DatabaseReference examsReference;
    private ValueEventListener examsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        // initialize components

        initialize();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.exams_recyclerView);


        btn_CreateNewExam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ExamsActivity.this,NewExamActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //test(); Usato per valori dummy

        //Creo l'adapter che crea gli elementi con i relativi dati.
        adapter = new ExamsRecyclerAdapter(new ExamsRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked(Exam exam) {
                chooseExam(exam);
            }
        });
        recyclerView.setAdapter(adapter);
        //Faccio sì che la lista venga visualizzata come una griglia su due colonne
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        //Ottengo i dati con cui riempire la lista.
        examsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS);
        examsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Exam> exams = new ArrayList<>();

                for (DataSnapshot child: snapshot.getChildren()) {
                    exams.add(child.getValue(Exam.class));
                }

                adapter.submitList(exams);  //Ogni volta che gli esami cambiano, la lista
                //visualizzata cambia.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        examsReference.addValueEventListener(examsListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        examsReference.removeEventListener(examsListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_exams;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    private void chooseExam (Exam exam) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Se lu studente partecipa già all'esame, lo fa entrare. Altrimenti manda una richiesta
        //per partecipare.
        if (exam.getStudents() != null && exam.getStudents().contains(uid)) {
            Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
            intent.putExtra(Exam.Keys.EXAM, exam);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.text_message_join_exam)
                    .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO implementare sistema di richiesta di accesso
                            List<String> students = exam.getStudents();
                            if (students == null) {
                                students = new ArrayList<>();
                            }
                            students.add(uid);

                            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS)
                                    .child(exam.getId()).child(Exam.Keys.STUDENTS).setValue(students);

                            Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
                            intent.putExtra(Exam.Keys.EXAM, exam);
                            startActivity(intent);
                        }
                    }).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
        }
    }

    //TODO rimuovere questo codice nella versione finale.
    public void test () {
        DatabaseReference userRef = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        DatabaseReference tableRef = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS);

        userRef.child("ABC").setValue(new User("ABC", "Fabrizio", "Balducci", "Email@email.com",
                User.ROLE_PROFESSOR, Arrays.asList("dipart informatica"),  User.COURSE_ITPS));
        DatabaseReference newElement = tableRef.push();
        newElement.setValue(new Exam(newElement.getKey(), "SMS20-21", Arrays.asList("ABC"), null, 2020));
    }

    private void initialize() {
        btn_CreateNewExam = findViewById(R.id.exam_add_floating_action_button);
    }
}