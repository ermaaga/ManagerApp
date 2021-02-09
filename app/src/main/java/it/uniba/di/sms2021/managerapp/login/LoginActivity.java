package it.uniba.di.sms2021.managerapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.home.HomeActivity;
import it.uniba.di.sms2021.managerapp.utility.FormUtil;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LoginActivityTag";

    private FirebaseAuth mAuth;

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount googleSignInAccount;

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


        gso = LoginHelper.getOptions(this);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        usersReference.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!=null && googleSignInAccount == null){
            checkIfUserExistsAndGoToHome(mAuth.getCurrentUser().getUid());
        }

        if (googleSignInAccount != null) {
            loginWithGoogleCredentials();
        }
    }

    private void checkIfUserExistsAndGoToHome(String id) {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User is null, please check sign in method.");
        }

        // Aggiunta di un listener che esegue il metodo onDataChange la prima volta che arrivano
        // i dati, o ogni volta che vengono aggiornati.
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        User user = child.getValue(User.class);

                        /*Se non ha effettuato l'accesso con Google l'utente è presente sia in Authentication
                         * che in Realtime Database senza ruolo e corso quindi deve permettere la scelta di quest'ultimi
                         */
                        if(user.getRuolo() == 0){
                            Intent intent = new Intent(LoginActivity.this, UserRoleActivity.class);
                            startActivity(intent);
                        }else{
                            // Setta l'utente attuale in una variabile accessibile nel resto dell'applicazione
                            LoginHelper.setCurrentUser(user);
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }

                    }
                }
                /*se ha effettuato l'accesso con Google l'utente è presente solo in Authentication
                 * quindi deve permettere la scelta del ruolo e del corso e salvare l'utente in Realtime Database
                 */
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
            googleSignInAccount = completedTask.getResult(ApiException.class);

            //Mi ero scordato di fare effettivamente l'accesso a firebase, così da poter usare
            //auth nelle regole del db.
            //TODO rimuovere questo commento in seguito
            loginWithGoogleCredentials();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
                Toast.makeText(this, R.string.error_sha1_not_found, Toast.LENGTH_SHORT).show();
            }
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void loginWithGoogleCredentials() {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(),
                null);
        loginWithCredentials(authCredential);
    }

    private void loginWithCredentials(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                checkIfUserExistsAndGoToHome(mAuth.getUid());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO aggiungere messaggio di errore visibile
                Log.i(TAG, "Login failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonLoginEmailPassword) {
            loginEmailPassword(emailEditText.getText().toString(), passwordEditText.getText().toString());
        } else if (id == R.id.buttonLoginGoogle) {
            loginGoogle();
        } else if (id == R.id.goButtonCreateAccount) {
            goCreateAccount();
        } else {
            throw new IllegalStateException("Unexpected value: " + v.getId());
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

            Log.d(TAG, "is validate");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (task.isSuccessful()) {
                                Log.d(TAG, getString(R.string.login_success));
                                Toast.makeText(getApplicationContext(), getString(R.string.login_success), Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Log.e(TAG, getString(R.string.login_failed));
                                Log.e(TAG, task.getException().getMessage());
                                Toast.makeText(getApplicationContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                            }

                        }
                    });

        }else{
            Log.e(TAG, "not validate");
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