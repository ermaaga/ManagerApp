package it.uniba.di.sms2021.managerapp.projects;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ListProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.lists.ProjectsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;

public class ProjectsActivity extends AbstractBottomNavigationActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor sensor;
    private float x, y, z;
    private boolean itIsNotFirstTime = false;
    private float accelerationPrev= 0.0f;
    private float acceleration = 0.0f;

    //Soglia massima della variazione di accelerazione affinchè venga considerato lo scuotimento
    private static final float THRESHOLD = 5f;
    //Soglia minima della accelerazione corrente affinchè venga considerato lo scuotimento
    private static final float THRESHOLD_ACCELERATION = 20f;
    //Soglia minima della accelerazione precedente affinchè venga considerato lo scuotimento
    private static final float THRESHOLD_ACCELERATION_PREV = 20f;

    private Vibrator vibrator;

    private static final int REQUEST_ENABLE_BT = 1;

    private RecyclerView myProjectsRecyclerView;
    private ProjectsRecyclerAdapter myProjectsAdapter;
    private RecyclerView listProjectsRecyclerView;
    private ListProjectsRecyclerAdapter listProjectsAdapter;
    private BluetoothAdapter bluetoothAdapter;

    private static final String TAG = "ProjectsActivity";
    private DatabaseReference groupsReference;
    private DatabaseReference listIdReference;
    private ValueEventListener projectListener;
    private ValueEventListener listIdProjectsListener;

    private String lastQuery = "";
    private final Set<String> searchFilters = new HashSet<>();

    private List<Project> projects;
    private List<ListProjects> idLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inizializza mSensorManager che gestisce il sensore
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Inizializza sensor, usata per incapsulare il sensore. In questo caso l'accelerometro
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /*Stabilisce i valori di riferimento per determinare le accelerazioni.
        In questo caso uguale per tutte e due le variabili:la gravità terrestre*/
        accelerationPrev = SensorManager.GRAVITY_EARTH;
        acceleration = SensorManager.GRAVITY_EARTH;

        ImageView shareProjects = findViewById(R.id.share_list_image_view);
        myProjectsRecyclerView = findViewById(R.id.my_projects_recycler_view);
        listProjectsRecyclerView = findViewById(R.id.list_projects_recycler_view);
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

        //Creo l'adapter che crea gli elementi con i relativi dati.
        myProjectsAdapter = new ProjectsRecyclerAdapter(new ProjectsRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Project project) {
                chooseProject(project);
            }
        });
        myProjectsRecyclerView.setAdapter(myProjectsAdapter);
        myProjectsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        myProjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                                myProjectsAdapter.submitList(projects);
                                myProjectsAdapter.notifyDataSetChanged();
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

        listIdReference = FirebaseDbHelper.getListsProjectsReference(LoginHelper.getCurrentUser().getAccountId());

        listIdProjectsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idLists = new ArrayList<>();
                if(snapshot.getChildrenCount() != 0) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        ListProjects list = child.getValue(ListProjects.class);
                        idLists.add(list);
                    }
                    listProjectsAdapter.submitList(idLists);
                    Log.d(TAG, "listId: "+ idLists.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listIdReference.addValueEventListener(listIdProjectsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupsReference.removeEventListener(projectListener);
        listIdReference.removeEventListener(listIdProjectsListener);
    }

    protected void onResume() {
        super.onResume();
        //Registrazione del Listener per il sensore
        mSensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        //Cancellazione del Listener per il sensore
        mSensorManager.unregisterListener(this);
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

    private SearchUtil.OnSearchListener onSearchListener = new SearchUtil.OnSearchListener() {
        @Override
        public void onSearchAction(String query) {
            lastQuery = query;

            String[] keyWords = query.toLowerCase().split(" ");

            List<Project> searchProjects = new ArrayList<>();
            String createdFilter = getString(R.string.text_filter_projects_created).toLowerCase();
            String releaseFilter = getString(R.string.text_filter_projects_withrelease).toLowerCase();
            String evaluatedFilter = getString(R.string.text_filter_projects_hasevaluation).toLowerCase();

            for (Project project: projects) {
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
        Log.d(TAG, "vote click project: " + project.getGroup().getEvaluation());
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

    public void chooseList(ListProjects list) {
        Log.d(TAG, "click list: " + list.getIdList());
        Intent intent = new Intent(this, ProjectsListDetailActivity.class);
        intent.putExtra(ListProjects.KEY, list);
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

    //Chiamato quando viene letto un nuovo evento dal sensore
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        /*Inizializzati i valori delle accelerazioni
        misurati per tutte e tre le dimensioni dello spazio*/
        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];

        //Viene salvata l'accelerazione precedente per poi ricalcolarne la nuova
        accelerationPrev=acceleration;

        if(itIsNotFirstTime){
            /*Calcola la somma delle componenti cartesiane x, y e z in valore
             assoluto per determinare l'accelerazione corrente.
             Accelerazione: a = a_x + a_y + a_z */
            acceleration = Math.abs(x+y+z);
            Log.d(TAG, "accelerazione"+ acceleration);
            Log.d(TAG, "accelerationPrev"+ accelerationPrev);

            //Calcola la differenza tra accelerazione e accelerazione precendente in valore assoluto
            float differenceAcc = Math.abs(acceleration - accelerationPrev);
            Log.d(TAG, "differenza accelerazione"+ differenceAcc);

            /*Lo scuotimento viene rilevato nel caso in cui:
             -la differenza delle accelerazioni è minore di una soglia (in questo caso 5)
             tale per cui i valori di entrambe le accelerazioni applicate non si discostino di molto;
             - I valori delle accelerazioni applicate siano maggiori di una certa soglia (in queso caso 20)*/
            if(differenceAcc<THRESHOLD && acceleration>THRESHOLD_ACCELERATION && accelerationPrev>THRESHOLD_ACCELERATION_PREV){

                //Se si sta eseguendo su Oreo (Api level 26) o successivi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }else{
                    //Deprecato in Api 26
                    vibrator.vibrate(500);
                }
                actionShareList();
            }
        }
        //Usato per indicare che in precedenza c'è stato almeno un rilevamento
        itIsNotFirstTime = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}