package it.uniba.di.sms2021.managerapp.utility;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.GoogleApi;
import com.google.firebase.auth.FirebaseAuth;

import it.uniba.di.sms2021.managerapp.NotificationsActivity;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.LoginHelper;
import it.uniba.di.sms2021.managerapp.login.LoginActivity;

public class MenuUtil {
    public static void performMainActions (Context context, int menuItemId) {
        if (menuItemId == R.id.action_notifications) {
            context.startActivity(new Intent(context, NotificationsActivity.class));
        } else if (menuItemId == R.id.action_settings) {
            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show();
        } else if (menuItemId == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(context, LoginHelper.getOptions(context)).signOut();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
}
