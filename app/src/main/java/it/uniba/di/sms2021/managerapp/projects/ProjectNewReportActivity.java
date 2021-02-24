package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class ProjectNewReportActivity extends AbstractFormActivity implements View.OnClickListener{
    private static final String TAG = "ProjectNewReportActivity";

    TextInputEditText reportEditText;
    TextInputLayout reportInputLayout;
    Button buttonSubmitReport;

    String user;
    Project project;

    private String idgroup;

    private FirebaseDatabase database;
    private DatabaseReference reportReference;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_new_report;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportEditText = (TextInputEditText) findViewById(R.id.report_edit_text);
        reportInputLayout = (TextInputLayout) findViewById(R.id.report_input_layout);
        buttonSubmitReport = (Button) findViewById(R.id.button_submit_report);

        database=FirebaseDbHelper.getDBInstance();
        reportReference=database.getReference(FirebaseDbHelper.TABLE_REPORTS);

        buttonSubmitReport.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.button_submit_report) {
            subtimReport();
        }
    }

    private void subtimReport() {
        if(isReportValid(reportEditText.getText().toString())){

            user = LoginHelper.getCurrentUser().getAccountId();
            String reportComment = reportEditText.getText().toString();
            project = getIntent().getParcelableExtra(Project.KEY);
            idgroup = project.getGroup().getId();
            String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

            DatabaseReference newElement=reportReference.push();
            Report report  = new Report(newElement.getKey(), user, date, idgroup, reportComment);
            newElement.setValue(report);

            Toast.makeText(getApplicationContext(), R.string.text_message_project_report_submitted, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ProjectDetailActivity.class);
            intent.putExtra(Project.KEY, project);
            startActivity(intent);
        }
    }

    private boolean isReportValid(String reportEditText){
        boolean valid = true;
        if(reportEditText.length()==0) {
            valid=false;
            reportInputLayout.setError(getString(R.string.required_field));
        }
        return valid;
    }

}