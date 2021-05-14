package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.ListProjects;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;
import it.uniba.di.sms2021.managerapp.utility.ShakeUtil;

public class ProjectsListDetailActivity extends AbstractBottomNavigationActivity implements SensorEventListener {

    Animation animRotate;
    boolean firstStart;

    private static final int REQUEST_ENABLE_BT = 1;

    private RecyclerView projectsRecyclerView;
    private ProjectsRecyclerAdapter projectsAdapter;
    private BluetoothAdapter bluetoothAdapter;

    private static final String TAG = "ProjectsActivity";
    private DatabaseReference groupsReference;
    private ValueEventListener projectsListener;

    private MenuItem searchMenuItem;
    private List<String> listIdProjects;
    private List<Project> projects;
    private ListProjects listSelected;

    ImageView shareProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShakeUtil.inizializeShake(getApplicationContext());

        animRotate = AnimationUtils.loadAnimation(this, R.anim.shake_animation);

        listSelected  = getIntent().getParcelableExtra(ListProjects.KEY);
        TextView project_list_title = findViewById(R.id.project_list_title_text_view);
        project_list_title.setText(listSelected.getNameList());

        shareProjects = findViewById(R.id.share_list_image_view);
        projectsRecyclerView = findViewById(R.id.projects_recycler_view);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //controlla se il bluetooth è supportato dal device
        //se non supportato non fa vedere icona di condivisione
        if (bluetoothAdapter == null){
            shareProjects.setVisibility(View.GONE);
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(listSelected.getNameList());

        boolean myProjectsExist = getIntent().getBooleanExtra("tutorial", false);
        //Se non ci sono progetti nella lista dei progetti personali
        if(myProjectsExist == false){
            //Se il Bluetooth è supportato dal dispositivo
            if(shareProjects.getVisibility()==View.VISIBLE){
                //Se l'activity viene aperta per la prima volta, viene mostrato il tutorial
                if(firstStart){
                    showImageDialog();
                }
            }
        }

        projectsAdapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        projectsRecyclerView.setAdapter(projectsAdapter);
        projectsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        projectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        listIdProjects = listSelected.getIdProjects();

        Log.d(TAG, "listId: "+ listIdProjects.toString());

        //Ottengo i dati con cui riempire la lista.
        groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);

        projectsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                projects = new ArrayList<>();
                if(listIdProjects != null){
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Group group = child.getValue(Group.class);
                        for(String id: listIdProjects){
                            if (group.getId().equals(id)) {
                                //Uso l'inizializzatore di progetti per ottenere tutti i dati utili
                                //e quando è inizializzato, lo visualizzo nella lista
                                new Project.Initialiser() {
                                    @Override
                                    public void onProjectInitialised(Project project) {
                                        projects.add(project);
                                        Log.d(TAG, "list project ricevuti: "+projects.toString());
                                        projectsAdapter.submitList(projects);
                                        projectsAdapter.notifyDataSetChanged();
                                    }
                                }.initialiseProject(group);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupsReference.addValueEventListener(projectsListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        groupsReference.removeEventListener(projectsListener);
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
        return R.layout.activity_projects_list_detail;
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
                    }
                }

                if (toAdd) {
                    searchProjects.add(project);
                }
            }
            projectsAdapter.submitList(searchProjects);
        }

        @Override
        public void onFilterAdded(String filter) {
            //TODO implementare
        }

        @Override
        public void onFilterRemoved(String filter) {
            //TODO implementare
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    public void chooseProject(Project project) {
        Log.d(TAG, "vote click project: " + project.getGroup().getEvaluation());
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

    public void share_list_project(View view){
        actionShareList();
    }

    private void actionShareList(){
        if (!bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            Log.d(TAG, "Bluetooth is already on ");
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
                    //TODO decidere se far vedere un dialog in cui spiegare all'utente che è necessario attivare il bluetooth se vuole condividere la lista
                    Log.d(TAG, "User denied to turn bluetooth on");
                }
                break;
        }

    }


    public void go_sharing_activity(){
        Log.d(TAG, "goSharingActivity");

        String projectsId = new String();

        for(Project proj: projects){
            if(projectsId.isEmpty()){
                projectsId = proj.getId();
            }else {
                projectsId = projectsId + "," + proj.getId();
            }
        }

        Log.d(TAG, "groupsId: "+projectsId);
        Intent intent = new Intent(this, ProjectsSharingActivity.class);
        intent.putExtra(Project.KEY, projectsId);
        startActivity(intent);
    }

    //Utilizzato per mostrare il tutorial della condivisione tramite lo scuotimento
    private void showImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsListDetailActivity.this);

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

    private ShakeUtil.OnShakeListener onShakeListener = new ShakeUtil.OnShakeListener() {
        @Override
        public void doActionAfterShake() {
            actionShareList();
        }
    };

    //Chiamato quando viene letto un nuovo evento dal sensore
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(shareProjects.getVisibility()==View.VISIBLE) {
            ShakeUtil.checkShake(sensorEvent, onShakeListener);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}