package it.uniba.di.sms2021.managerapp.projects;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.exams.StudyCaseDetailActivity;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;

public class ProjectAboutFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProjectAboutFragment";

    private ImageView favouriteImageView;

    private ImageButton addReviewButton;
    private ImageButton addReportButton;

    private TextView textVote;
    private TextView textComment;
    private TextView seeReviewButton;
    private TextView seeReportButton;
    private TextView avarageTextView;
    private TextView noneRevievsTextView;
    private TextView noneReportsTextView;
    private TextView lastReportTextView;
    private TextView studycaseTextView;
    private TextView noneEvaluationTextView;
    private TextView voteTextView;
    private TextView commentTextView;

    private View reviewDivider;
    private View reportDivider;

    private RatingBar avarageRatingBar;

    private Project project;

    private String idgroup;

    private float avarage;
    private String lastReview;
    private String commentReport;
    private float numOfReviews;
    
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

        favouriteImageView = (ImageView) view.findViewById(R.id.favourite_action);
        addReviewButton = (ImageButton) view.findViewById(R.id.add_review_button);
        addReportButton = (ImageButton) view.findViewById(R.id.add_report_button);
        seeReviewButton = (TextView) view.findViewById(R.id.reviews_clickable_text_view);
        seeReportButton = (TextView) view.findViewById(R.id.reports_clickable_text_view);
        avarageTextView = (TextView) view.findViewById(R.id.reviews_avarage_text_view);
        noneRevievsTextView = (TextView) view.findViewById(R.id.none_reviews_text_view);
        noneReportsTextView = (TextView) view.findViewById(R.id.none_reports_text_view);
        lastReportTextView = (TextView) view.findViewById(R.id.last_report_text_view);
        studycaseTextView = (TextView) view.findViewById(R.id.about_subtitle_value_study_case);
        noneEvaluationTextView = (TextView) view.findViewById(R.id.none_evaluation);
        voteTextView = (TextView) view.findViewById(R.id.about_subtitle_label_vote);
        commentTextView = (TextView) view.findViewById(R.id.about_subtitle_label_comment);

        reviewDivider = (View) view.findViewById(R.id.reviews_divider);
        reportDivider = (View) view.findViewById(R.id.report_divider);

        avarageRatingBar = (RatingBar) view.findViewById(R.id.stars_average);

        favouriteImageView.setOnClickListener(this);
        addReviewButton.setOnClickListener(this);
        addReportButton.setOnClickListener(this);
        seeReviewButton.setOnClickListener(this);
        seeReportButton.setOnClickListener(this);
        studycaseTextView.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        project = ((ProjectDetailActivity)getActivity()).getSelectedProject();

        if(project.isPreferred()){
            favouriteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_star_24, getContext().getTheme()));
        }else{
            favouriteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_star_border_24, getContext().getTheme()));
        }

        if(!project.getGroup().getMembri().contains(LoginHelper.getCurrentUser().getAccountId())){
            addReviewButton.setVisibility(View.VISIBLE);
            addReportButton.setVisibility(View.VISIBLE);
        }

        if(project.getEvaluation()!=null){
            voteTextView.setVisibility(View.VISIBLE);
            textVote.setVisibility(View.VISIBLE);
            textVote.setText(""+project.getEvaluation().getVote());
            if(!project.getEvaluation().getComment().equals("")){
                commentTextView.setVisibility(View.VISIBLE);
                textComment.setVisibility(View.VISIBLE);
                textComment.setText(project.getEvaluation().getComment());
            }

        }else{
            //todo controllare se servono
            voteTextView.setVisibility(View.GONE);
            textVote.setVisibility(View.GONE);
            commentTextView.setVisibility(View.GONE);
            textComment.setVisibility(View.GONE);

            noneEvaluationTextView.setVisibility(View.VISIBLE);
        }

        studycaseTextView.setText(project.getStudyCaseName());

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
        }else if(v.getId()== R.id.about_subtitle_value_study_case) {
            goToStudyCase();
        }else if(v.getId()== R.id.favourite_action) {
            addOrRemoveFavourite();
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

    private void goToStudyCase() {
        Intent intent = new Intent(getContext(), StudyCaseDetailActivity.class);
        intent.putExtra(StudyCase.Keys.ID, project.getGroup().getStudyCase());
        intent.putExtra(Exam.Keys.EXAM, project.getGroup().getExam());
        startActivity(intent);
    }

    private void addOrRemoveFavourite() {

        List<String> whoPrefers = project.getWhoPrefers();
        DatabaseReference  favouriteReference =  FirebaseDbHelper.getFavouriteProjectsReference(LoginHelper.getCurrentUser().getAccountId());
        Object value;

        if(project.isPreferred()){
            //se il progetto è nei preferiti lo rimuove dai preferiti
            whoPrefers.remove(LoginHelper.getCurrentUser().getAccountId());
            value = null;

        }else{
            //se non è nei preferiti allora lo aggiunge ai preferiti
            whoPrefers.add(LoginHelper.getCurrentUser().getAccountId());
            value = true;
        }


         favouriteReference.child(project.getGroup().getId()).setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                            .child(project.getGroup().getId()).child(Group.Keys.WHO_PREFERS).setValue(whoPrefers)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    project.setWhoPrefers(whoPrefers);
                                   if(project.isPreferred()){
                                       favouriteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_star_24, getContext().getTheme()));
                                       Toast.makeText(getContext(), "Aggiunto ai preferiti", Toast.LENGTH_SHORT).show();
                                    }else{
                                       favouriteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_star_border_24, getContext().getTheme()));
                                       Toast.makeText(getContext(), "Rimosso dai preferiti", Toast.LENGTH_SHORT).show();
                                   }
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

                    }
                });





    }
}