package it.uniba.di.sms2021.managerapp.utility;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Activity di base dell'applicazione che contiene behaviour contenuti in tutte le activity.<br>
 * In particolare contiene logica relativa al cambio di stato della connettività internet, con
 * relativi listener implementabili nelle singole activity.
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

        checkConnectionOnFragmentResumed();
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

    /**
     * Controlla la connessione e notifica il listener
     */
    public void checkConnection () {
        receiver.checkConnection();
    }

    /**
     * Controlla la connessione ogni volta che un frammento entra nello stato di resumed (utile
     * quando si cambia fragment e l'activity non chiama onResume)
     */
    public void checkConnectionOnFragmentResumed() {
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            /**
             * Called after the fragment has returned from the FragmentManager's call to
             * {@link Fragment#onResume()}.
             *
             * @param fm Host FragmentManager
             * @param f  Fragment changing state
             */
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                checkConnection();
            }
        }, false);
    }
}
