package it.uniba.di.sms2021.managerapp.exams;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.projects.ProjectDetailActivity;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamDetailActivity extends AbstractTabbedNavigationHubActivity {
    private static final int STUDY_CASES_TAB_POSITION = 0;
    private static final int GROUPS_TAB_POSITION = 1;
    private static final String TAG = "ExamDetailActivity";


    //I frammenti sottostanti useranno metodi presenti in questa classe che opereranno su
    // questo campo.
    private Exam exam;
    private final Context context = ExamDetailActivity.this;

    @Override
    protected Fragment getInitialFragment() {
        return new ExamStudyCasesFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        exam = getIntent().getParcelableExtra(Exam.Keys.EXAM);

        Log.d("ExamDetailActivity", exam.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setTitle(exam.getName());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_exam_detail;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPosition = tab.getPosition();
        if (tabPosition == STUDY_CASES_TAB_POSITION) {
            navigateTo(new ExamStudyCasesFragment(), false);
        } else if (tabPosition == GROUPS_TAB_POSITION) {
            navigateTo(new ExamGroupsFragment(), false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exams, menu);

        MenuItem  abandonExamMenuItem = menu.findItem(R.id.action_abandons_exam);
        if(exam.getStudents() != null &&
                exam.getStudents().contains(LoginHelper.getCurrentUser().getAccountId())){
            abandonExamMenuItem.setVisible(true);
        }

        if (exam.getProfessors().contains(LoginHelper.getCurrentUser().getAccountId())) {
            menu.findItem(R.id.action_delete_exam).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);
        if (menuId == R.id.action_exam_partecipants) {
            Intent intent = new Intent(this, ExamsPartecipantsActivity.class);
            intent.putExtra(Exam.Keys.EXAM, exam);
            startActivity(intent);
        }else if(menuId == R.id.action_abandons_exam){
            showDialogAbandonsExam();
        } else if (menuId == R.id.action_delete_exam) {
            showDialogDeleteExam();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialogAbandonsExam() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.label_Dialog_title_abandon_exam)
                .setMessage(R.string.label_Dialog_message_abandon_exam)
                .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       abandonsExamAndGroups(context, exam, new OnExamAbandonedListener() {
                           @Override
                           public void onExamAbandoned(Exam exam) {
                               if (exam != null) {
                                   ExamDetailActivity.this.onExamAbandoned();
                               }
                           }
                       });

                    }
                }).setNegativeButton(R.string.text_button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();

    }

    public void abandonsExamAndGroups(Context context,Exam exam, OnExamAbandonedListener listener){
        abandonsExamGroups(context,exam,listener);
    }
    private void abandonsExamGroups(Context context,Exam exam, OnExamAbandonedListener listener){
        DatabaseReference groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);

        ValueEventListener groupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> groupsToAbandon = new HashSet<>();
                List<Group> groups = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Group group = child.getValue(Group.class);
                    String currentUserId = LoginHelper.getCurrentUser().getAccountId();
                    if (exam.getId().equals(group.getExam()) && group.getMembri().contains(currentUserId)) {
                        groups.add(group);
                        groupsToAbandon.add(group.getId());
                    }
                }

                if (groups.isEmpty()) {
                    abandonsExam(context, exam, listener);
                    return;
                }

                for (Group group: groups) {
                    ProjectDetailActivity.abandonsProject(context, group, new ProjectDetailActivity.OnProjectAbandonedListener(){
                        @Override
                        public void onProjectAbandoned(Group project) {
                            if (project == null) {
                                Toast.makeText(context,
                                        R.string.text_message_exam_abandonment_error,
                                        Toast.LENGTH_LONG).show();
                                listener.onExamAbandoned(null);
                                return;
                            }

                            Log.i(TAG, "Abandoned group " + group.getName());
                            groupsToAbandon.remove(project.getId());
                            if (groupsToAbandon.isEmpty()) {
                                Log.i(TAG, "Finished abandonment groups.");
                                abandonsExam(context, exam, listener);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onExamAbandoned(null);
            }
        };
        groupsReference.addListenerForSingleValueEvent(groupsListener);
    }


    private void abandonsExam(Context context, Exam exam, OnExamAbandonedListener listener){
        List<String> partecipants = exam.getStudents();
        if (partecipants == null) {
            throw new RuntimeException("Questo non dovrebbe mai accadere");
        }
        partecipants.remove(LoginHelper.getCurrentUser().getAccountId());

        DatabaseReference examReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS).child(exam.getId());

        examReference.child(Exam.Keys.STUDENTS).setValue(partecipants)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       listener.onExamAbandoned(exam);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,
                        R.string.text_message_exam_abandonment_error,
                        Toast.LENGTH_LONG).show();
                listener.onExamAbandoned(null);
            }
        });

    }

    private void onExamAbandoned() {
        Toast.makeText(getApplicationContext(), R.string.text_message_abandoned_exam, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showDialogDeleteExam() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.label_Dialog_title_delete_exam)
                .setMessage(R.string.text_message_delete_exam)
                .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteStudyCases();
                    }
                }).setNegativeButton(R.string.text_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void deleteStudyCases() {
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Set<String> studyCaseToDelete = new HashSet<>();
                        List<StudyCase> studyCases = new ArrayList<>();
                        for (DataSnapshot child: snapshot.getChildren()) {
                            StudyCase studyCase = child.getValue(StudyCase.class);
                            if (studyCase.getEsame().equals(exam.getId())) {
                                studyCases.add(studyCase);
                                studyCaseToDelete.add(studyCase.getId());

                                StudyCaseDetailActivity.deleteStudyCaseAndGroups(context,
                                        studyCase, new StudyCaseDetailActivity.OnStudyCaseDeletedListener() {
                                            @Override
                                            public void onStudyCaseDeleted(StudyCase studyCase) {
                                                if (studyCase == null) {
                                                    Toast.makeText(context,
                                                            R.string.text_message_exam_deletion_failed,
                                                            Toast.LENGTH_LONG).show();
                                                    return;
                                                }

                                                studyCaseToDelete.remove(studyCase.getId());
                                                if (studyCaseToDelete.isEmpty()) {
                                                    Log.i(TAG, "Deleted Studycases");
                                                    deleteExam();
                                                }
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context,
                                R.string.text_message_exam_deletion_failed,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteExam() {
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS)
                .child(exam.getId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                onExamDeleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,
                        R.string.text_message_exam_deletion_failed,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onExamDeleted() {
        Toast.makeText(context, R.string.text_message_exam_deleted_successfully,
                Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            fragmentManager.popBackStack();
        }

        return super.onSupportNavigateUp();
    }

    /*
     * Chiamato dal bottone di aggiunta casi di studio (nel fragment dei casi di studio)
   */
    public void addStudyCase(View view) {
        Intent intent = new Intent(this, NewStudyCaseActivity.class);
        intent.putExtra(Exam.Keys.ID,exam.getId());
        startActivity(intent);
    }

    public Exam getSelectedExam () {
        return exam;
    }

  public interface OnExamAbandonedListener {
        /**
         * Specifica l'azione da fare quando è stato abbandonato un esame o l'azione
         * è stata annullata
         * @param exam l'esame è stato abbandonato o null se l'azione è stata annullata
         */
        void onExamAbandoned (Exam exam);
    }
}