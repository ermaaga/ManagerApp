package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

public class StudyCaseDetailActivity extends AppCompatActivity {

    String id;
    String esame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_case_detail);


        Intent intent = getIntent();
        id = intent.getStringExtra(StudyCase.Keys.ID);
        esame = intent.getStringExtra(StudyCase.Keys.ESAME);
        TextView textName = (TextView) findViewById(R.id.textView_name_study_case);
        TextView textDesc = (TextView) findViewById(R.id.textView_desc_study_case);

        textName.setText(intent.getStringExtra(StudyCase.Keys.NOME));
        textDesc.setText(intent.getStringExtra(StudyCase.Keys.DESCRIZIONE));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void createGroup(View v){
        Intent intent = new Intent(this, NewGroupActivity.class);
        intent.putExtra(StudyCase.Keys.ID, id);
        intent.putExtra(StudyCase.Keys.ESAME, esame);
        startActivity(intent);
    }
}
