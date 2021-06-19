package it.uniba.di.sms2021.managerapp.utility;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import it.uniba.di.sms2021.managerapp.R;

public class FormUtil {
    private static final String TAG ="FormUtil" ;

    //TODO migliorare questa classe per gestire la validazione dei campi in maniera più elegante

    public static boolean validateEmailPassword(String email, String password, Context context, TextInputLayout  emailInputLayout, TextInputLayout  passwordInputLayout) {
        boolean valid = true;

        if (!isEmailValid(email,emailInputLayout, context)) {
            valid = false;
        } else {
            emailInputLayout.setError(null);
        }

        if (!isPasswordValid(password, passwordInputLayout, context)) {
            valid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return valid;
    }

    public static boolean isEmailValid(String email, TextInputLayout emailInputLayout, Context context) {
        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError( context.getResources().getString(R.string.required_field) );
            valid = false;
        } else {
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInputLayout.setError(context.getResources().getString(R.string.error_email));
                valid = false;
            }
        }
        return valid;
    }

    private static boolean isPasswordValid(String password,TextInputLayout  passwordInputLayout, Context context) {
        boolean valid = true;
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError( context.getResources().getString(R.string.required_field) );
            valid = false;
        } else {
            if(password.length() < 8){
                passwordInputLayout.setError(context.getResources().getString(R.string.error_password));
                valid = false;
            }
        }
        return valid;
    }

}
