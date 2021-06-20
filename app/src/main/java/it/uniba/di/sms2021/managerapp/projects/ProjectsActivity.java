package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.ListProjects;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ListProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.lists.ProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.lists.ProjectsRecyclerViewManager;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;
import it.uniba.di.sms2021.managerapp.utility.ShakeUtil;

public class ProjectsActivity extends AbstractBottomNavigationActivity implements SensorEventListener {

    private final Context context = ProjectsActivity.this;

    Animation animRotate;
    boolean firstStart;
    boolean myProjectsExist = false;

    private static final int REQUEST_ENABLE_BT = 1;

    private ProjectsRecyclerViewManager myProjectsRecyclerViewManager;
    private ProjectsRecyclerViewManager.Builder myProjectsRecyclerViewManagerBuilder;
    private ProjectsRecyclerAdapter myProjectsAdapter;
    private ProjectsRecyclerViewManager favouriteProjectsRecyclerViewManager;
    private ProjectsRecyclerViewManager.Builder favouriteProjectsRecyclerViewManagerBuilder;
    private ProjectsRecyclerAdapter favouriteProjectsAdapter;
    private ProjectsRecyclerViewManager triedProjectsRecyclerViewManager;
    private ProjectsRecyclerViewManager.Builder triedProjectsRecyclerViewManagerBuilder;
    private ProjectsRecyclerAdapter triedProjectsAdapter;
    private boolean isProfessor;
    private ProjectsRecyclerViewManager evaluatedProjectsRecyclerViewManager;
    private ProjectsRecyclerViewManager.Builder evaluatedProjectsRecyclerViewManagerBuilder;
    private ProjectsRecyclerAdapter evaluatedProjectsAdapter;

    private RecyclerView listProjectsRecyclerView;
    private ListProjectsRecyclerAdapter listProjectsAdapter;

    private BluetoothAdapter bluetoothAdapter;

    private TextView listProjectsEmptyTextView;
    private ImageView receivedProjectListsMinimizeButton;
    private boolean receivedListsExpanded = true;

    private static final String TAG = "ProjectsActivity";
    private DatabaseReference groupsReference;
    private DatabaseReference listIdReference;
    private ValueEventListener projectListener;
    private ValueEventListener listIdProjectsListener;

    private Button receiveButton;

    private String lastQuery = "";
    private final Set<String> searchFilters = new HashSet<>();

    private List<Project> myProjects;
    private List<Project> favouriteProjects;
    private List<Project> triedProjects;
    private List<Project> evaluatedProjects;
    private List<String> favouriteProjectsIds;
    private List<String> triedProjectsIds;
    private List<String> evaluatedProjectsIds;
    private List<Project> shareableProjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShakeUtil.inizializeShake(getApplicationContext());

        animRotate = AnimationUtils.loadAnimation(this, R.anim.shake_animation);

        myProjectsRecyclerViewManagerBuilder = ProjectsRecyclerViewManager.getBuilder(
                findViewById(R.id.my_projects_recycler_view));
        myProjectsRecyclerViewManagerBuilder.withShareButton(findViewById(R.id.share_list_image_view))
            .withEmptyTextView(findViewById(R.id.my_projects_empty_text_view))
            .withMinimizeButton(findViewById(R.id.my_projects_minimize_button))
            .withProjectsLayout(findViewById(R.id.my_projects_layout));

        favouriteProjectsRecyclerViewManagerBuilder = ProjectsRecyclerViewManager.getBuilder(
                findViewById(R.id.favourite_recycler_view));
        favouriteProjectsRecyclerViewManagerBuilder.withShareButton(findViewById(R.id.share_favourite_list_image_view))
                .withEmptyTextView(findViewById(R.id.favourite_empty_text_view))
                .withMinimizeButton(findViewById(R.id.favourite_minimize_button))
                .withProjectsLayout(findViewById(R.id.favourite_projects_layout));

        evaluatedProjectsRecyclerViewManagerBuilder = ProjectsRecyclerViewManager.getBuilder(
                findViewById(R.id.evaluated_recycler_view));
        evaluatedProjectsRecyclerViewManagerBuilder.withShareButton(findViewById(R.id.share_evaluated_list_image_view))
                .withEmptyTextView(findViewById(R.id.evaluated_empty_text_view))
                .withMinimizeButton(findViewById(R.id.evaluated_minimize_button))
                .withProjectsLayout(findViewById(R.id.evaluated_projects_layout));

        triedProjectsRecyclerViewManagerBuilder = ProjectsRecyclerViewManager.getBuilder(
                findViewById(R.id.tried_recycler_view));
        triedProjectsRecyclerViewManagerBuilder.withShareButton(findViewById(R.id.share_tried_list_image_view))
                .withEmptyTextView(findViewById(R.id.tried_empty_text_view))
                .withMinimizeButton(findViewById(R.id.tried_minimize_button))
                .withProjectsLayout(findViewById(R.id.tried_projects_layout));

        receiveButton = findViewById(R.id.button_receive);
        listProjectsRecyclerView = findViewById(R.id.list_projects_recycler_view);
        listProjectsEmptyTextView = findViewById(R.id.project_lists_empty_text_view);
        receivedProjectListsMinimizeButton = findViewById(R.id.projects_received_minimize_button);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //controlla se il bluetooth è supportato dal device
        //se non supportato non fa vedere icona di condivisione
        if (bluetoothAdapter == null){
            myProjectsRecyclerViewManagerBuilder.shareable(false);
            favouriteProjectsRecyclerViewManagerBuilder.shareable(false);
            triedProjectsRecyclerViewManagerBuilder.shareable(false);
            evaluatedProjectsRecyclerViewManagerBuilder.shareable(false);
            receiveButton.setVisibility(View.GONE);
            Log.d(TAG, "Bluetooth non è supportato da questo dispositivo");
        }
        else {
            Log.d(TAG, "Bluetooth è supportato da questo dispositivo");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        firstStart = prefs.getBoolean("firstStart", true);

        initialiseRecyclerViews();
        initialiseProjectListsExpansionButton();
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupsReference.removeEventListener(projectListener);
        listIdReference.removeEventListener(listIdProjectsListener);
    }

    protected void onResume() {
        super.onResume();
        ShakeUtil.registerShakeListener(this);
    }

    public void onPause() {
        super.onPause();
        ShakeUtil.unRegisterShakeListener(this);
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
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        //Impostazioni per la barra di ricerca.
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        ViewGroup searchFilters = findViewById(R.id.searchFilterScrollView);
        SearchUtil.setUpSearchBar(this, searchView, searchFilters,
                R.string.text_hint_search_project, onSearchListener);

        return true;
    }

    private void initialiseRecyclerViews() {
        initialiseMyProjectRecyclerView();
        addDataListenerToMyProjects();

        initialiseFavouriteProjectsRecyclerView();
        initialiseTriedProjectsRecyclerView();
        if (LoginHelper.getCurrentUser().getRuolo() == User.ROLE_PROFESSOR) {
            isProfessor = true;
            initialiseEvaluatedProjectsRecyclerView();
        } else {
            isProfessor = false;
            findViewById(R.id.header_evaluated_card_view).setVisibility(View.GONE);
            findViewById(R.id.evaluated_projects_layout).setVisibility(View.GONE);
        }
        initialiseReceivedProjectsRecyclerView();
        addDataListenerToProjectLists();
    }

    private void initialiseMyProjectRecyclerView() {
        //Creo l'adapter che crea gli elementi con i relativi dati.
        myProjectsAdapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        myProjectsRecyclerViewManagerBuilder.withAdapter(myProjectsAdapter);
        myProjectsRecyclerViewManager = myProjectsRecyclerViewManagerBuilder.build();
    }

    private void initialiseFavouriteProjectsRecyclerView() {
        favouriteProjectsAdapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        favouriteProjectsRecyclerViewManagerBuilder.withAdapter(favouriteProjectsAdapter);
        favouriteProjectsRecyclerViewManager = favouriteProjectsRecyclerViewManagerBuilder.build();
    }

    private void initialiseTriedProjectsRecyclerView() {
        triedProjectsAdapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        triedProjectsRecyclerViewManagerBuilder.withAdapter(triedProjectsAdapter);
        triedProjectsRecyclerViewManager = triedProjectsRecyclerViewManagerBuilder.build();
    }

    private void initialiseEvaluatedProjectsRecyclerView() {
        evaluatedProjectsAdapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        evaluatedProjectsRecyclerViewManagerBuilder.withAdapter(evaluatedProjectsAdapter);
        evaluatedProjectsRecyclerViewManager = evaluatedProjectsRecyclerViewManagerBuilder.build();
    }

    private void initialiseReceivedProjectsRecyclerView() {
        listProjectsAdapter = new ListProjectsRecyclerAdapter(new ListProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked(ListProjects list) {
                chooseList(list);
            }
        });
        listProjectsRecyclerView.setAdapter(listProjectsAdapter);
        listProjectsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        listProjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void addDataListenerToMyProjects() {
        //Ottengo i dati con cui riempire la lista.
        groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);
        projectListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myProjects = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Group group = child.getValue(Group.class);
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    if (group.getMembri().contains(currentUserId)) {
                        myProjectsExist=true;
                        //Uso l'inizializzatore di progetti per ottenere tutti i dati utili
                        //e quando è inizializzato, lo visualizzo nella lista
                        new Project.Initialiser() {
                            @Override
                            public void onProjectInitialised(Project project) {
                                if (!myProjects.contains(project)) {
                                    myProjects.add(project);
                                    myProjectsAdapter.submitList(myProjects);
                                    myProjectsAdapter.notifyDataSetChanged();
                                }
                            }
                        }.initialiseProject(group);
                    }
                }

                //Se ci sono progetti nella lista dei progetti personali
                if(myProjectsExist == true){
                    //Se il Bluetooth è supportato dal dispositivo
                    if(myProjectsRecyclerViewManager.isShareable()){
                        //Se l'activity viene aperta per la prima volta, viene mostrato il tutorial
                        if(firstStart){
                            showImageDialog();
                        }
                    }
                }else{
                    //Se la lista di progetti è vuota non viene visualizzata l'icona di condivisione
                    myProjectsRecyclerViewManager.setShareable(false);
                }

                myProjectsRecyclerViewManager.setProjectsViewHasData(myProjectsExist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupsReference.addValueEventListener(projectListener);
    }

    private void addDataListenerToProjectLists() {
        listIdReference = FirebaseDbHelper.getListsProjectsReference(LoginHelper.getCurrentUser().getAccountId());

        listIdProjectsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                initialiseFavouriteProjects(snapshot.child(FirebaseDbHelper.TABLE_FAVOURITE_PROJECTS));
                initialiseTriedProjects(snapshot.child(FirebaseDbHelper.TABLE_TRIED_PROJECTS));
                if (isProfessor) {
                    initialiseEvaluatedProjects(snapshot.child(FirebaseDbHelper.TABLE_EVALUATED_PROJECTS));
                }
                initialiseReceivedProjectLists(snapshot.child(FirebaseDbHelper.TABLE_RECEIVED_PROJECT_LISTS));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listIdReference.addValueEventListener(listIdProjectsListener);
    }

    private void initialiseFavouriteProjects(DataSnapshot snapshot) {
        favouriteProjectsIds = new ArrayList<>();
        if (snapshot.getChildrenCount() == 0) {
            favouriteProjectsRecyclerViewManager.setShareable(false);
            favouriteProjectsRecyclerViewManager.setProjectsViewHasData(false);
            addProjectsToRecyclerViews();
            return;
        }

        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
            favouriteProjectsIds.add(dataSnapshot.getKey());
        }

        favouriteProjectsRecyclerViewManager.setProjectsViewHasData(true);
        addProjectsToRecyclerViews();
    }

    private void initialiseTriedProjects(DataSnapshot snapshot) {
        triedProjectsIds = new ArrayList<>();
        if (snapshot.getChildrenCount() == 0) {
            triedProjectsRecyclerViewManager.setShareable(false);
            triedProjectsRecyclerViewManager.setProjectsViewHasData(false);
            addProjectsToRecyclerViews();
            return;
        }

        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
            triedProjectsIds.add(dataSnapshot.getKey());
        }

        triedProjectsRecyclerViewManager.setProjectsViewHasData(true);
        addProjectsToRecyclerViews();
    }

    private void initialiseEvaluatedProjects(DataSnapshot snapshot) {
        evaluatedProjectsIds = new ArrayList<>();
        if (snapshot.getChildrenCount() == 0) {
            evaluatedProjectsRecyclerViewManager.setShareable(false);
            evaluatedProjectsRecyclerViewManager.setProjectsViewHasData(false);
            addProjectsToRecyclerViews();
            return;
        }

        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
            evaluatedProjectsIds.add(dataSnapshot.getKey());
        }

        evaluatedProjectsRecyclerViewManager.setProjectsViewHasData(true);
        addProjectsToRecyclerViews();
    }

    private void addProjectsToRecyclerViews() {
        if (projectsInitializationEnded()) {
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            favouriteProjects = new ArrayList<>();
                            for (String key: favouriteProjectsIds) {
                                Group group = snapshot.child(key).getValue(Group.class);
                                new Project.Initialiser() {
                                    @Override
                                    public void onProjectInitialised(Project project) {
                                        if (project != null && !favouriteProjects.contains(project)) {
                                            favouriteProjects.add(project);
                                            favouriteProjectsAdapter.submitList(favouriteProjects);
                                            favouriteProjectsAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }.initialiseProject(group);
                            }

                            triedProjects = new ArrayList<>();
                            for (String key: triedProjectsIds) {
                                Group group = snapshot.child(key).getValue(Group.class);
                                new Project.Initialiser() {
                                    @Override
                                    public void onProjectInitialised(Project project) {
                                        if (!triedProjects.contains(project)) {
                                            triedProjects.add(project);
                                            triedProjectsAdapter.submitList(triedProjects);
                                            triedProjectsAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }.initialiseProject(group);
                            }

                            if (isProfessor) {
                                evaluatedProjects = new ArrayList<>();
                                for (String key: evaluatedProjectsIds) {
                                    Group group = snapshot.child(key).getValue(Group.class);
                                    new Project.Initialiser() {
                                        @Override
                                        public void onProjectInitialised(Project project) {
                                            if (!evaluatedProjects.contains(project)) {
                                                evaluatedProjects.add(project);
                                                evaluatedProjectsAdapter.submitList(evaluatedProjects);
                                                evaluatedProjectsAdapter.notifyDataSetChanged();
                                            }
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
    }

    private boolean projectsInitializationEnded() {
        return favouriteProjectsIds != null &&
                triedProjectsIds != null &&
                (evaluatedProjectsIds != null || !isProfessor);
    }

    public void initialiseReceivedProjectLists(DataSnapshot snapshot) {
        List<ListProjects> idLists = new ArrayList<>();
        if(snapshot.getChildrenCount() != 0) {
            for (DataSnapshot child : snapshot.getChildren()) {
                ListProjects list = child.getValue(ListProjects.class);
                idLists.add(list);
            }
            listProjectsAdapter.submitList(idLists);
            Log.d(TAG, "listId: "+ idLists.toString());

            setReceivedListsViewHasData(true);
        } else {
            setReceivedListsViewHasData(false);
        }
    }

    private void initialiseProjectListsExpansionButton() {
        receivedProjectListsMinimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receivedListsExpanded) {
                    receivedListsExpanded = false;
                    receivedProjectListsMinimizeButton.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.ic_baseline_expand_24));
                    setReceivedProjectsExpanded(false);
                } else {
                    receivedListsExpanded = true;
                    receivedProjectListsMinimizeButton.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.ic_baseline_minimize_24));
                    setReceivedProjectsExpanded(true);
                }
            }
        });
    }

    private void setReceivedProjectsExpanded(boolean expanded) {
        ConstraintLayout receivedProjectsLayout = findViewById(R.id.received_project_lists_layout);
        if (expanded) {
            receivedProjectsLayout.setVisibility(View.VISIBLE);
        } else {
            receivedProjectsLayout.setVisibility(View.GONE);
        }
    }

    //Utilizzato per mostrare il tutorial della condivisione tramite lo scuotimento
    private void showImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_shake_smartphone, null);
        view.startAnimation(animRotate);

        animRotate.setAnimationListener(new Animation.AnimationListener() {
            int countRepeat=0;
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                countRepeat++;
                animation.setStartOffset(0);
                if(countRepeat>15){
                    countRepeat=0;
                    animation.setStartOffset(2000);
                    view.startAnimation(animation);
                }else{
                    view.startAnimation(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        builder.setView(view)
                .setTitle(R.string.text_lable_shake_tutorial)
                .setMessage(R.string.text_message_shake_tutorial)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        //Preferenza impostata a false per indicare che l'activity è stata già aperta una volta
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    private SearchUtil.OnSearchListener onSearchListener = new SearchUtil.OnSearchListener() {
        @Override
        public void onSearchAction(String query) {
            lastQuery = query;

            String[] keyWords = query.toLowerCase().split(" ");

            List<Project> searchProjects = new ArrayList<>();
            String createdFilter = getString(R.string.text_filter_projects_created).toLowerCase();
            String releaseFilter = getString(R.string.text_filter_projects_withrelease).toLowerCase();
            String evaluatedFilter = getString(R.string.text_filter_projects_hasevaluation).toLowerCase();

            for (Project project: myProjects) {
                boolean toAdd = true;

                if (!query.isEmpty()) {
                    for (String string: keyWords) {
                        //Se il file non include una delle parole chiavi, non verrà mostrato.
                        //Verrà sempre mostrato sempre invece se la query è vuota
                        if (toAdd) {
                            toAdd = // Va aggiunto se il nome corrisponde alla query
                                    project.getName().toLowerCase().contains(string) ||
                                    // Va aggiunto se il nome del gruppo corrisponde alla query
                                    project.getStudyCaseName().toLowerCase().contains(string);
                        }
                    }
                }

                if (toAdd && !searchFilters.isEmpty()) {
                    // Il file va aggiunto se include almeno uno dei filtri
                    boolean containsFilter = false;
                    for (String string: searchFilters) {
                        containsFilter =    // Filtro per i progetti creati dall'utente
                                (string.contains(createdFilter) && project.isCreator()) ||
                                // Filtro per i progetti con rilasci
                                (string.contains(releaseFilter) && project.hasReleases()) ||
                                // Filtro per i progetti valutati dal professore
                                (string.contains(evaluatedFilter) && project.isEvaluated());

                        if (containsFilter) {
                            break;
                        }
                    }

                    toAdd = containsFilter;
                }

                if (toAdd) {
                    searchProjects.add(project);
                }
            }
            myProjectsAdapter.submitList(searchProjects);
        }

        @Override
        public void onFilterAdded(String filter) {
            searchFilters.add(filter.toLowerCase());
            onSearchListener.onSearchAction(lastQuery);
        }

        @Override
        public void onFilterRemoved(String filter) {
            searchFilters.remove(filter.toLowerCase());
            onSearchListener.onSearchAction(lastQuery);
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    public void chooseProject(Project project) {
        Log.d(TAG, " chooseProject vote and prefers: " + project.getEvaluation()+ " " +project.getWhoPrefers());
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

    public void chooseList(ListProjects list) {
        Log.d(TAG, "click list: " + list.getIdList());
        Intent intent = new Intent(this, ProjectsListDetailActivity.class);
        intent.putExtra(ListProjects.KEY, list);
        intent.putExtra("tutorial", myProjectsExist);
        startActivity(intent);
    }

    public void share_my_project_list(View view){
        actionShareList(myProjects);
    }

    public void share_favourite_list(View view) {
        actionShareList(favouriteProjects);
    }

    public void share_tried_list(View view) {
        actionShareList(triedProjects);
    }

    public void share_evaluated_list(View view) {
        actionShareList(evaluatedProjects);
    }

    public void reciveList(View view){
        actionShareList(null);
    }

    private void actionShareList(List<Project> projectList){
        if (!bluetoothAdapter.isEnabled()){
             Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            shareableProjectList = projectList;
             startActivityForResult(intent, REQUEST_ENABLE_BT);

        } else {
            Log.d(TAG, "Bluetooth is already on ");
            shareableProjectList = projectList;
            go_sharing_activity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    Log.d(TAG, "Bluetooth is on");
                    go_sharing_activity();
                }
                else {
                    Log.d(TAG, "User denied to turn bluetooth on");
                }
                break;
        }

    }

    public void go_sharing_activity(){
        Log.d(TAG, "goSharingActivity");

        String projectsId=null;

        //se shareableProjectList è diverso da null vuol dire che l'utente
        // ha cliccato su condividi di una delle tipologie delle liste.
        // Altrimenti se shareableProjectList è null ha cliccato su ricevi.
        if( shareableProjectList != null) {
            projectsId = new String();
            for (Project proj : shareableProjectList) {
                if (projectsId.isEmpty()) {
                    projectsId = proj.getId();
                } else {
                    projectsId = projectsId + "," + proj.getId();
                }
            }
        }

        Log.d(TAG, "projectsId: "+projectsId);
        Intent intent = new Intent(this, ProjectsSharingActivity.class);
       //se projectsId è uguale a null vuol dire che ha cliccato su ricevi
       // e quindi non deve condividere nessuna lista
        intent.putExtra(Project.KEY, projectsId);
        startActivity(intent);
    }

    private ShakeUtil.OnShakeListener onShakeListener = new ShakeUtil.OnShakeListener() {
        @Override
        public void doActionAfterShake() {
            //todo decidere quale lista condividere
            actionShareList(myProjects);
        }
    };

    //Chiamato quando viene letto un nuovo evento dal sensore
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(myProjectsRecyclerViewManager.getShareButton().getVisibility()==View.VISIBLE) {
            ShakeUtil.checkShake(sensorEvent, onShakeListener);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void setReceivedListsViewHasData(boolean hasData) {
        if (hasData) {
            listProjectsRecyclerView.setVisibility(View.VISIBLE);
            listProjectsEmptyTextView.setVisibility(View.GONE);
        } else {
            listProjectsRecyclerView.setVisibility(View.GONE);
            listProjectsEmptyTextView.setVisibility(View.VISIBLE);
        }
    }
}