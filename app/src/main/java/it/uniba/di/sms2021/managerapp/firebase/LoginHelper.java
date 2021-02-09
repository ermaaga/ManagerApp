package it.uniba.di.sms2021.managerapp.firebase;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;

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
                    "si dovrebbe usare LoginHelper.setCurrentUser() per inizializzare la variabile");
        }
        return user;
    }

    public static void setCurrentUser (User user) {
        LoginHelper.user = user;
    }
}
