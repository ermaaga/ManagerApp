package it.uniba.di.sms2021.managerapp.firebase;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.login.LoginActivity;
import it.uniba.di.sms2021.managerapp.notifications.NotificationChecker;

public class LoginHelper {
    private static User user;

    public static GoogleSignInOptions getOptions (Context context) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    public static User getCurrentUser () {
        if (user == null) {
            throw new RuntimeException("Utente corrente non inizializzato. Al momento del login" +
                    " si dovrebbe usare LoginHelper.setCurrentUser() per inizializzare la variabile");
        }
        return user;
    }

    public static void setCurrentUser (User user) {
        LoginHelper.user = user;
    }

    public static void logout (Context context) {
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(context, LoginHelper.getOptions(context)).signOut();
        LoginHelper.setCurrentUser(null);

        NotificationChecker.unsubscribeCheckForNotifications(context);

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
