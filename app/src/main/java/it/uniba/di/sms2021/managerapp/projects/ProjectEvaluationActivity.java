package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.Evaluation;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewEvaluation;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class ProjectEvaluationActivity extends AbstractFormActivity {

    private static final String TAG = "ProjectVoteActivity";
    private TextInputEditText voteEditText;
    private TextInputEditText commentEditText;

    private  TextInputLayout voteInputLayout;
    private  TextInputLayout commentInputLayout;

    private Project project;
    private DatabaseReference groupsReference;
    private  DatabaseReference evaluatedProjectsReference;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_evaluation;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voteEditText = (TextInputEditText) findViewById(R.id.vote_edit_text);
        commentEditText = (TextInputEditText) findViewById(R.id.comment_edit_text);

        voteInputLayout = (TextInputLayout) findViewById(R.id.vote_input_layout);
        commentInputLayout = (TextInputLayout) findViewById(R.id.comment_input_layout);

        project = getIntent().getParcelableExtra(Project.KEY);

       groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);
       evaluatedProjectsReference =  FirebaseDbHelper.getEvaluatedProjectsReference(LoginHelper.getCurrentUser().getAccountId());

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

        if(validate(voteEditText.getText().toString(), commentEditText.getText().toString())){
            float votefloat = Float.parseFloat(voteEditText.getText().toString());

            Evaluation evaluation = new Evaluation(votefloat, commentEditText.getText().toString());

            String idgroup = project.getGroup().getId();

            HashMap childUpdates = new HashMap();
            childUpdates.put("/evaluation/", evaluation);

            groupsReference.child(idgroup).updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    evaluatedProjectsReference.child(project.getGroup().getId()).setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), R.string.text_message_project_evaluated, Toast.LENGTH_SHORT).show();
                                    sendEvaluation();

                                    project.setEvaluation(evaluation);

                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra(Project.KEY, project);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.text_message_evaluation_failed,
                            Toast.LENGTH_LONG).show();
                }
            });

        }


    }

    private boolean validate(String textRate, String textComment){
        boolean valid = true;

        if(textRate.length()==0) {
            valid=false;
            voteInputLayout.setError(getString(R.string.required_field));
        }

        if(textComment.length()>125) {
            valid=false;
           commentInputLayout.setError(getString(R.string.error_characters_evaluation_comment));
        }

        return valid;
    }

    private void sendEvaluation() {
        Group group = project.getGroup();
        String currentUserId = LoginHelper.getCurrentUser().getAccountId();

      Boolean isUpdate=project.getEvaluation()!=null ? true : false;

        for(String user: group.getMembri()){
            DatabaseReference pushReference = FirebaseDbHelper.getNewEvaluationReference(user).push();
            pushReference.setValue(
                    new NewEvaluation(pushReference.getKey(), currentUserId,
                            group.getId(), isUpdate));

        }

    }
}
