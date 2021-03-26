package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    private ProgressBar progressBar;
    private RecyclerView availableRecyclerView;
    private RecyclerView pairedRecyclerView;
    private DeviceRecyclerAdapter availableAdapter;
    private DeviceRecyclerAdapter pairedAdapter;

    private Context context;
    private ListView listMessages;
    private EditText  messageEditText;
    private Button buttonSendMessage;
    private ArrayAdapter<String> adapterMessages;
    private String connectedDevice;
    private String projectsId;

    BluetoothAdapter bluetoothAdapter;
    private BluetoothConnection bluetoothConnection;

    public ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case BluetoothConnection.MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case BluetoothConnection.STATE_NONE:
                            setState("Not Connected");
                            break;
                        case BluetoothConnection.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            setState("Connecting...");
                            break;
                        case BluetoothConnection.STATE_CONNECTED:
                            setState("Connected: " + connectedDevice);
                            break;
                    }
                    break;
                case BluetoothConnection.MESSAGE_WRITE:
                    byte[] buffer1 = (byte[]) message.obj;
                    String outputBuffer = new String(buffer1);
                    adapterMessages.add("Me: " + outputBuffer);
                    Log.d(TAG, "Me: " + outputBuffer);
                    break;
                case BluetoothConnection.MESSAGE_READ:
                    byte[] buffer = (byte[]) message.obj;
                    String inputBuffer = new String(buffer, 0, message.arg1);
                    adapterMessages.add(connectedDevice + ": " + inputBuffer);
                    Log.d(TAG, connectedDevice + ": " + inputBuffer);
                    displayListNameDialog(inputBuffer);
                    break;
                case BluetoothConnection.MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(BluetoothConnection.DEVICE_NAME);
                    Toast.makeText(context, connectedDevice, Toast.LENGTH_SHORT).show();
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

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_list_name, null);
        EditText editText = (EditText) dialogView.findViewById(R.id.editTextNameList);

        new AlertDialog.Builder(this)
                .setTitle(R.string.label_Dialog_title_name_list)
                .setView(dialogView)
                .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveListProjects(editText.getText().toString(), message);
                    }
                }).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void saveListProjects(String nameList, String message) {
        String[] messageSplit = message.split(",");

        List<String> listId = new ArrayList<>();

        for(String id: messageSplit){
            listId.add(id);
        }

        DatabaseReference listReference = FirebaseDbHelper.getListsProjectsReference(LoginHelper.getCurrentUser().getAccountId());
        DatabaseReference newElement=listReference.push();

        ListProjects list = new ListProjects( newElement.getKey(), nameList, listId );

        newElement.setValue(list).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Intent intent = new Intent(getApplicationContext(), ProjectsActivity.class);
                   startActivity(intent);
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
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by discover() method.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG, "onReceive: ACTION FOUND.");
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if(!availableDevices.contains(device)){
                    availableDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    availableAdapter.submitList(availableDevices);
                    availableAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
            }
        }
    };

    private BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED.");
                Log.d(TAG, "onReceive: new devices" + availableDevices.toString());
            }
        }
    };

    //Brodcast unico
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "onReceive: start discovery");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onReceive: discovery finish");
                Log.d(TAG, "onReceive: new devices" + availableDevices.toString());
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 /*if (device.getBondState() !=BluetoothDevice.BOND_BONDED)    {

                    near_device.add(device.getAddress());
                    */

                availableDevices.add(device);


                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                availableAdapter.submitList(availableDevices);
                availableAdapter.notifyDataSetChanged();
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
        context = this;
        progressBar=findViewById(R.id.progressBar);
        availableRecyclerView = findViewById(R.id.available_devices__recycler_view);
        pairedRecyclerView = findViewById(R.id.paired_devices_recycler_view);

        buttonSendMessage = findViewById(R.id.button_send);

        //TODO CODICE DA ELIMINARE
        listMessages = findViewById(R.id.list_conversation);
        messageEditText = findViewById(R.id.editTextMessage);
        listMessages.setVisibility(View.GONE);
        messageEditText.setVisibility(View.GONE);
        adapterMessages = new ArrayAdapter<String>(context, R.layout.list_item_message);
        listMessages.setAdapter(adapterMessages);


        projectsId = getIntent().getStringExtra(Project.KEY);
        messageEditText.setText(projectsId);


        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothConnection.write(projectsId.getBytes());
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, discoverDevicesIntent);

        IntentFilter startdiscoverIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(broadcastReceiver1, startdiscoverIntent);

        IntentFilter finishdiscoverIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver2, finishdiscoverIntent);

         /* brodcast unico
             IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            registerReceiver(mReceiver, filter);*/

        bluetoothConnection = new BluetoothConnection(context, handler);

    }

    @Override
    protected void onStart() {
        super.onStart();

        pairedAdapter = new DeviceRecyclerAdapter(new DeviceRecyclerAdapter.OnActionListener() {

            @Override
            public void onItemClicked(String address) {
                bluetoothConnection.connect(bluetoothAdapter.getRemoteDevice(address));

                Toast.makeText(getApplicationContext(), "devices selected is: "+ address, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "devices selected is: "+ address);
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
                pairedDevices.add(device);
            }

            pairedAdapter.submitList(pairedDevices);
            pairedAdapter.notifyDataSetChanged();
            Log.d(TAG, "PairedDevices: " + pairedDevices.toString());
        }else{
            Log.d(TAG, "PairedDevices: " + pairedDevices.toString());
        }

        availableAdapter = new DeviceRecyclerAdapter(new DeviceRecyclerAdapter.OnActionListener() {

            @Override
            public void onItemClicked(String address) {
                bluetoothConnection.connect(bluetoothAdapter.getRemoteDevice(address));
                    
                Toast.makeText(getApplicationContext(), "devices selected is: "+ address, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "devices selected is: "+ address);
            }
        });
        availableRecyclerView.setAdapter(availableAdapter);
        availableRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        discover();

    }

    public void discover() {
        Log.d(TAG, "discover method: Looking for available devices.");

        if(bluetoothAdapter != null) {
            // Se un'altra ricerca dei dispositivi è in corso la cancella per avviarne un'altra
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

        }

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
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(broadcastReceiver1);
        unregisterReceiver(broadcastReceiver2);

        //brodcast unico
        // unregisterReceiver(mReceiver);

        if (bluetoothConnection != null) {
            bluetoothConnection.stop();
        }
    }

}

