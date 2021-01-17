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
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.exams.NewStudyCaseActivity;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;
import it.uniba.di.sms2021.managerapp.utility.FormUtil;

public class SignInActivity extends AbstractFormActivity {

    private static final String TAG = "SignInActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText surnameEditText;

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout nameInputLayout;
    private TextInputLayout surnameInputLayout;

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
        nameEditText = (TextInputEditText) findViewById(R.id.name_edit_text);
        surnameEditText = (TextInputEditText) findViewById(R.id.surname_edit_text);

        emailInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);
        nameInputLayout = (TextInputLayout) findViewById(R.id.name_input_layout);
        surnameInputLayout = (TextInputLayout) findViewById(R.id.surname_input_layout);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    public void saveNewAccount(View v) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();

        Log.d(TAG, "createAccount method:" + email);
       if (FormUtil.validateEmailPassword(email, password, getApplicationContext(), emailInputLayout,passwordInputLayout )) {

           progressBar.setVisibility(View.VISIBLE);
           Log.d(TAG, "is validate");

           mAuth.createUserWithEmailAndPassword(email, password)
                   .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()) {

                               User user = new User(
                                       FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                       name,
                                       surname,
                                       email
                               );

                               database = FirebaseDbHelper.getDBInstance();
                               usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);

                               usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                       .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       progressBar.setVisibility(View.INVISIBLE);

                                       if (task.isSuccessful()) {
                                           Log.d(TAG, getString(R.string.registration_success));
                                           Toast.makeText(SignInActivity.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                                       } else {
                                           Log.e(TAG, getString(R.string.registration_failed));
                                           Toast.makeText(SignInActivity.this, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });

                               sendEmailVerification();

                               Intent intent = new Intent(SignInActivity.this, LoginActivity.class);
                               startActivity(intent);
                           }
                           else {
                               Log.e(TAG, getString(R.string.registration_failed));
                               Log.e(TAG, task.getException().getMessage());
                               Toast.makeText(SignInActivity.this, getString(R.string.registration_failed), Toast.LENGTH_LONG).show();
                           }
                       }
                   });
       }
        else{
            Log.e(TAG, "not validate");
        }
    }

    private void sendEmailVerification() {

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, getString(R.string.send_verification_email_success));
                            Toast.makeText(SignInActivity.this, getString(R.string.send_verification_email_success) + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, getString(R.string.send_verification_email_failed));
                            Log.e(TAG, task.getException().getMessage());
                            Toast.makeText(SignInActivity.this, getString(R.string.send_verification_email_failed), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

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

