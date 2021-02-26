package it.uniba.di.sms2021.managerapp.projects;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.uniba.di.sms2021.managerapp.R;
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

    Project project;
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ProjectDetailActivity activity = (ProjectDetailActivity) getActivity();
        activity.setUpSearchAction(false, null);
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
            if(project.getEvaluation().getComment()!=null)
                textComment.setText(project.getEvaluation().getComment());
        }
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
    }

    private void saveMoreReports() {
    }

}