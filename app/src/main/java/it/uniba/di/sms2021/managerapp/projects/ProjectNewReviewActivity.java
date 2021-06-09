package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class ProjectNewReviewActivity extends AbstractFormActivity implements View.OnClickListener{
    private static final String TAG = "ProjectNewReviewActivity";

    RatingBar reviewRatingBar;
    TextInputEditText reviewEditText;
    TextInputLayout reviewInputLayout;
    Button buttonSubmitReview;
    ImageView ratingRequiredImageView;
    TextView ratingRequiredTextView;
    TextView submitReportTextView;

    String user;
    Project project;

    private String idgroup;

    private FirebaseDatabase database;
    private DatabaseReference reviewReference;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_new_review;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reviewRatingBar = (RatingBar) findViewById(R.id.ratingBar_new_review);
        reviewEditText = (TextInputEditText) findViewById(R.id.review_edit_text);
        reviewInputLayout = (TextInputLayout) findViewById(R.id.review_input_layout);
        buttonSubmitReview = (Button) findViewById(R.id.button_submit_review);
        ratingRequiredImageView = (ImageView) findViewById(R.id.rating_required_imageView);
        ratingRequiredTextView = (TextView) findViewById(R.id.rating_required_textView);
        submitReportTextView = (TextView) findViewById(R.id.text_submit_report_from_review);

        database=FirebaseDbHelper.getDBInstance();
        reviewReference=database.getReference(FirebaseDbHelper.TABLE_REVIEWS);

        buttonSubmitReview.setOnClickListener(this);
        submitReportTextView.setOnClickListener(this);

        project = getIntent().getParcelableExtra(Project.KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.button_submit_review) {
            subtimReview();
        }else if(v.getId()== R.id.text_submit_report_from_review) {
            subtmitReport();
        }
    }

    private void subtimReview() {
        if(isReviewValid((int) reviewRatingBar.getRating())){

            user = LoginHelper.getCurrentUser().getAccountId();
            int rating = (int) reviewRatingBar.getRating();
            String reviewComment = reviewEditText.getText().toString();
            String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            idgroup = project.getGroup().getId();

            DatabaseReference newElement=reviewReference.push();
            Review review = new Review(newElement.getKey(), user, date, rating, idgroup, reviewComment);
            newElement.setValue(review);

            Toast.makeText(getApplicationContext(), R.string.text_message_project_review_submitted, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isReviewValid(int rating){
        boolean valid = true;
        if(rating==0) {
            valid=false;
            ratingRequiredImageView.setVisibility(View.VISIBLE);
            ratingRequiredTextView.setText(R.string.text_message_star_rating_required);
        }
        if(reviewEditText.length()>800){
            valid=false;
            reviewInputLayout.setError(getString(R.string.text_error_max_review_report));
        }
        return valid;
    }

    private void subtmitReport(){
        Intent intent = new Intent(this, ProjectNewReportActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }
}

