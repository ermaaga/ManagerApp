package it.uniba.di.sms2021.managerapp.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.utility.AbstractBaseActivity;

public class UserRoleActivity extends AbstractBaseActivity {
    private static final String TAG = "UserRoleActivity";

    public static final String USER_ROLE = "UserRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role);
    }

    public void pickRole(android.view.View view) {
        int id = view.getId();
        Intent intent = new Intent(this, DepartmentActivity.class);
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