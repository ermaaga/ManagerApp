package it.uniba.di.sms2021.managerapp.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.exams.NewStudyCaseActivity;
import it.uniba.di.sms2021.managerapp.home.HomeActivity;
import it.uniba.di.sms2021.managerapp.utility.FormUtil;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LoginActivityTag";

    private FirebaseAuth mAuth;

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

    private FirebaseDatabase database;
    private DatabaseReference usersReference;

    private Button goCreateAccountButton;
    private Button loginEmailPassButton;
    private Button loginGoogleButton;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        goCreateAccountButton = (Button) findViewById(R.id.goButtonCreateAccount);
        loginEmailPassButton = (Button) findViewById(R.id.buttonLoginEmailPassword);
        loginGoogleButton = (Button) findViewById(R.id.buttonLoginGoogle);

        emailEditText = (TextInputEditText) findViewById(R.id.email_edit_text);
        passwordEditText = (TextInputEditText) findViewById(R.id.password_edit_text);

        emailInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        goCreateAccountButton.setOnClickListener(this);
        loginEmailPassButton.setOnClickListener(this);
        loginGoogleButton.setOnClickListener(this);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);

        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        usersReference.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (account != null) {
            checkIfUserExistsAndGoToHome(account.getId());
        }
    }

    private void checkIfUserExistsAndGoToHome(String id) {
        // Aggiunta di un listener che esegue il metodo onDataChange la prima volta che arrivano
        // i dati, o ogni volta che vengono aggiornati.
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                // Iterazione tra i vari elementi appartenenti al nodo "users"
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getKey().equals(id)) {
                        /*
                        User user = child.getValue(User.class);       //Se serve il riferimento all'utente
                        if (user.getRuolo() == User.ROLE_PROFESSOR) {

                        }
                         */

                        Log.d(TAG, "Id of child: " + child.getKey());
                        found = true;
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    }
                }

                if (!found) {
                    Intent intent = new Intent(LoginActivity.this, UserRoleActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loginGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);

            checkIfUserExistsAndGoToHome(account.getId());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
                Toast.makeText(this, R.string.error_sha1_not_found, Toast.LENGTH_SHORT).show();
            }
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLoginEmailPassword:
                loginEmailPassword(emailEditText.getText().toString(), passwordEditText.getText().toString());
                break;
            case R.id.buttonLoginGoogle:
                loginGoogle();
                break;
            case R.id.goButtonCreateAccount:
                goCreateAccount();
                break;
        }
    }

    private void goCreateAccount() {
        Intent signInIntent = new Intent(this, SignInActivity.class);
        startActivity(signInIntent);
    }

    //TODO da migliorare
    private void loginEmailPassword(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        if (FormUtil.validateEmailPassword(email, password, getApplicationContext(), emailInputLayout, passwordInputLayout )) {

            progressBar.setVisibility(View.VISIBLE);

            Log.w(TAG, "is validate");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                            }
                           progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

        }else{
            Log.w(TAG, "not validate");
        }
    }

   /* metodi  duplicati sia in questa activity che in SignInActivity
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