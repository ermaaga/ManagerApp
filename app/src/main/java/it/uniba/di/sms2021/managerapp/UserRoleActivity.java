package it.uniba.di.sms2021.managerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import it.uniba.di.sms2021.managerapp.enitities.User;

public class UserRoleActivity extends AppCompatActivity {
    private static final String TAG = "UserRoleActivity";

    public static final String USER_ROLE = "UserRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role);
    }

    public void pickRole(android.view.View view) {
        int id = view.getId();
        Intent intent = new Intent(this, DegreeCoursesActivity.class);
        if (id == R.id.studentChoiceButton) {
            intent.putExtra(USER_ROLE, User.ROLE_STUDENT);
        } else if (id == R.id.professorChoiceButton) {
            intent.putExtra(USER_ROLE, User.ROLE_PROFESSOR);
        } else {
            throw new IllegalStateException("Aggiungere nuovi casi al metodo pickRole, rispetto al" +
                    " numero di bottoni presenti.");
        }

        startActivity(intent);
    }
}