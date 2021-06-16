package it.uniba.di.sms2021.managerapp.exams;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.GroupRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.projects.ProjectDetailActivity;

public class ExamGroupsFragment extends Fragment {

    private RecyclerView groupRecyclerView;
    private GroupRecyclerAdapter adapter;

    private Exam exam;

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

        exam = ((ExamDetailActivity)getActivity()).getSelectedExam();
        adapter = new GroupRecyclerAdapter(new GroupRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(Group group) {
                String uid = LoginHelper.getCurrentUser().getAccountId();

                // Se l'utente partecipa già al progetto, apre la schermata del progetto
                // Se l'utente non partecipa al progetto ed il progetto è visualizzabile da tutti,
                // apre la schermata del progetto in modalità visitatore
                // Se l'utente è il professore dell'esame in cui è presente il progetto, apre la
                // schermata del progetto in modalità professore
                if (group.getMembri().contains(uid) || group.getPermissions().isAccessible() ||
                        exam.getProfessors().contains(uid)) {
                    openProject(group);
                }
                // Se il progetto non è visualizzabile, chiede all'utente se vuole unirsi al progetto
                else if (group.getPermissions().isJoinable() && !group.isGroupFull()) {
                    openJoinGroupDialog(group, true);
                }
                // Altrimenti viene visualizzato un messaggio informativo all'utente
                else {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.text_message_group_not_joinable)
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }

            @Override
            public void onJoin(Group group) {
                if (group.getPermissions().isJoinable() && !group.isGroupFull()) {
                    doJoinGroupAction(group);
                } else {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.text_message_group_not_joinable)
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
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
                                            currentGroup.setStudyCaseName(item.getNome());
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

    private void doJoinGroupAction(Group group) {
        openJoinGroupDialog(group, false);
    }

    /**
     * Apre il dialogo in cui si chiede all'utente se vuole unirsi al gruppo.
     * Se forced è true, l'utente sarà anche informato che non è possibile visualizzare il gruppo
     * senza esserne un partecipante.
     */
    private void openJoinGroupDialog(Group group, boolean forced) {
        ConfirmGroupDialog dialog = new ConfirmGroupDialog(getContext(), group, forced);
        dialog.show(getFragmentManager(),"groupDialog");
    }

    private void openProject(Group group) {
        new Project.Initialiser() {
            @Override
            public void onProjectInitialised(Project project) {
                Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
                intent.putExtra(Project.KEY, project);
                startActivity(intent);
            }
        }.initialiseProject(group);
    }

}