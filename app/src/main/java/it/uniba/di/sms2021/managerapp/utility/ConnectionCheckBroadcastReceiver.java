package it.uniba.di.sms2021.managerapp.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.google.android.material.snackbar.Snackbar;

import it.uniba.di.sms2021.managerapp.R;

public class ConnectionCheckBroadcastReceiver extends BroadcastReceiver {
    private OnConnectionDownListener listener;

    public ConnectionCheckBroadcastReceiver(OnConnectionDownListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        NetworkInfo info = (NetworkInfo) extras
                .getParcelable("networkInfo");

        NetworkInfo.State state = info.getState();
        Log.d("ConnectionCheckBroadRec", info.toString() + " "
                + state.toString());

        if (state == NetworkInfo.State.CONNECTED) {
            // Connection up
        } else {
            listener.onConnectionDown();
        }
    }

    public static void registerReceiver (Context context, ConnectionCheckBroadcastReceiver receiver) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, intentFilter);
    }

    public static void removeReceiver (Context context, ConnectionCheckBroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public static void showConnectivitySnackbar (Context context, Window activityWindow) {
        Snackbar.make(activityWindow.getDecorView().findViewById(android.R.id.content),
                R.string.text_message_connection_down, Snackbar.LENGTH_LONG)
                .setAction(R.string.text_button_check_connection, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                            context.startActivity(panelIntent);
                        } else {
                            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }
                })
                .show();
    }

    public interface OnConnectionDownListener {
        void onConnectionDown();
    }
}
