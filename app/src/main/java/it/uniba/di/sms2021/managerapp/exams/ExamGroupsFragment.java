package it.uniba.di.sms2021.managerapp.exams;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.dialogs.ConfirmGroupDialog;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.lists.GroupRecyclerAdapter;

public class ExamGroupsFragment extends Fragment {

    private RecyclerView groupRecyclerView;
    private GroupRecyclerAdapter adapter;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_exam_groups,container,false);
        groupRecyclerView = itemView.findViewById(R.id.studyCases_recyclerView);
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new GroupRecyclerAdapter(new GroupRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Group group) {
                doGroupAction(group);
            }

            @Override
            public void onInfo(Group group) {showGruoupsInfo(group);

            }
        });
        groupRecyclerView.setAdapter(adapter);
        groupRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL));
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Exam selectedExam = ((ExamDetailActivity) getActivity()).getSelectedExam();

        List<StudyCase> studyCases = getStudyCasesGroup(selectedExam);
        setGroupsOnView(selectedExam, studyCases );


    }

    private void setGroupsOnView(Exam selectedExam,List<StudyCase> studyCases) {
        try {
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<Group> groups = new ArrayList<>();
                            for (DataSnapshot child: snapshot.getChildren()) {
                                Group currentGroup = child.getValue(Group.class);
                                if (currentGroup.getExam().equals(selectedExam.getId())) {
                                    for(StudyCase item : studyCases){
                                        if(item.getId().equals(currentGroup.getStudyCase())){
                                            currentGroup.setStudyCase(item.getNome());
                                        }
                                    }
                                    groups.add(currentGroup);
                                }
                            }
                            adapter.submitList(groups);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("error","errore in setGroupsOnView" );
        }
    }

    private List<StudyCase> getStudyCasesGroup(Exam selectedExam) {
        List<StudyCase> lstStudyCases = new ArrayList<>();
        try {
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot child: snapshot.getChildren()) {
                                StudyCase currentStudyCase = child.getValue(StudyCase.class);

                                if (currentStudyCase.getEsame().equals(selectedExam.getId())) {
                                    lstStudyCases.add(currentStudyCase);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("error","errore in getStudyCasesGroup" );
        }
        return  lstStudyCases;

    }

    private void showGruoupsInfo(Group group) {
        CharSequence text = "GroupsInfo";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getContext(), text, duration);
        toast.show();
    }

    private void doGroupAction(Group group) {
        openDialog(group);
    }

    private void openDialog(Group group) {
        ConfirmGroupDialog dialog = new ConfirmGroupDialog(group);
        dialog.show(getFragmentManager(),"groupDialog");
    }

}