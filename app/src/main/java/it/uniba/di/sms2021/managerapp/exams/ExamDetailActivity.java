package it.uniba.di.sms2021.managerapp.exams;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamDetailActivity extends AbstractTabbedNavigationHubActivity {
    private static final int STUDY_CASES_TAB_POSITION = 0;
    private static final int GROUPS_TAB_POSITION = 1;
    private static final String TAG = "ExamDetailActivity";


    //I frammenti sottostanti useranno metodi presenti in questa classe che opereranno su
    // questo campo.
    private Exam exam;

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
        if(exam.getStudents().contains(LoginHelper.getCurrentUser().getAccountId())){
            abandonExamMenuItem.setVisible(true);
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialogAbandonsExam() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.label_Dialog_title_abandon_exam)
                .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      List<String> partecipants = exam.getStudents();
                        if (partecipants == null) {
                            throw new RuntimeException("Questo non dovrebbe mai accadere");
                        }
                        partecipants.remove(LoginHelper.getCurrentUser().getAccountId());

                        DatabaseReference examReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS).child(exam.getId());
                        DatabaseReference groupsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS);

                        examReference.child(Exam.Keys.STUDENTS).setValue(partecipants)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    ValueEventListener groupsListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot child : snapshot.getChildren()) {
                                                Group group = child.getValue(Group.class);
                                                String currentUserId = LoginHelper.getCurrentUser().getAccountId();
                                                if (exam.getId().equals(group.getExam()) && group.getMembri().contains(currentUserId)) {

                                                    removeParticipationGroups( child.getRef(), group.getMembri() );

                                                }
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    };
                                    groupsReference.addValueEventListener(groupsListener);

                                    onSupportNavigateUp();
                                    Toast.makeText(getApplicationContext(), R.string.text_message_abandoned_exam, Toast.LENGTH_LONG).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), R.string.text_message_abandonment_error, Toast.LENGTH_LONG).show();
                                }
                        });



                    }
                }).setNegativeButton(R.string.text_button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();

    }

    private void removeParticipationGroups(DatabaseReference groupReference, List<String> members){
        if (members == null) {
            throw new RuntimeException("Questo non dovrebbe mai accadere");
        }
        members.remove(LoginHelper.getCurrentUser().getAccountId());

        Log.d(TAG,members.toString());

        if(members.size()!=0){
            //se la lista dei membri non è vuota quindi vuol dire che c'è ancora almeno un membro allora aggiorna la lista dei membri
            groupReference.child(Group.Keys.MEMBERS).setValue(members)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                           Log.d(TAG, getString(R.string.text_message_abandoned_project)+" "+groupReference.getKey());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, getString(R.string.text_message_abandonment_error)+" "+groupReference.getKey());
                }
            });
        }else{
            //se l'ultimo membro rimasto ha abbandonato rimuove completamento l'intero gruppo
            groupReference.removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, getString(R.string.text_message_abandoned_project)+" "+groupReference.getKey());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, getString(R.string.text_message_abandonment_error)+" "+groupReference.getKey());
                        }
                    });
        }
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
}