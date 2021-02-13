package it.uniba.di.sms2021.managerapp.projects;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.Vote;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class ProjectVoteActivity extends AbstractFormActivity {

    Button buttonEvaluate;
    private TextInputEditText voteEditText;
    private TextInputEditText commentEditText;

    private  TextInputLayout voteInputLayout;
    private  TextInputLayout commentInputLayout;

    private String idgroup;

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
        commentInputLayout = (TextInputLayout) findViewById(R.id.comment_input_layout);

       idgroup = getIntent().getStringExtra(Group.Keys.GROUP);

       groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);

    }

    public void evaluateProject(View v){
        if(validate(voteEditText.getText().toString(),commentEditText.getText().toString())){
            float votefloat = Float.parseFloat(voteEditText.getText().toString());

            Vote vote = new Vote(votefloat, commentEditText.getText().toString());

            HashMap childUpdates = new HashMap();
            childUpdates.put("/vote/", vote);

            groupsReference.child(idgroup).updateChildren(childUpdates);

            Toast.makeText(getApplicationContext(), "The project has been evaluated", Toast.LENGTH_SHORT).show();
            ProjectVoteActivity.super.onBackPressed();

        }
    }

    private boolean validate(String textRate, String textComment){
        boolean valid = true;

        if(textRate.length()==0) {
            valid=false;
            voteInputLayout.setError(getString(R.string.required_field));

        }
        if(textComment.length()==0) {
            valid=false;
            commentInputLayout.setError(getString(R.string.required_field));
        }

        return valid;
    }
}
