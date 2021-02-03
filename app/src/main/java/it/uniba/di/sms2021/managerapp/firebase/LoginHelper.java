package it.uniba.di.sms2021.managerapp.firebase;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import it.uniba.di.sms2021.managerapp.R;

public class LoginHelper {
    public static GoogleSignInOptions getOptions (Context context) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }
}
