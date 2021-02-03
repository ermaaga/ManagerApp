package it.uniba.di.sms2021.managerapp.projects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectsActivity extends AbstractBottomNavigationActivity {

    private RecyclerView recyclerView;
    private ProjectsRecyclerAdapter adapter;

    private static final String TAG = "ProjectsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.projects_recycler_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Creo l'adapter che crea gli elementi con i relativi dati.
        adapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Ottengo i dati con cui riempire la lista.
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Project> projects = new ArrayList<>();

                        for (DataSnapshot child: snapshot.getChildren()) {
                            Group group = child.getValue(Group.class);
                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            if (group.getMembri().contains(currentUserId)) {
                                //Uso l'inizializzatore di progetti per ottenere tutti i dati utili
                                //e quando è inizializzato, lo visualizzo nella lista
                                new Project.Initialiser() {
                                    @Override
                                    public void onProjectInitialised(Project project) {
                                        Log.d(TAG, currentUserId + " " + group.getName() + " " +
                                                group.getMembri());
                                        projects.add(project);
                                        adapter.submitList(projects);
                                        adapter.notifyDataSetChanged();
                                    }
                                }.initialiseProject(group);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_projects;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
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

    public void chooseProject(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }
}