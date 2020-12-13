package it.uniba.di.sms2021.managerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import it.uniba.di.sms2021.managerapp.lists.RecyclerViewArrayAdapter;

public class DegreeCoursesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_degree_courses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.degreeCoursesRecyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        RecyclerViewArrayAdapter adapter = new RecyclerViewArrayAdapter(getResources()
                .getStringArray(R.array.list_degree_courses), new RecyclerViewArrayAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(String item) {
                //TODO Salva dato.

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
}