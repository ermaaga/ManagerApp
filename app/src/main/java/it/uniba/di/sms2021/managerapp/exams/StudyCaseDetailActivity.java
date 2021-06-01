package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class StudyCaseDetailActivity extends AbstractBottomNavigationActivity {

    private StudyCase studyCase;
    private String idStudyCase;
    private String idExam;

    TextView textName;
    TextView textDesc;

    private DatabaseReference studyCaseReference;
    private ValueEventListener studyCaseListener;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_study_case_detail;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        idStudyCase =  intent.getStringExtra(StudyCase.Keys.ID);
        idExam = intent.getStringExtra(Exam.Keys.EXAM);

        textName = (TextView) findViewById(R.id.textView_name_study_case);
        textDesc = (TextView) findViewById(R.id.textView_desc_study_case);


    }

    @Override
    protected void onStart() {
        super.onStart();

        studyCaseReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES);
        studyCaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getKey().equals(idStudyCase)) {
                        studyCase = child.getValue(StudyCase.class);
                        textName.setText(studyCase.getNome());
                        textDesc.setText(studyCase.getDescrizione());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        studyCaseReference.addValueEventListener(studyCaseListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void createGroup(View v){
        Intent intent = new Intent(this, NewGroupActivity.class);
        intent.putExtra(StudyCase.Keys.ID, studyCase.getId());
        intent.putExtra(Exam.Keys.EXAM, idExam);
        startActivity(intent);
    }
}
