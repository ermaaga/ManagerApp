package it.uniba.di.sms2021.managerapp.projects;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;

public class ProjectAboutFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProjectAboutFragment";

    ImageButton addReviewButton;
    ImageButton addReportButton;

    TextView textVote;
    TextView textComment;
    TextView seeReviewButton;
    TextView seeReportButton;
    TextView avarageTextView;
    TextView noneRevievsTextView;
    TextView noneReportsTextView;
    TextView lastReportTextView;

    View reviewDivider;
    View reportDivider;

    RatingBar avarageRatingBar;

    Project project;

    private String idgroup;

    float avarage;
    String lastReview;
    String commentReport;
    float numOfReviews;
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ProjectDetailActivity activity = (ProjectDetailActivity) requireActivity();
        activity.setUpSearchAction(false, null);
        activity.setUpConnectionChangeListener(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textVote = (TextView) view.findViewById(R.id.about_subtitle_value_vote);
        textComment = (TextView) view.findViewById(R.id.about_subtitle_value_comment);

        addReviewButton = (ImageButton) view.findViewById(R.id.add_review_button);
        addReportButton = (ImageButton) view.findViewById(R.id.add_report_button);
        seeReviewButton = (TextView) view.findViewById(R.id.reviews_clickable_text_view);
        seeReportButton = (TextView) view.findViewById(R.id.reports_clickable_text_view);
        avarageTextView = (TextView) view.findViewById(R.id.reviews_avarage_text_view);
        noneRevievsTextView = (TextView) view.findViewById(R.id.none_reviews_text_view);
        noneReportsTextView = (TextView) view.findViewById(R.id.none_reports_text_view);
        lastReportTextView = (TextView) view.findViewById(R.id.last_report_text_view);

        reviewDivider = (View) view.findViewById(R.id.reviews_divider);
        reportDivider = (View) view.findViewById(R.id.report_divider);

        avarageRatingBar = (RatingBar) view.findViewById(R.id.stars_average);

        addReviewButton.setOnClickListener(this);
        addReportButton.setOnClickListener(this);
        seeReviewButton.setOnClickListener(this);
        seeReportButton.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        project = ((ProjectDetailActivity)getActivity()).getSelectedProject();

        if(!project.getGroup().getMembri().contains(LoginHelper.getCurrentUser().getAccountId())){
            addReviewButton.setVisibility(View.VISIBLE);
            addReportButton.setVisibility(View.VISIBLE);
        }

        if(project.getEvaluation()!=null){
            textVote.setText(""+project.getEvaluation().getVote());
            if(project.getEvaluation().getComment()!=null){
                textComment.setText(project.getEvaluation().getComment());
            }

        }

        setAvarage();
        setLastReport();
    }

    public void setAvarage() {
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REVIEWS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float sum = 0;
                numOfReviews = 0;

                /*Ottengo solo i voti delle recensioni che ri riferiscono al progetto corrente
                  e ne calcolo la media*/
                idgroup = project.getGroup().getId();
                for (DataSnapshot child: snapshot.getChildren()) {
                    if (child.getValue(Review.class).getGroupId().equals(idgroup)) {
                        Review review = child.getValue(Review.class);
                        sum += review.getRating();
                        numOfReviews++;
                    }
                }
                avarage = sum/numOfReviews;

                /*Se non ci sono recensioni imposto il valore della TextView a 0
                  e rendo visibile la TextView che lo denota*/
                if(numOfReviews==0){
                    avarageTextView.setText("0");
                    noneRevievsTextView.setVisibility(View.VISIBLE);
                    seeReviewButton.setVisibility(View.GONE);
                    reviewDivider.setVisibility(View.INVISIBLE);
                }else{
                    avarageTextView.setText(String.format("%.1f",avarage));
                    avarageRatingBar.setRating(avarage);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLastReport() {
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REPORTS).child("latestReports");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount()!=0){
                    //Ottengo la chiave dell'ultima segnalazione del progetto
                    for (DataSnapshot child: snapshot.getChildren()) {
                        if (child.getKey().equals(idgroup)) {
                            lastReview = (String) child.getValue();
                            break;
                        }
                    }

                    if(lastReview!=null){
                        setCommentLastReport();
                    }else{
                        noneReportsTextView.setVisibility(View.VISIBLE);
                        seeReportButton.setVisibility(View.GONE);
                        reportDivider.setVisibility(View.INVISIBLE);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setCommentLastReport(){
        DatabaseReference refList = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REPORTS).child("reportsList");
        refList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Ottengo il commento dell'ultima segnalazione e imposto il testo della relativa TextView
                for (DataSnapshot child: snapshot.getChildren()) {
                    if (child.getKey().equals(lastReview)) {
                        commentReport = child.getValue(Report.class).getComment();
                    }
                }
                lastReportTextView.setText(commentReport);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.add_review_button) {
            addReview();
        }else if(v.getId()== R.id.add_report_button) {
            addReport();
        }else if(v.getId()== R.id.reviews_clickable_text_view) {
            seeMoreReviews();
        }else if(v.getId()== R.id.reports_clickable_text_view) {
            saveMoreReports();
        }
    }

    private void addReview() {
        Intent intent = new Intent(getContext(), ProjectNewReviewActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

    private void addReport() {
        Intent intent = new Intent(getContext(), ProjectNewReportActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

    private void seeMoreReviews() {
        Intent intent = new Intent(getContext(), ProjectReviewsActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

    private void saveMoreReports() {
        Intent intent = new Intent(getContext(), ProjectReportsActivity.class);
        intent.putExtra(Project.KEY, project);
        startActivity(intent);
    }

}