package it.uniba.di.sms2021.managerapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.exams.NewStudyCaseActivity;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;
import it.uniba.di.sms2021.managerapp.utility.FormUtil;

public class SignInActivity extends AbstractFormActivity {

    private static final String TAG = "SignInActivity";

    private FirebaseAuth mAuth;

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;

    ProgressBar progressBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = (TextInputEditText) findViewById(R.id.email_edit_text);
        passwordEditText = (TextInputEditText) findViewById(R.id.password_edit_text);

        emailInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    //TODO da migliorare
    public void saveNewAccount(View v) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        Log.d(TAG, "createAccount method:" + email);
       if (FormUtil.validateEmailPassword(email, password, getApplicationContext(),emailInputLayout,passwordInputLayout )) {

           progressBar.setVisibility(View.VISIBLE);
           Log.w(TAG, "is validate");

           mAuth.createUserWithEmailAndPassword(email, password)
                   .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()) {
                               Log.w(TAG, "success");
                               Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                               Intent intent = new Intent(SignInActivity.this, LoginActivity.class);
                               startActivity(intent);
                           }
                           else {
                               Log.w(TAG, "failed");
                               Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();

                           }
                       }
                   });
       }
        else{
            Log.w(TAG, "not validate");
        }
    }


    /* metodi  duplicati sia in questa activity che in LoginActivity
    per questo motivo è stata creta la classe utility FormUtil (da migliorare)

    private boolean validate(String email, String password) {
        boolean valid = true;

        if (!isEmailValid(email)) {
            valid = false;
        } else {
            emailInputLayout.setError(null);
        }

        if (!isPasswordValid(password)) {
            valid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return valid;
    }

    private boolean isPasswordValid(String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.required_field));
            valid = false;
        } else {
            if(password.length() < 8){
                passwordInputLayout.setError(getString(R.string.error_password));
                valid = false;
            }
        }
        return valid;
    }

    private boolean isEmailValid(String email) {
        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.required_field));
            valid = false;
        } else {
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInputLayout.setError(getString(R.string.error_email));
                valid = false;
            }
        }
        return valid;
    }
*/

}

