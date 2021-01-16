package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class NewGroupActivity extends AbstractFormActivity {
    private FirebaseDatabase database;
    private DatabaseReference groupsRef;

    @Override
    protected int getLayoutId() { return R.layout.activity_new_group; }
    @Override
    protected int getBottomNavigationMenuItemId() { return R.id.nav_exams; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = findViewById(R.id.button_create_new_group);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.name_edit_text);
                if (!TextUtils.isEmpty(name.getText().toString())) {

                    Intent intent = getIntent();

                    database = FirebaseDbHelper.getDBInstance();
                    groupsRef = database.getReference(FirebaseDbHelper.TABLE_GROUPS);

                    DatabaseReference newElement = groupsRef.push();

                    //TODO: aggiungere utente loggato nei membri
                    Group group = new Group(newElement.getKey(), name.getText().toString(), intent.getStringExtra(StudyCase.Keys.ID),
                            intent.getStringExtra(StudyCase.Keys.ESAME));
                    newElement.setValue(group);

                    Toast.makeText(getApplicationContext(), R.string.text_message_group_created , Toast.LENGTH_SHORT).show();
                    NewGroupActivity.super.onBackPressed();
                }

            }
        });

    }
}