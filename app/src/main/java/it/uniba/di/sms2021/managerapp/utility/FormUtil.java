package it.uniba.di.sms2021.managerapp.utility;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import it.uniba.di.sms2021.managerapp.R;

public class FormUtil {
    private static final String TAG ="FormUtil" ;
    private static TextInputLayout  emailInputLayout;
    private static TextInputLayout  passwordInputLayout;

    public static boolean validateEmailPassword(String email, String password, AppCompatActivity activity) {
        boolean valid = true;

        emailInputLayout = activity.findViewById(R.id.email_input_layout);
        passwordInputLayout =  activity.findViewById(R.id.password_input_layout);

        if (!isEmailValid(email, activity)) {
            valid = false;
        } else {
            emailInputLayout.setError(null);
        }

        if (!isPasswordValid(password, activity)) {
            valid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return valid;
    }

    public static boolean validateEmail(String email, AppCompatActivity activity){
        emailInputLayout = activity.findViewById(R.id.email_input_layout);
        return isEmailValid(email, activity);
    }

    private static boolean isEmailValid(String email, AppCompatActivity activity) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError( activity.getResources().getString(R.string.required_field) );
            valid = false;
        } else {
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInputLayout.setError(activity.getResources().getString(R.string.error_email));
                valid = false;
            }
        }
        return valid;
    }

    private static boolean isPasswordValid(String password, AppCompatActivity activity ) {
        boolean valid = true;
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError( activity.getResources().getString(R.string.required_field) );
            valid = false;
        } else {
            if(password.length() < 8){
                passwordInputLayout.setError(activity.getResources().getString(R.string.error_password));
                valid = false;
            }
        }
        return valid;
    }

}
