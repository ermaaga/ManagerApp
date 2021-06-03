 package it.uniba.di.sms2021.managerapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

 /* Questa classe si occupa di impostare e gestire le connessioni Bluetooth con altri dispositivi.
  * Ha un thread che ascolta le connessioni in entrata,
  * un thread per la connessione con un dispositivo e
  * un thread per eseguire trasmissioni di dati quando connesso.
 */
public class BluetoothConnection {
     private static final String TAG = "BluetoothConnection";

    //Tipi di messaggi inviati dall'Handler BluetoothConnection
    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    // Nomi delle chiavi ricevute dall'Handler BluetoothConnection
    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";

     // Costanti che indicano lo stato di connessione corrente
    public static final int STATE_NONE = 0; // non stiamo facendo nulla
    public static final int STATE_LISTEN = 1; // ora ascolta le connessioni in entrata
    public static final int STATE_CONNECTING = 2; // ora sta avviando una connessione in uscita
    public static final int STATE_CONNECTED = 3; // ora connesso a un dispositivo remoto

    // UUID univoco per questa applicazione
    private final UUID APP_UUID = UUID.fromString("4d4dead6-4db8-4f0c-881c-b35df17ab418");
    // Nome per il record SDP durante la creazione del socket del server
    private final String APP_NAME = "ManagerApp";

    private Context context;
    private final Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private int state;

    public BluetoothConnection(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        state = STATE_NONE;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized int getState() {
        return state;
    }

    // Aggiorna lo stato della connessione e assegna il nuovo stato all'Handler in modo che l'activity ProjectsSharingActivity
    // possa essere aggiornata con lo stato corrente della connessione
     public synchronized void setState(int state) {
        Log.d(TAG, "setstate " + getState() + " -> " + state);
        this.state = state;
    }

     // Assegna il nuovo stato all'Handler in modo che l'activity ProjectsSharingActivity
     // possa essere aggiornata con lo stato corrente della connessione
     public synchronized void updateUserInterface() {
         handler.obtainMessage(MESSAGE_STATE_CHANGED, state, -1).sendToTarget();
     }

     /*
     * Avvia il servizio di connessione Bluetooth.
     * In particolare, avvia AcceptThread per iniziare una sessione in modalità di ascolto (server).
     */
    public synchronized void start() {
        Log.d(TAG, "BluetoothConnection START");

        // Annulla qualsiasi thread che tenti di stabilire una connessione
        if (connectThread != null) {
            Log.d(TAG, "BluetoothConnection START: connectThread != null ");
            connectThread.cancel();
            connectThread = null;
        }

        // Annulla qualsiasi thread che attualmente esegue una connessione
        if (connectedThread != null) {
            Log.d(TAG, "BluetoothConnection START: connectedThread != null ");
            connectedThread.cancel();
            connectedThread = null;
        }

        // Avvia il thread per ascoltare su un BluetoothServerSocket
        if (acceptThread == null) {
            Log.d(TAG, "BluetoothConnection START: acceptThread == null ");
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        updateUserInterface();
    }

    //Interrompe tutti i thread
    public synchronized void stop() {
        Log.d(TAG, "BluetoothConnection STOP");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        setState(STATE_NONE);
        updateUserInterface();
    }

    /*
    * Avvia ConnectThread (client) per avviare una connessione a un dispositivo remoto.
    */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "BluetoothConnection connect");

        // Annulla qualsiasi thread che tenti di stabilire una connessione
       if (state == STATE_CONNECTING) {
           if (connectThread != null) {
               connectThread.cancel();
               connectThread = null;
           }
        }

        // Annulla qualsiasi thread che attualmente esegue una connessione
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Avvia il thread per connettersi con il dispositivo specificato
        connectThread = new ConnectThread(device);
        connectThread.start();

        updateUserInterface();
    }

    //Scrivi in​ConnectedThread in modo non sincronizzato
    public void write(byte[] buffer) {
        Log.d(TAG, "BluetoothConnection write");

        //Crea un oggetto temporaneo
        ConnectedThread connThread;

        // Sincronizza una copia di ConnectedThread
        synchronized (this) {
            if (state != STATE_CONNECTED) {
                return;
            }

            connThread = connectedThread;
        }

       // Esegue la scrittura non sincronizzata
        connThread.write(buffer);
    }

    /* Componente Server
     * Questo thread ascolta le connessioni in entrata.
     * Funziona fino a quando non viene accettata una connessione
     * (o fino all'annullamento).
     */
    private class AcceptThread extends Thread {
        // Server socket locale
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            //Utilizza un oggetto temporaneo che viene successivamente assegnato a serverSocket (definitivo)
            BluetoothServerSocket tmp = null;

            // Crea un nuovo socket del server in ascolto
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
            } catch (IOException e) {
                //Può lanciare un'eccezione in caso di errore, ad esempio Bluetooth non disponibile, o autorizzazioni insufficienti o canale in uso.
                Log.e(TAG,"Accept->Constructor"+ e.toString());
            }

            serverSocket = tmp;

            setState(STATE_LISTEN);
        }

        public void run() {
            Log.d(TAG, "AcceptThread RUN");
            BluetoothSocket socket = null;

             //Ascolta il socket del server se non siamo connessi
            while (state != STATE_CONNECTED){
                try {
                    /* Ascolta le richieste di connessione
                     * Chiamata bloccante continua ad ascoltare fino a quando non si verifica un'eccezione o viene accetta una connessione.
                     * la connessione è accettata se il dispositivo ha mandato un UUID che corrisponde con quello registrato,
                     * in caso di successo sarà restituito un BluetoothSocket connesso.
                    */
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG,"Accept->Run" + e.toString());
                    break;
                }

                Log.d(TAG,"prima if accept " + state);
               // Se è stata accettata una connessione
                if (socket != null) {
                    synchronized (BluetoothConnection.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                Log.d(TAG,"STATE_CONNECTING " + state);
                                //Avvia ConnectedThread (per il trasferimento dei dati)
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                Log.d(TAG,"STATE_CONNECTED " + state);
                                // O non pronto o già connesso. Termina il nuovo socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Accept->CloseSocket" + e.toString());
                                }
                                break;
                        }
                    }
                }
            }

        }

        // Chiude il socket di connessione.
        public void cancel() {
            Log.d(TAG, "AcceptThread CANCEL");
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Accept->CloseServer" + e.toString());
            }
        }
    }

    /*
     * Componente client
     * Questo thread viene eseguito durante il tentativo di stabilire
     * una connessione in uscita con un dispositivo.
     * la connessione riesce o fallisce.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;

            BluetoothSocket tmp = null;

            //Crea un RFCOMM BluetoothSocket pronto per avviare una connessione sicura con il BluetoothDevice specificato
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e(TAG,"Connect->Constructor" + e.toString());
            }

            socket = tmp;

            setState(STATE_CONNECTING);
        }


        public void run() {
            Log.d(TAG, "ConnectThread RUN");

            //Annulla sempre il rilevamento di nuovi dispositivi perché rallenta la connessione
            bluetoothAdapter.cancelDiscovery();

            try {
               /* Effettua una connessione al BluetoothSocket
                * Chiamata bloccante. Si bloccherà fino a quando non viene stabilita
                * una connessione o la connessione non riesce. Il sistema fa una verifica sull’UUID.
                * Se la connessione non riesce genera un'eccezione.
                * Se il metodo connect ha successo il socket è connesso.
               */
                socket.connect();
            } catch (IOException e) {
                Log.e(TAG,"Connect->Run" + e.toString());
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.e(TAG,"Connect->CloseSocket" + e.toString());
                }
                connectionFailed();
                return;
            }

            //Reimposta ConnectThread perché abbiamo finito
            synchronized (BluetoothConnection.this) {
                connectThread = null;
            }

            //Avvia ConnectedThread
            connected(socket, device);
        }

        public void cancel() {
            Log.d(TAG, "ConnectThread CANCEL");
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG,"Connect->Cancel" + e.toString());
            }
        }
    }

    /*
     * Questo thread viene eseguito durante una connessione con un dispositivo remoto.
     * Gestisce l'invio e la ricezione dei dati tramite stream di input/output.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Ottiene i flussi di input e di output associati al socket
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG,"temp sockets not created" + e.toString());
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

            setState(STATE_CONNECTED);
        }

        public void run() {
            Log.d(TAG, "ConnectedThread RUN");
            byte[] buffer = new byte[1024];
            int bytes;


            // Continua ad ascoltare InputStream mentre è connesso
              while (state == STATE_CONNECTED) {
                try {
                    //Legge da InputStream
                    bytes = inputStream.read(buffer);

                    //Invia i byte ottenuti all'activity: ProjectsSharingActivity
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                   break;
                }
            }
        }

        //Scrive sull'OutStream, invia i dati al dispositivo remoto
        public void write(byte[] buffer) {
            Log.d(TAG, "ConnectedThread WRITE");
            try {
                outputStream.write(buffer);
                //Condivide il messaggio inviato nell'activity: ProjectsSharingActivity
                handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            Log.d(TAG, "ConnectedThread CANCEL");
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    //Indica che la connessione è stata persa e invia una notifica all'activity: ProjectsSharingActivity
    private void connectionLost() {
        Log.d(TAG, "connectionLost()");
        //Invia un messaggio di errore all'activity: ProjectsSharingActivity
        Message message = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, context.getString(R.string.text_message_bluetooth_connection_lost));
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_NONE);
        updateUserInterface();

        BluetoothConnection.this.stop();
        //Riavvia il servizio per riavviare la modalità di ascolto
        BluetoothConnection.this.start();
    }

     //Indica che il tentativo di connessione non è riuscito e invia una notifica all'activity: ProjectsSharingActivity
     private void connectionFailed() {
        Log.d(TAG, "connectionFailed()");
        // Invia un messaggio di errore all'activity: ProjectsSharingActivity
        Message message = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, context.getString(R.string.text_message_cant_connect_bluetooth_device));
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_NONE);
        updateUserInterface();

        BluetoothConnection.this.stop();
        //Riavvia il servizio per riavviare la modalità di ascolto
        BluetoothConnection.this.start();
    }

    //Avvia ConnectedThread per iniziare a gestire una connessione Bluetooth
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected()");

        //Annulla il thread che ha completato la connessione
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        //Annulla il thread che attualmente esegue una connessione
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        //Annulla acceptThread perché vogliamo connetterci solo a un dispositivo
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // Avvia il thread per gestire la connessione ed eseguire lo scambio dei dati
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        //Invia il nome del dispositivo connesso all'activity: ProjectsSharingActivity
        Message message = handler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);

        updateUserInterface();
    }

}
