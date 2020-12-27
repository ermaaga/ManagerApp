package it.uniba.di.sms2021.managerapp.exams;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.lists.ExamsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamsActivity extends AbstractBottomNavigationActivity {

    RecyclerView recyclerView;
    ExamsRecyclerAdapter adapter;

    public static final String CHOSEN_EXAM = "ChosenExam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.exams_recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //test(); Usato per valori dummy

        //Creo l'adapter che crea gli elementi con i relativi dati.
        adapter = new ExamsRecyclerAdapter(new ExamsRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked(Exam exam) {
                //TODO rendere dinamico in base all'item cliccato.
                chooseExam(exam);
            }
        });
        recyclerView.setAdapter(adapter);
        //Faccio sì che la lista venga visualizzata come una griglia su due colonne
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        //Ottengo i dati con cui riempire la lista.
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS)
                .addValueEventListener(new ValueEventListener() {
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
                });

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
        Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
        intent.putExtra(CHOSEN_EXAM, exam);
        startActivity(intent);
    }

    //TODO rimuovere questo codice nella versione finale.
    public void test () {
        DatabaseReference userRef = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        DatabaseReference tableRef = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS);

        userRef.child("ABC").setValue(new User("ABC", "Fabrizio", "Balducci", "Email@email.com",
                User.ROLE_PROFESSOR, User.COURSE_ITPS));
        DatabaseReference newElement = tableRef.push();
        newElement.setValue(new Exam(newElement.getKey(), "SMS20-21", Arrays.asList("ABC"), null, 2020));
    }
}