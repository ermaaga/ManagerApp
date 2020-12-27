package it.uniba.di.sms2021.managerapp.exams;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

//TODO renderlo un fragment
public class FormTemplateActivity extends AppCompatActivity{
    private FirebaseDatabase database;
    private DatabaseReference studycasesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_template);

        Button button = findViewById(R.id.create_study_case);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = findViewById(R.id.editText_name_study_case);
                EditText desc = findViewById(R.id.editText_desc_study_case);

                database = FirebaseDbHelper.getDBInstance();
                studycasesReference = database.getReference(FirebaseDbHelper.TABLE_STUDYCASES);

                //Ho modificato questa parte per inizializzare il caso di studio con un id.
                DatabaseReference newElement = studycasesReference.push();
                StudyCase studycase = new StudyCase(newElement.getKey(),
                        name.getText().toString(),desc.getText().toString());
                newElement.setValue(studycase);

                /*aggiungere controlli:
                di validazione campi
                se è stato salvato correttamente*/

                Context context = getApplicationContext();
                CharSequence text = "Il caso di studio è stato creato";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

}
