package it.uniba.di.sms2021.managerapp.projects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;

public class ProjectsActivity extends AbstractBottomNavigationActivity {

    private RecyclerView recyclerView;
    private ProjectsRecyclerAdapter adapter;

    private static final String TAG = "ProjectsActivity";
    private DatabaseReference groupsReference;
    private ValueEventListener projectListener;

    private MenuItem searchMenuItem;
    private List<Project> projects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);
        projectListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                projects = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Group group = child.getValue(Group.class);
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    if (group.getMembri().contains(currentUserId)) {
                        //Uso l'inizializzatore di progetti per ottenere tutti i dati utili
                        //e quando è inizializzato, lo visualizzo nella lista
                        new Project.Initialiser() {
                            @Override
                            public void onProjectInitialised(Project project) {
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
        };
        groupsReference.addValueEventListener(projectListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupsReference.removeEventListener(projectListener);
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
        getMenuInflater().inflate(R.menu.menu_projects, menu);
        searchMenuItem = menu.findItem(R.id.action_search);

        //Impostazioni per la barra di ricerca.
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        ViewGroup searchFilters = findViewById(R.id.searchFilterScrollView);
        SearchUtil.setUpSearchBar(this, searchView, searchFilters,
                R.string.text_hint_search_project, onSearchListener);

        return true;
    }

    private SearchUtil.OnSearchListener onSearchListener = new SearchUtil.OnSearchListener() {
        @Override
        public void onSearchAction(String query) {
            String[] keyWords = query.toLowerCase().split(" ");

            List<Project> searchProjects = new ArrayList<>();

            for (Project project: projects) {
                boolean toAdd = true;
                for (String string: keyWords) {
                    //Se il progetto non include una delle parole chiavi, non verrà mostrato.
                    //Verrà sempre mostrato sempre invece se la query è vuota
                    if (toAdd && !query.equals("")) {
                        toAdd = // Va aggiunto se il nome corrisponde alla query
                                project.getName().toLowerCase().contains(string) ||
                                // Va aggiunto se il nome del gruppo corrisponde alla query
                                project.getStudyCaseName().toLowerCase().contains(string);

                                        /*
                                        // Va aggiunto se il tipo corrisponde alla query
                                        file.getType().toLowerCase().contains(string.toLowerCase()) ||
                                        // Va aggiunto se il filtro contiene i rilasci ed il file ne è uno
                                        (string.contains(releaseFilter) && project.getReleaseNumber(file.getName()) != 0) ||
                                        // Va aggiunto se il filtro contiene le immagini ed il file ne è una.
                                        (string.contains(imagesFilter) && file.getType().contains("image/")) ||
                                        // Va aggiunto se il filtro contiene i pdf ed il file ne è uno.
                                        (string.contains(pdfFilter) && file.getType().equals("application/pdf"));
                                         */
                    }
                }

                if (toAdd) {
                    searchProjects.add(project);
                }
            }
            adapter.submitList(searchProjects);
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    public void chooseProject(Project project) {
        Log.d(TAG, "vote click project: "+project.getGroup().getEvaluation());
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }
}