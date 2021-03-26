package it.uniba.di.sms2021.managerapp.projects;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
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

public class ProjectsListDetailActivity extends AbstractBottomNavigationActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSIONS = 2;
    private static final int REQUEST_CODE_GPS = 3;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listSelected  = getIntent().getParcelableExtra(ListProjects.KEY);
        TextView project_list_title = findViewById(R.id.project_list_title_text_view);
        project_list_title.setText(listSelected.getNameList());

        ImageView shareProjects = findViewById(R.id.share_list_image_view);
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(listSelected.getNameList());

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
            projectsAdapter.submitList(searchProjects);
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
        if (!bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth is already on ");
            checkBTPermissions();
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
                    checkBTPermissions();
                }
                else {
                    //TODO decidere se far vedere un dialog in cui spiegare all'utente che è necessario attivare il bluetooth se vuole condividere la lista
                    Log.d(TAG, "User denied to turn bluetooth on");
                }
                break;
            case REQUEST_CODE_GPS:
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (!isGpsEnabled) {
                    //TODO decidere se far vedere un dialog in cui spiegare all'utente che è necessario attivare il GPS per trovare i dispositivi vicini
                    Log.d(TAG, "User denied to turn GPS on");
                    //startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_GPS);
                }else{
                    Log.d(TAG, "GPS is on");
                    go_sharing_activity();
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

    /*
     I permessi dangerous per tutti i dispositivi che eseguono API >= 23 (Android 6.0+ MARSHMALLOW) devono essere gestiti a run-time.
     In questo caso per il Bluetooth il permesso dangerous è: ACCESS_FINE_LOCATION.
     //todo vedere se è necessario ACCESS_COARSE_LOCATION
     L'utente in qualsiasi momento può revocare tali permessi, pertanto l’ app deve verificare i permessi ogni qualvolta deve usare le risorse.

     In versioni precedenti, i permessi erano verificati solo all’installazione, quindi bastava indicarli solo nel manifest.
     */
    private void checkBTPermissions() {

        Log.d(TAG, "checkBTPermissions");

        //TODO vedere se implementare anche il permesso ACCESS_COARSE_LOCATION

        // Controlliamo se i permessi sono stati concessi
        if (/*ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&*/ ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permessi non concessi
            // Dobbiamo mostrare una spiegazione?
            if (/*ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) ||*/ ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostra una spiegazione del perchè la mancanza di questi permessi può negare alcune
                // funzionalità. Questa spiegazione può essere data con un semplice AlertDialog(). Alla riposta
                // positiva (l'utente accetta di dare i permessi) andremo a richiedere i permessi con le istruzioni
                // predefiniti (es. ActivityCompat.requestPermissions([...])
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission necessary");
                builder.setMessage("Permission is required to send your project list.");
                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ProjectsListDetailActivity.this,
                                new String [ ]{ /*Manifest.permission.ACCESS_COARSE_LOCATION,*/ Manifest.permission.ACCESS_FINE_LOCATION} ,
                                REQUEST_LOCATION_PERMISSIONS);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            } else {
                // Nessuna spiegazione da dare, richiediamo direttamente i permessi
                ActivityCompat.requestPermissions(this,
                        new String [ ]{ /*Manifest.permission.ACCESS_COARSE_LOCATION,*/ Manifest.permission.ACCESS_FINE_LOCATION } ,
                        REQUEST_LOCATION_PERMISSIONS);

                //REQUEST_LOCATION_PERMISSIONS è una costante che andremo ad utilizzare
                // nel metodo onRequestPermissionsResults([...]) per analizzare i risultati
                // ed agire di conseguenza
            }
        } else {
            // Abbiamo già i permessi, possiamo procedere con ciò che vogliamo fare
            Log.d(TAG, "already PERMISSION_GRANTED");
            checkLocationServicesIsNeededAndEnable();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                // Se la richiesta viene annullata, gli array dei risultati sono vuoti.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED /*&& grantResults[1] == PackageManager.PERMISSION_GRANTED*/) {
                    // L'autorizzazione è concessa. Continua l'azione o il flusso di lavoro nella tua app.
                    Log.d(TAG, "PERMISSION_GRANTED");
                    checkLocationServicesIsNeededAndEnable();

                }  else {
                    // Spiega all'utente che la funzione non è disponibile perché la funzione richiede
                    //un permesso che l'utente ha negato. Allo stesso tempo, rispetta la decisione dell'utente.
                    // Non collegare a impostazioni di sistema nel tentativo di convincere l'utente
                    //a modificare la propria decisione.
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission denied");
                    builder.setMessage("Without permission the app is unable to send your project list.");
                    builder.setPositiveButton("Ok", null);
                    builder.show();
                    Log.d(TAG, "PERMISSION_DENIED");
                }
                return;
        }
        // Altri case per verificare altri permessi che questa app potrebbe richiedere.
    }

    /*
    * Dalla versione android >= 10 per per cercare i dispositivi Bluetooth disponibili è necessario attivare il GPS
    * Quindi questo metodo controlla la versione di android del dispositivo se >= 10 chiede di attivare il GPS (se non attivo)
    * altrimenti prosegue con l'esecuzione
    */
    //TODO vedere se è necessario controllare che il GPS è disponibile nel dispositivo (come abbiamo fatto col Bluetooth)
    public void checkLocationServicesIsNeededAndEnable() {
        Log.d(TAG, "checkLocationServicesIsNeededAndEnable");
        //TODO controllare se anche per la versione 11 è necessario attivare la posizione per poter individuare i dispositivi disponibili
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Log.d(TAG, "android >= 10");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGpsEnabled) {
                //TODO decidere se far vedere un dialog in cui spiegare all'utente che è necessario attivare il GPS per trovare i dispositivi vicini
                Log.d(TAG, "GPS NOT ENABLE");
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_GPS);
            }else{
                  go_sharing_activity();
            }
        }else{
            //il dispositivo ha una versione android < 10 e quindi per cercare i dispositivi disponibili non è necessario attivare il GPS
            //si può procedere alla chiamata dell'activity successiva
            go_sharing_activity();
        }
    }

}