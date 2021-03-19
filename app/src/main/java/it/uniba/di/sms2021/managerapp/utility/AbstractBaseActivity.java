package it.uniba.di.sms2021.managerapp.utility;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity di base dell'applicazione che contiene behaviour contenuti in tutte le activity
 */
public class AbstractBaseActivity extends AppCompatActivity {
    private ConnectionCheckBroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        receiver = new ConnectionCheckBroadcastReceiver(new ConnectionCheckBroadcastReceiver.OnConnectionChangeListener() {
            @Override
            public void onConnectionUp() {
                AbstractBaseActivity.this.onConnectionUp();
            }

            @Override
            public void onConnectionDown() {
                AbstractBaseActivity.this.onConnectionDown();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionCheckBroadcastReceiver.registerReceiver(this, receiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        ConnectionCheckBroadcastReceiver.removeReceiver(this, receiver);
    }

    protected void onConnectionUp () {
        // Metodo vuoto che lascio per override nelle altre classi
    }

    protected void onConnectionDown () {
        ConnectionCheckBroadcastReceiver.showConnectivitySnackbar(
                AbstractBaseActivity.this, getWindow());
    }
}
