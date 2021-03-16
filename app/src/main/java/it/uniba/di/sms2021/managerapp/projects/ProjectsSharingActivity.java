package it.uniba.di.sms2021.managerapp.projects;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.lists.DeviceRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;

public class ProjectsSharingActivity extends AbstractBottomNavigationActivity {

    private static final String TAG = "ProjectsSharingActivity";

    ProgressBar progressBar;
    private RecyclerView recyclerView;
    private DeviceRecyclerAdapter adapter;

    BluetoothAdapter bluetoothAdapter;

    public ArrayList<String> devices = new ArrayList<>();

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
                if(!devices.contains(device.getAddress() + ", " + device.getName())){
                    devices.add(device.getAddress() + ", " + device.getName());
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    adapter.submitList(devices);
                    adapter.notifyDataSetChanged();
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
                Log.d(TAG, "onReceive: new devices" + devices.toString());
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
                Log.d(TAG, "onReceive: new devices" + devices.toString());
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 /*if (device.getBondState() !=BluetoothDevice.BOND_BONDED)    {

                    near_device.add(device.getAddress());
                    */

                devices.add(device.getAddress() + ", " + device.getName());


                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                adapter.submitList(devices);
                adapter.notifyDataSetChanged();
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

        progressBar=findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.devices_recycler_view);

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

    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO vedere se aggiungere icona dispositivo nell'adapter e quindi nel layout oppure
        // se avere una semplice stringa e quindi togliere questo adapter e utilizzare stringAdapter

        adapter = new DeviceRecyclerAdapter(new DeviceRecyclerAdapter.OnActionListener() {

            @Override
            public void onItemClicked(String string) {
                Toast.makeText(getApplicationContext(), "devices selected is: "+ string, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "devices selected is: "+ string);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
    }

}

