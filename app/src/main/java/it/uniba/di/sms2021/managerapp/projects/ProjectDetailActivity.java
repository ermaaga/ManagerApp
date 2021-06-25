package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.ListProjects;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinNotice;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewEvaluation;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReplyReportNotice;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReportNotice;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.ConnectionCheckBroadcastReceiver;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;

public class ProjectDetailActivity extends AbstractTabbedNavigationHubActivity {
    public static final int ABOUT_TAB_POSITION = 0;
    public static final int FILES_TAB_POSITION = 1;
    public static final int MEMBERS_TAB_POSITION = 2;
    public static final String INITIAL_TAB_POSITION_KEY = "initial_position";

    private static final int REQUEST_EVALUATION = 1;

    private static final String TAG = "ProjectDetailActivity";
    private static final int ONE_MEMBER = 1;

    private Project project;

    private MenuItem searchMenuItem;
    private boolean searchActivated;
    // Listener custom implementato nei fragment al momento della loro creazione
    private SearchUtil.OnSearchListener onSearchListener;

    // Elementi della seach view presente nell'action bar
    private SearchView searchView;
    private HorizontalScrollView searchFilters;

    //Listener implementato nei fragment al momento della loro creazione
    private ConnectionCheckBroadcastReceiver.OnConnectionChangeListener connectionChangeListener;

    private final Context context = ProjectDetailActivity.this;

    @Override
    protected Fragment getInitialFragment() {
        return new ProjectAboutFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        project = getIntent().getParcelableExtra(Project.KEY);
        int initialTab = getIntent().getIntExtra(INITIAL_TAB_POSITION_KEY, -1);

        if (initialTab != -1) {
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            TabLayout.Tab tab = tabLayout.getTabAt(initialTab);
            tabLayout.selectTab(tab);
            navigateToTabByPosition(initialTab);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(project.getName() + " - " + project.getStudyCaseName());
        actionBar.setSubtitle(project.getExamName());

        Log.i(TAG, project.toString());
        Log.i(TAG, "valutazione: "+project.getEvaluation());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_detail;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPosition = tab.getPosition();
        navigateToTabByPosition(tabPosition);
    }

    private void navigateToTabByPosition (int tabPosition) {
        if (tabPosition == ABOUT_TAB_POSITION) {
            navigateTo(new ProjectAboutFragment(), false);
        } else if (tabPosition == FILES_TAB_POSITION) {
            navigateTo(new ProjectFilesFragment(), false);
        } else if (tabPosition == MEMBERS_TAB_POSITION) {
            navigateTo(new ProjectMembersFragment(), false);
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
        getMenuInflater().inflate(R.menu.menu_project_detail, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setVisible(searchActivated);

        //Inizializzazione della barra di ricerca. La barra è solo attivata nei fragment che la richiedono.
        if (searchActivated) {
            searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchFilters = findViewById(R.id.searchFilterScrollView);
            setUpSearchBar();
        }

        MenuItem  evaluateMenuItem = menu.findItem(R.id.action_evaluate_project);
        if(project.isProfessor() && project.getEvaluation()==null){
            evaluateMenuItem.setVisible(true);
        }else{
            if(project.isProfessor() && project.getEvaluation()!=null){
                evaluateMenuItem.setTitle(R.string.text_label_update_evaluate);
                evaluateMenuItem.setVisible(true);
            }

        }

        MenuItem  abandonProjectMenuItem = menu.findItem(R.id.action_abandons_project);
        if(project.isMember()){
            abandonProjectMenuItem.setVisible(true);
        }

        if (project.getMembri().get(0).equals(LoginHelper.getCurrentUser().getAccountId())
            || project.isProfessor()) {
            menu.findItem(R.id.action_delete_project).setVisible(true);
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);
        if (menuId == R.id.action_project_permissions) {
            Intent intent = new Intent(this, ProjectPermissionsActivity.class);
            intent.putExtra(Project.KEY, project);
            startActivity(intent);
        }else if (menuId == R.id.action_evaluate_project) {
            Intent intent = new Intent(this, ProjectEvaluationActivity.class);
            intent.putExtra(Project.KEY, project);
            startActivityForResult(intent, REQUEST_EVALUATION);
        }else if(menuId == R.id.action_abandons_project){
            showDialogAbandonsProject();
        }else if(menuId == R.id.action_delete_project) {
            showDeleteProjectDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogAbandonsProject() {
        Log.d(TAG, "ABANDONS PROJECT"+project.toString());
        new AlertDialog.Builder(this)
            .setTitle(R.string.label_Dialog_title_abandon_project)
            .setMessage(R.string.label_Dialog_message_abandon_project)
            .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    abandonsProject(context, project.getGroup(), new OnProjectAbandonedListener(){
                        @Override
                        public void onProjectAbandoned(Group project) {
                            if (project != null) {
                                ProjectDetailActivity.this.onProjectAbandoned(context);
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

   public static void abandonsProject(Context context, Group project, OnProjectAbandonedListener listener){
        List<String> members = project.getMembri();
        if (members == null) {
            throw new RuntimeException("Questo non dovrebbe mai accadere");
        }

        DatabaseReference groupReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS).child(project.getId());

        if(members.size() > ONE_MEMBER){
            //se nella lista c'è ancora almeno un membro allora aggiorna la lista dei membri
            Log.d(TAG, "IL GRUPPO HA ANCORA DEI MEMBRI");
            members.remove(LoginHelper.getCurrentUser().getAccountId());

            groupReference.child(Group.Keys.MEMBERS).setValue(members)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onProjectAbandoned(project);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onProjectAbandoned(null);
                }
            });
        }else{
            Log.d(TAG, "IL GRUPPO HA SOLO UN MEMBRO "+ project.toString());
            //se il membro che abbandona è l'ultimo membro rimasto rimuove completamente l'intero gruppo
            deleteProject(context, project, new OnProjectDeletedListener() {
                @Override
                public void onProjectDeleted(Group project) {
                    if (project == null) {
                        Toast.makeText(context, R.string.text_message_project_abandonment_error, Toast.LENGTH_LONG).show();
                        listener.onProjectAbandoned(null);
                        return;
                    }
                    listener.onProjectAbandoned(project);
                }
            });
        }
}
    private void showDeleteProjectDialog() {
        Log.d(TAG, "DELETE PROJECT dialog"+project.toString());
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_dialog_title_delete_project)
                .setMessage(R.string.text_message_delete_project)
                .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProject(context, project.getGroup(), new OnProjectDeletedListener() {
                            @Override
                            public void onProjectDeleted(Group project) {
                                if (project != null) {
                                    ProjectDetailActivity.this.onProjectDeleted(context);
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

    public static void deleteProject(Context context, Group project, OnProjectDeletedListener listener) {
        Log.d(TAG, "DELETE PROJECT method"+project.toString());
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                .child(project.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, "Deleted Project");
                deleteProjectFromLists(context, project, listener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.text_message_project_deletion_failed, Toast.LENGTH_LONG).show();
                listener.onProjectDeleted(null);
            }
        });
    }

    private static void deleteProjectFromLists(Context context, Group project, OnProjectDeletedListener listener) {
        Log.d(TAG, "DELETE LIST " +project.toString());
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_LISTS_PROJECTS)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        for (MutableData userSnapshot: currentData.getChildren()) {
                            MutableData favouriteList =
                                    userSnapshot.child(FirebaseDbHelper.TABLE_FAVOURITE_PROJECTS);
                            if (favouriteList.hasChild(project.getId())) {
                                favouriteList.child(project.getId()).setValue(null);
                            }
                            MutableData triedList =
                                    userSnapshot.child(FirebaseDbHelper.TABLE_TRIED_PROJECTS);
                            if (triedList.hasChild(project.getId())) {
                                triedList.child(project.getId()).setValue(null);
                            }
                            MutableData evaluatedList =
                                    userSnapshot.child(FirebaseDbHelper.TABLE_EVALUATED_PROJECTS);
                            if (evaluatedList.hasChild(project.getId())) {
                                evaluatedList.child(project.getId()).setValue(null);
                            }

                            for (MutableData receivedListsSnapshot:
                                    userSnapshot.child(FirebaseDbHelper.TABLE_RECEIVED_PROJECT_LISTS)
                                            .getChildren()) {
                                ListProjects listProjects = receivedListsSnapshot.getValue(ListProjects.class);
                                if (listProjects.getIdProjects().contains(project.getId())) {
                                    listProjects.getIdProjects().remove(project.getId());
                                    FirebaseDbHelper.getReceivedProjectListsReference(userSnapshot.getKey())
                                            .child(receivedListsSnapshot.getKey()).setValue(listProjects);
                                }
                            }
                        }

                        Log.i(TAG, "Deleted Project From List");
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        Log.d(TAG, "postTransaction:onComplete:" + error);
                        deleteNotifications(context, project, listener);
                    }
                });
    }

    private static void deleteNotifications(Context context, Group project, OnProjectDeletedListener listener) {
        Log.d(TAG, "DELETE NOTICATIONS " +project.toString());
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_NOTIFICATIONS)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        for (String userId: project.getMembri()) {
                            for (MutableData groupJoinNoticeData: currentData.child(userId)
                                    .child(FirebaseDbHelper.TABLE_GROUP_JOIN_NOTICE)
                                    .getChildren()) {
                                GroupJoinNotice notice = groupJoinNoticeData.getValue(GroupJoinNotice.class);
                                if (notice.getGroup().equals(project.getId())) {
                                    groupJoinNoticeData.setValue(null);
                                }
                            }

                            for (MutableData newEvaulationData: currentData.child(userId)
                                    .child(FirebaseDbHelper.TABLE_NEW_EVALUATION)
                                    .getChildren()) {
                                NewEvaluation newEvaluation = newEvaulationData.getValue(NewEvaluation.class);
                                if (newEvaluation.getGroupId().equals(project.getId())) {
                                    newEvaulationData.setValue(null);
                                }
                            }

                            for (MutableData newReplyReportData: currentData.child(userId)
                                    .child(FirebaseDbHelper.TABLE_NEW_REPLY_REPORT)
                                    .getChildren()) {
                                NewReplyReportNotice notice =
                                        newReplyReportData.getValue(NewReplyReportNotice.class);
                                if (notice.getGroupId().equals(project.getId())) {
                                    newReplyReportData.setValue(null);
                                }
                            }

                            for (MutableData newReportData: currentData.child(userId)
                                    .child(FirebaseDbHelper.TABLE_NEW_REPORT)
                                    .getChildren()) {
                                NewReportNotice notice =
                                        newReportData.getValue(NewReportNotice.class);
                                if (notice.getGroupId().equals(project.getId())) {
                                    newReportData.setValue(null);
                                }
                            }
                        }

                        for (MutableData groupJoinRequestData :currentData
                                .child(project.getMembri().get(0))
                                .child(FirebaseDbHelper.TABLE_GROUP_JOIN_REQUESTS)
                                .getChildren()) {
                            GroupJoinRequest request = groupJoinRequestData.getValue(GroupJoinRequest.class);
                            if (request.getGroupId().equals(project.getId())) {
                                groupJoinRequestData.setValue(null);
                            }
                        }

                        Log.i(TAG, "Deleted Project Notifications");
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        deleteProjectFiles(context, project, listener);
                    }
                });
    }

    private static void deleteProjectFiles(Context context, Group project, OnProjectDeletedListener listener) {
        FirebaseStorage.getInstance().getReference()
                .child(FirebaseDbHelper.GROUPS_FOLDER).child(project.getId()).listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> filesToDelete = new ArrayList<>();

                        if (listResult.getItems().size() == 0) {
                            Log.i(TAG, "Deleted Project Files");
                            listener.onProjectDeleted(project);
                        }

                        for (StorageReference file: listResult.getItems()) {
                            filesToDelete.add(file);
                            file.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    filesToDelete.remove(file);
                                    if (filesToDelete.isEmpty()) {
                                        Log.i(TAG, "Deleted Project Files");
                                        listener.onProjectDeleted(project);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void onProjectAbandoned(Context context) {
        Toast.makeText(context, R.string.text_message_abandoned_project, Toast.LENGTH_LONG).show();
        finish();
    }

    private void onProjectDeleted(Context context) {
        Toast.makeText(context, R.string.text_message_project_successfully_deleted, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_EVALUATION && resultCode == RESULT_OK){
            project = data.getParcelableExtra(Project.KEY);
            invalidateOptionsMenu();
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

    public Project getSelectedProject () {
        return project;
    }

    /**
     * Imposta la visibilità e l'azione dell'azione di ricerca nello specifico fragment.
     * Deve essere chiamato in onAttach in ogni fragment dell'activity.
     * @param activated se l'azione di ricerca è attivata nel fragment corrente
     * @param listener cosa fà l'azione di ricerca nel fragment corrente
     */
    public void setUpSearchAction (boolean activated, @Nullable SearchUtil.OnSearchListener listener) {
        this.searchActivated = activated;
        this.onSearchListener = listener;
        invalidateOptionsMenu();
    }

    @Override
    protected void onConnectionUp() {
        super.onConnectionUp();
        if (connectionChangeListener != null) {
            connectionChangeListener.onConnectionUp();
        }
    }

    @Override
    protected void onConnectionDown() {
        super.onConnectionDown();
        if (connectionChangeListener != null) {
            connectionChangeListener.onConnectionDown();
        }
    }

    /**
     * Imposta le azioni da prendere nei singoli fragment qualora cambiasse lo stato della connessione.
     * Deve essere chiamato in onAttach in ogni fragment di questa activity.
     */
    public void setUpConnectionChangeListener (@Nullable ConnectionCheckBroadcastReceiver.OnConnectionChangeListener listener) {
        connectionChangeListener = listener;
    }

    private void setUpSearchBar() {
        //Inizializza la barra di ricerca ed i filtri
        SearchUtil.setUpSearchBar(this, searchView, searchFilters,
                R.string.text_hint_search_file, onSearchListener);
    }

    public interface OnProjectDeletedListener {
        /**
         * Specifica l'azione da fare quando è stato eliminato un progetto o l'azione
         * è stata annullata
         * @param project il progetto è stato eliminato o null se l'azione è stata annullata
         */
        void onProjectDeleted (Group project);
    }

    public interface OnProjectAbandonedListener {
        /**
         * Specifica l'azione da fare quando è stato abbandonato un progetto o l'azione
         * è stata annullata
         * @param project il progetto è stato abbandonato o null se l'azione è stata annullata
         */
        void onProjectAbandoned (Group project);
    }
}