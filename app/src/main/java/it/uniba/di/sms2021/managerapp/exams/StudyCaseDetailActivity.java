package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class StudyCaseDetailActivity extends AbstractBottomNavigationActivity {

    private StudyCase studyCase;
    private Exam exam;

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
        studyCase = (StudyCase) intent.getParcelableExtra(StudyCase.Keys.ID);
        exam = intent.getParcelableExtra(Exam.Keys.EXAM);

        TextView textName = (TextView) findViewById(R.id.textView_name_study_case);
        TextView textDesc = (TextView) findViewById(R.id.textView_desc_study_case);

        textName.setText(studyCase.getNome());
        textDesc.setText(studyCase.getDescrizione());
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        intent.putExtra(Exam.Keys.EXAM, exam);
        startActivity(intent);
    }
}
