package it.uniba.di.sms2021.managerapp.utility;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseAuth;

import it.uniba.di.sms2021.managerapp.NotificationsActivity;
import it.uniba.di.sms2021.managerapp.ProfileActivity;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.login.LoginActivity;

public class MenuUtil {
    public static void performMainActions (Context context, int menuItemId) {
        if (menuItemId == R.id.action_notifications) {
            context.startActivity(new Intent(context, NotificationsActivity.class));
        } else if (menuItemId == R.id.action_settings) {
            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show();
        } else if (menuItemId == R.id.action_profile) {
            Intent intent = new Intent(context, ProfileActivity.class);
            context.startActivity(intent);
        } else if (menuItemId == R.id.action_logout) {
            LoginHelper.logout(context);
        }
    }
}
