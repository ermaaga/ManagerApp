package it.uniba.di.sms2021.managerapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.AbstractBaseActivity;
import it.uniba.di.sms2021.managerapp.utility.FormUtil;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ForgotPasswordActivity extends AbstractBaseActivity{

    private static final String TAG = "ForgotPasswordActivity";

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        MenuUtil.setIncludedToolbar(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_clear_24);

        emailInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        emailEditText = (TextInputEditText) findViewById(R.id.email_edit_text);

        mAuth = FirebaseAuth.getInstance();
    }

    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void sendEmail(View view) {

        String email=emailEditText.getText().toString();

        if(FormUtil.isEmailValid(email, emailInputLayout, getApplicationContext())){
            emailInputLayout.setError(null);
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), getString(R.string.test_message_reset_email) , Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.test_message_email_not_exist), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }


}
