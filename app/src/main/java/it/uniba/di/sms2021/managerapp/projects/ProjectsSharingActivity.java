  package it.uniba.di.sms2021.managerapp.projects;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.BluetoothConnection;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ListProjects;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.DeviceRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;

public class ProjectsSharingActivity extends AbstractBottomNavigationActivity {

    private static final String TAG = "ProjectsSharingActivity";
    private static final int REQUEST_LOCATION_PERMISSIONS = 2;
    private static final int REQUEST_CODE_GPS = 3;
    private static final int REQUEST_CODE_DISCOVERABLE = 4;

    private ProgressBar progressBar;
    private RecyclerView availableRecyclerView;
    private RecyclerView pairedRecyclerView;
    private DeviceRecyclerAdapter availableAdapter;
    private DeviceRecyclerAdapter pairedAdapter;

    private TextView stateAvailableMessageTextView;
    private TextView emptyPairedMessageTextView;

    private Context context;
    private Button buttonSendMessage;
    private Button buttonSearch;
    private Button buttonDiscoverable;
    private String connectedDevice;
    private String projectsId;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnection bluetoothConnection = null;

    public ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case BluetoothConnection.MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case BluetoothConnection.STATE_NONE:
                            setState(getString(R.string.activity_subtitle_not_connected_bluetooth));
                            break;
                        case BluetoothConnection.STATE_LISTEN:
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            setState(getString(R.string.activity_subtitle_connecting_bluetooth));
                            break;
                        case BluetoothConnection.STATE_CONNECTED:
                            setState(getString(R.string.activity_subtitle_connected_bluetooth) +  " " + connectedDevice);
                            break;
                    }
                    break;
                case BluetoothConnection.MESSAGE_WRITE:
                    byte[] outputBuffer = (byte[]) message.obj;
                    String outputMessage = new String(outputBuffer);
                    Log.d(TAG, "MESSAGE_WRITE Me: " + outputMessage);
                    break;
                case BluetoothConnection.MESSAGE_READ:
                    byte[] inputBuffer = (byte[]) message.obj;
                    String inputMessage = new String(inputBuffer, 0, message.arg1);
                    Log.d(TAG, "MESSAGE_READ " + connectedDevice + ": " + inputMessage);
                    displayListNameDialog(inputMessage);
                    break;
                case BluetoothConnection.MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(BluetoothConnection.DEVICE_NAME);
                    break;
                case BluetoothConnection.MESSAGE_TOAST:
                    Toast.makeText(context, message.getData().getString(BluetoothConnection.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void setState(CharSequence state) {
        getSupportActionBar().setSubtitle(state);
    }

    private void displayListNameDialog (String message) {
        Log.d(TAG, "displayListNameDialog");
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_list_name, null);
        EditText editText = (EditText) dialogView.findViewById(R.id.editTextNameList);
        CheckBox viewListCheckbox = (CheckBox) dialogView.findViewById(R.id.checkBoxViewList);

        new AlertDialog.Builder(this)
                .setTitle(R.string.label_Dialog_title_name_list)
                .setView(dialogView)
                .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Boolean wantsViewList = viewListCheckbox.isChecked();
                        saveListProjects(editText.getText().toString(), message, wantsViewList);
                    }
                }).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void saveListProjects(String nameList, String message, Boolean wantsViewList) {
        Log.d(TAG, "saveListProjects");
        String[] messageSplit = message.split(",");

        List<String> listId = new ArrayList<>();

        for(String id: messageSplit){
            listId.add(id);
        }

        DatabaseReference listReference = FirebaseDbHelper.getReceivedProjectListsReference(LoginHelper.getCurrentUser().getAccountId());
        DatabaseReference newElement=listReference.push();

        ListProjects list = new ListProjects( newElement.getKey(), nameList, listId );

        newElement.setValue(list).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Toast.makeText(ProjectsSharingActivity.this, getString(R.string.text_message_list_saving_success, nameList), Toast.LENGTH_SHORT).show();

                   if(wantsViewList){
                       bluetoothConnection.stop();
                       Intent intent = new Intent(getApplicationContext(), ProjectsActivity.class);
                       startActivity(intent);
                   }

               }
            }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProjectsSharingActivity.this, R.string.text_message_list_saving_failed, Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Broadcast Receiver per quando trova un dispositivo disponibile
     */
    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "onReceive: ACTION FOUND. Name device: "+ device.getName());
                if(!availableDevices.contains(device)){

                    if(device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER ||
                       device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE){

                       if(availableDevices.size() == 0){
                            stateAvailableMessageTextView.setVisibility(View.GONE);
                            availableRecyclerView.setVisibility(View.VISIBLE);
                        }

                        availableDevices.add(device);
                        Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                        availableAdapter.submitList(availableDevices);
                        availableAdapter.notifyDataSetChanged();
                    }

                }
            }
        }
    };

    /**
     * Broadcast Receiver per quando inizia la ricerca dei dispositivi disponibili
     */
    private BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
            }
        }
    };

    /**
     * Broadcast Receiver per quando finisce la ricerca dei dispositivi disponibili
     */
    private BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED.");
                progressBar.setVisibility(View.GONE);
                if(availableDevices.size() == 0) {
                    stateAvailableMessageTextView.setVisibility(View.VISIBLE);
                    availableRecyclerView.setVisibility(View.GONE);
                    stateAvailableMessageTextView.setText(R.string.text_message_available_devices_empty);
                }
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED. new devices:" + availableDevices.toString() + " " + availableDevices.isEmpty());
            }
        }
    };

    /**
     * Broadcast Receiver per le modifiche apportate alla rilevabilità del dispositivo
     */
    private final BroadcastReceiver broadcastReceiver4 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcastReceiver3: SCAN_MODE_CONNECTABLE_DISCOVERABLE Il dispositivo è in modalità rilevabile.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcastReceiver3: SCAN_MODE_CONNECTABLE Il dispositivo non è in modalità rilevabile ma può comunque ricevere connessioni.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "broadcastReceiver3: SCAN_MODE_NONE Il dispositivo non è in modalità rilevabile e non può ricevere connessioni.");
                        break;
                }

            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_projects_sharing;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        context = getApplicationContext();
        progressBar = findViewById(R.id.progressBar);
        availableRecyclerView = findViewById(R.id.available_devices_recycler_view);
        pairedRecyclerView = findViewById(R.id.paired_devices_recycler_view);

        stateAvailableMessageTextView = findViewById(R.id.available_devices_empty_state_message_text_view);
        emptyPairedMessageTextView = findViewById(R.id.paired_devices_empty_state_message_text_view);

        buttonSendMessage = findViewById(R.id.button_send);
        buttonSearch = findViewById(R.id.button_search);
        buttonDiscoverable = findViewById(R.id.button_discoverable);

        projectsId = getIntent().getStringExtra(Project.KEY);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBTPermissions();
            }
        });

        buttonDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivityForResult(discoverableIntent,REQUEST_CODE_DISCOVERABLE);
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.text_message_already_discoverable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Controlla se il dispositivo è effettivamente connesso prima di provare a inviare la lista dei progetti
                if (bluetoothConnection.getState() == BluetoothConnection.STATE_CONNECTED) {
                    bluetoothConnection.write(projectsId.getBytes());
                }else{
                    Toast.makeText(context, R.string.text_message_not_connected_bluetooth_device, Toast.LENGTH_LONG).show();
                }

            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver1, discoverDevicesIntent);

        IntentFilter startdiscoverIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(broadcastReceiver2, startdiscoverIntent);

        IntentFilter finishdiscoverIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver3, finishdiscoverIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiver4, intentFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        Log.d(TAG, "onStart dispositivi disponibili "+ availableDevices.toString());
        pairedAdapter = new DeviceRecyclerAdapter(new DeviceRecyclerAdapter.OnActionListener() {

            @Override
            public void onItemClicked(String address) {
                connectIfBluetoothEnabled(address);
            }
        });

        pairedRecyclerView.setAdapter(pairedAdapter);
        pairedRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        pairedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Set<BluetoothDevice> setPairedDevices = bluetoothAdapter.getBondedDevices();

        // Se ci sono dispositivi già abbinati
        if (setPairedDevices.size() > 0) {
            for (BluetoothDevice device : setPairedDevices) {
                if(!pairedDevices.contains(device)){
                    if(device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER ||
                        device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE){

                        if(pairedDevices.size() == 0){
                            pairedRecyclerView.setVisibility(View.VISIBLE);
                        }

                        pairedDevices.add(device);
                    }
                }
            }
            pairedAdapter.submitList(pairedDevices);
            Log.d(TAG, "PairedDevices: " + pairedDevices.toString());
        }else{
            emptyPairedMessageTextView.setVisibility(View.VISIBLE);
            emptyPairedMessageTextView.setText(R.string.text_message_paired_devices_empty);
            Log.d(TAG, "PairedDevices empty: " + pairedDevices.toString());
        }

        availableAdapter = new DeviceRecyclerAdapter(new DeviceRecyclerAdapter.OnActionListener() {

            @Override
            public void onItemClicked(String address) {
                connectIfBluetoothEnabled(address);
            }
        });
        availableRecyclerView.setAdapter(availableAdapter);
        availableRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Visualizza i dispositivi disponibili eventualmente ricercati in precedenza se si ritornata da onStop()
         if(availableDevices.size() != 0){
            availableAdapter.submitList(availableDevices);
         }

        //Se il bluetooth è abilitato e bluetoothConnection non è stato inizializzato lo inizializza e avvia bluetoothConnection
        if (bluetoothAdapter.isEnabled() && bluetoothConnection == null) {
            bluetoothConnection = new BluetoothConnection(context, handler);
            bluetoothConnection.start();
        }

        //Codice per stampare nei log lo stato della rilevabilità attuale
        int mode = bluetoothAdapter.getScanMode();

        switch (mode) {
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                Log.d(TAG, "onStart: SCAN_MODE_CONNECTABLE_DISCOVERABLE Il dispositivo è in modalità rilevabile.");
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                Log.d(TAG, "onStart: SCAN_MODE_CONNECTABLE Il dispositivo non è in modalità rilevabile ma può comunque ricevere connessioni.");
                break;
            case BluetoothAdapter.SCAN_MODE_NONE:
                Log.d(TAG, "onStart: SCAN_MODE_NONE Il dispositivo non è in modalità rilevabile e non può ricevere connessioni.");
                break;
        }

    }

    private void connectIfBluetoothEnabled(String address) {

        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth disabilitato per favore abilitalo per connetterti al dispositivo");
            Toast.makeText(context, getString(R.string.text_message_enable_bluetooth), Toast.LENGTH_LONG).show();
        }else{
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            if(bluetoothConnection.getState() == BluetoothConnection.STATE_CONNECTED){
                bluetoothConnection.stop();
                bluetoothConnection.start();
            }
            bluetoothConnection.connect(bluetoothAdapter.getRemoteDevice(address));

            Log.d(TAG, "devices selected is: "+ address);
        }

    }


    /*
     I permessi dangerous per tutti i dispositivi che eseguono API >= 23 (Android 6.0+ MARSHMALLOW) devono essere gestiti a run-time.
     In questo caso per il Bluetooth il permesso dangerous è: ACCESS_FINE_LOCATION.
     L'utente in qualsiasi momento può revocare tali permessi, pertanto l’ app deve verificare i permessi ogni qualvolta deve usare le risorse.

     In versioni precedenti, i permessi erano verificati solo all’installazione, quindi bastava indicarli solo nel manifest.
     */
    private void checkBTPermissions() {

        Log.d(TAG, "checkBTPermissions");

        // Controlliamo se i permessi sono stati concessi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permessi non concessi
            // Dobbiamo mostrare una spiegazione?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostra una spiegazione del perchè la mancanza di questi permessi può negare alcune
                // funzionalità. Alla riposta positiva (l'utente accetta di dare i permessi)
                // andremo a richiedere i permessi.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.label_Dialog_title_gps_permission_necessary);
                builder.setMessage(R.string.label_Dialog_message_gps_permission_necessary);
                builder.setPositiveButton(R.string.text_button_retry, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ProjectsSharingActivity.this,
                                new String [ ]{Manifest.permission.ACCESS_FINE_LOCATION} ,
                                REQUEST_LOCATION_PERMISSIONS);
                    }
                });
                builder.setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            } else {
                // Nessuna spiegazione da dare, richiediamo direttamente i permessi
                ActivityCompat.requestPermissions(this,
                        new String [ ]{ Manifest.permission.ACCESS_FINE_LOCATION } ,
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // L'autorizzazione è concessa. Continua il flusso di lavoro dell'app.
                    Log.d(TAG, "PERMISSION_GRANTED");
                    checkLocationServicesIsNeededAndEnable();

                }  else {
                    // Spiega all'utente che la funzione non è disponibile perché la funzione richiede
                    //un permesso che l'utente ha negato. Allo stesso tempo, rispetta la decisione dell'utente.
                    // Non collegare a impostazioni di sistema nel tentativo di convincere l'utente
                    //a modificare la propria decisione.
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.label_Dialog_title_gps_permission_denied);
                    builder.setMessage(R.string.label_Dialog_message_gps_permission_denied);
                    builder.setPositiveButton(R.string.text_button_ok, null);
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
    public void checkLocationServicesIsNeededAndEnable() {
        Log.d(TAG, "checkLocationServicesIsNeededAndEnable");
        //TODO controllare se anche per la versione 11 è necessario attivare la posizione per poter individuare i dispositivi disponibili
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Log.d(TAG, "android >= 10");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGpsEnabled) {
                Log.d(TAG, "GPS NOT ENABLE");

                new AlertDialog.Builder(this)
                        .setMessage(R.string.label_Dialog_turn_on_gps)
                        .setPositiveButton(R.string.text_button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_GPS);
                            }
                        }).setNeutralButton(R.string.text_button_no_thanks, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

            }else{
               discover();
            }
        }else{
            //il dispositivo ha una versione android < 10 e quindi per cercare i dispositivi disponibili non è necessario attivare il GPS
            //si può procedere con la ricerca dei dispositivi
            discover();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        switch (requestCode){
            case REQUEST_CODE_GPS:
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (!isGpsEnabled) {
                    Toast.makeText(getApplicationContext(), getString(R.string.text_message_gps_not_turned_on), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User denied to turn GPS on");
                }else{
                    Log.d(TAG, "GPS is on");
                    discover();
                }
                break;

            case REQUEST_CODE_DISCOVERABLE:
                if(resultCode!=RESULT_CANCELED){
                    Toast.makeText(getApplicationContext(), getString(R.string.text_message_discoverability_turned_on), Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public void discover() {
        Log.d(TAG, "discover method: Looking for available devices.");

        if(bluetoothAdapter != null) {
            // Se un'altra ricerca dei dispositivi è in corso la cancella per avviarne un'altra
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            availableDevices.clear();
            availableRecyclerView.setVisibility(View.GONE);

            bluetoothAdapter.startDiscovery();

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called.");

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: called.");
        super.onPause();
        if(bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }


    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }

        unregisterReceiver(broadcastReceiver1);
        unregisterReceiver(broadcastReceiver2);
        unregisterReceiver(broadcastReceiver3);
        unregisterReceiver(broadcastReceiver4);

        if (bluetoothConnection != null) {
            bluetoothConnection.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called.");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: called.");
    }

}

