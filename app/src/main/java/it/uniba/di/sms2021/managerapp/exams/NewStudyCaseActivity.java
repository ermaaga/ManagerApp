package it.uniba.di.sms2021.managerapp.exams;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

//TODO renderlo un fragment?
public class NewStudyCaseActivity extends AbstractFormActivity {
    private FirebaseDatabase database;
    private DatabaseReference studycasesReference;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_study_case;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = findViewById(R.id.create_study_case);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.editText_name_study_case);
                EditText desc = (EditText) findViewById(R.id.editText_desc_study_case);

                if(validate(name,desc)) {

                    database = FirebaseDbHelper.getDBInstance();
                    studycasesReference = database.getReference(FirebaseDbHelper.TABLE_STUDYCASES);

                    //Ho modificato questa parte per inizializzare il caso di studio con un id.
                    DatabaseReference newElement = studycasesReference.push();
                    StudyCase studycase = new StudyCase(newElement.getKey(),
                            name.getText().toString(), desc.getText().toString());
                    newElement.setValue(studycase);

                    Toast.makeText(getApplicationContext(), R.string.text_message_study_case_created, Toast.LENGTH_SHORT).show();
                    NewStudyCaseActivity.super.onBackPressed();
                }

            }
        });
    }

   private boolean validate(EditText name, EditText desc){
        boolean validate = true;

        if(name.getText().toString().length()==0) {
            validate=false;
            name.setError(getString(R.string.required_field));

        }
        if(desc.getText().toString().length()==0) {
            validate=false;
            desc.setError(getString(R.string.required_field));
        }

        return validate;
    }

}
