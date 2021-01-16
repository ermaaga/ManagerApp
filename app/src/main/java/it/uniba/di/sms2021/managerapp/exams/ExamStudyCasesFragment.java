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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.lists.StudyCasesRecyclerAdapter;

public class ExamStudyCasesFragment extends Fragment {

    private RecyclerView studyCasesRecyclerView;
    private StudyCasesRecyclerAdapter adapter;

    private FloatingActionButton addStudyCaseFloatingActionButton;

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
            public void onInfo(StudyCase studyCase) {
                showStudyCaseInfo(studyCase);
            }
        });
        studyCasesRecyclerView.setAdapter(adapter);
        studyCasesRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        studyCasesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        //TODO far si che i casi di studio siano legati ad un esame e leggere solo i casi
        // di studio di un esame
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<StudyCase> studyCases = new ArrayList<>();

                        for (DataSnapshot child: snapshot.getChildren()) {
                            studyCases.add(child.getValue(StudyCase.class));
                        }

                        adapter.submitList(studyCases);  //Ogni volta che i casi di studio
                        // cambiano, la lista visualizzata cambia.
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void doStudyCaseAction(StudyCase studyCase) {
        Intent intent = new Intent(getContext(), StudyCaseDetailActivity.class);
        intent.putExtra(StudyCase.Keys.ID, studyCase.getId());
        intent.putExtra(StudyCase.Keys.NOME, studyCase.getNome());
        intent.putExtra(StudyCase.Keys.DESCRIZIONE, studyCase.getDescrizione());
        intent.putExtra(StudyCase.Keys.ESAME, studyCase.getEsame());
        startActivity(intent);
    }

    //TODO implementare visualizzazione del pdf di info
    private void showStudyCaseInfo(StudyCase studyCase) {
        Toast.makeText(requireContext(), R.string.text_message_not_yet_implemented, Toast.LENGTH_SHORT).show();
    }
}