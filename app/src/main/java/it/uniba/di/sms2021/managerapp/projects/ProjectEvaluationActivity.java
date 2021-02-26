package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.Evaluation;
import it.uniba.di.sms2021.managerapp.enitities.NewEvaluation;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class ProjectEvaluationActivity extends AbstractFormActivity {

    private static final String TAG = "ProjectVoteActivity";
    Button buttonEvaluate;
    private TextInputEditText voteEditText;
    private TextInputEditText commentEditText;

    private  TextInputLayout voteInputLayout;

    Project project;
    private DatabaseReference groupsReference;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_vote;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonEvaluate = (Button) findViewById(R.id.button_evaluate_project);

        voteEditText = (TextInputEditText) findViewById(R.id.vote_edit_text);
        commentEditText = (TextInputEditText) findViewById(R.id.comment_edit_text);

        voteInputLayout = (TextInputLayout) findViewById(R.id.vote_input_layout);

        project = getIntent().getParcelableExtra(Project.KEY);

       groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(project.getEvaluation()!=null){
            voteEditText.setText(""+project.getEvaluation().getVote());
            commentEditText.setText(""+project.getEvaluation().getComment());
        }
    }

    public void evaluateProject(View v){
        if(validate(voteEditText.getText().toString())){
            float votefloat = Float.parseFloat(voteEditText.getText().toString());

            Evaluation evaluation = new Evaluation(votefloat, commentEditText.getText().toString());

            String idgroup = project.getGroup().getId();

            HashMap childUpdates = new HashMap();
            childUpdates.put("/evaluation/", evaluation);

            groupsReference.child(idgroup).updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Toast.makeText(getApplicationContext(), R.string.text_message_project_evaluated, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.text_message_evaluation_failed,
                            Toast.LENGTH_LONG).show();
                }
            });

            project.setEvaluation(evaluation);
            sendEvaluation();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(Project.KEY, project);
            setResult(RESULT_OK, resultIntent);
            finish();

        }
    }

    private boolean validate(String textRate){
        boolean valid = true;

        if(textRate.length()==0) {
            valid=false;
            voteInputLayout.setError(getString(R.string.required_field));
        }

        return valid;
    }

    private void sendEvaluation() {
        Group group = project.getGroup();
        String currentUserId = LoginHelper.getCurrentUser().getAccountId();

        for(String user: group.getMembri()){
            DatabaseReference pushReference = FirebaseDbHelper.getNewEvaluationReference(user).push();
            pushReference.setValue(
                    new NewEvaluation(pushReference.getKey(), currentUserId,
                            group.getId()));

        }

    }
}
