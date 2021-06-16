package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.lists.StudyCasesRecyclerAdapter;

public class ExamStudyCasesFragment extends Fragment {

    private RecyclerView studyCasesRecyclerView;
    private StudyCasesRecyclerAdapter adapter;

    private DatabaseReference studyCasesReference;
    private ValueEventListener studyCasesListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_exam_study_cases, container, false);
        studyCasesRecyclerView = itemView.findViewById(R.id.studyCases_recyclerView);

        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new StudyCasesRecyclerAdapter(new StudyCasesRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(StudyCase studyCase) {
                doStudyCaseAction(studyCase);
            }

            @Override
            public void onNewGroup(StudyCase studyCase) {
                createGroup(studyCase);
            }
        });
        studyCasesRecyclerView.setAdapter(adapter);
        studyCasesRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        studyCasesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Exam selectedExam = ((ExamDetailActivity) getActivity()).getSelectedExam();
        studyCasesReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES);
        studyCasesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<StudyCase> studyCases = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    StudyCase currentStudyCase = child.getValue(StudyCase.class);

                    if (currentStudyCase.getEsame().equals(selectedExam.getId())) {
                        studyCases.add(currentStudyCase);
                    }
                }

                adapter.submitList(studyCases);  //Ogni volta che i casi di studio
                // cambiano, la lista visualizzata cambia.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        studyCasesReference.addValueEventListener(studyCasesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        studyCasesReference.removeEventListener(studyCasesListener);
    }

    private void doStudyCaseAction(StudyCase studyCase) {
        Intent intent = new Intent(getContext(), StudyCaseDetailActivity.class);
        intent.putExtra(StudyCase.Keys.ID, studyCase.getId());
        intent.putExtra(Exam.Keys.EXAM, ((ExamDetailActivity)getActivity()).getSelectedExam().getId());
        startActivity(intent);
    }


    public void createGroup(StudyCase studyCase){
        Intent intent = new Intent(getContext(), NewGroupActivity.class);
        intent.putExtra(StudyCase.Keys.ID, studyCase.getId());
        intent.putExtra(Exam.Keys.EXAM, ((ExamDetailActivity)getActivity()).getSelectedExam().getId());
        startActivity(intent);
    }
}