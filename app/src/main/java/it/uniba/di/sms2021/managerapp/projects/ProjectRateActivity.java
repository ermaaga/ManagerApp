package it.uniba.di.sms2021.managerapp.projects;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class ProjectRateActivity extends AbstractFormActivity {

    Button buttonrate;
    private TextInputEditText rateEditText;
    private TextInputEditText commentEditText;

    private  TextInputLayout rateInputLayout;
    private  TextInputLayout commentInputLayout;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_rate;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonrate = (Button) findViewById(R.id.button_rate_project);

        rateEditText = (TextInputEditText) findViewById(R.id.rate_edit_text);
        commentEditText = (TextInputEditText) findViewById(R.id.comment_edit_text);

        rateInputLayout = (TextInputLayout) findViewById(R.id.rate_input_layout);
        commentInputLayout = (TextInputLayout) findViewById(R.id.comment_input_layout);

    }

    public void rateProject(View v){
        if(validate(rateEditText.getText().toString(),commentEditText.getText().toString())){
            Toast.makeText(getApplicationContext(), R.string.text_message_not_yet_implemented, Toast.LENGTH_SHORT).show();
            ProjectRateActivity.super.onBackPressed();


        }
    }

    private boolean validate(String textRate, String textComment){
        boolean valid = true;

        if(textRate.length()==0) {
            valid=false;
            rateInputLayout.setError(getString(R.string.required_field));

        }
        if(textComment.length()==0) {
            valid=false;
            commentInputLayout.setError(getString(R.string.required_field));
        }

        return valid;
    }
}
