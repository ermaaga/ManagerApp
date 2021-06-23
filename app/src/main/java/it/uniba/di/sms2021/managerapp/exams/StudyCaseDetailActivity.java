package it.uniba.di.sms2021.managerapp.exams;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.TemporaryFileDownloader;
import it.uniba.di.sms2021.managerapp.projects.ProjectDetailActivity;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.FileException;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class StudyCaseDetailActivity extends AbstractBottomNavigationActivity {

    private static final int REQUEST_PERMISSION_OPEN_FILE = 1;
    private static final String TAG = "StudyCaseDetailActivity";

    private final Context context = StudyCaseDetailActivity.this;

    private StudyCase studyCase;
    private String idStudyCase;
    private String idExam;
    private Exam exam;

    TextView textName;
    TextView textDesc;
    ImageView fileImageView;

    private DatabaseReference studyCaseReference;
    private ValueEventListener studyCaseListener;
    private ManagerCloudFile studyCaseFile;
    private File studyCaseLocalFile;
    private Menu menu;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_study_case_detail;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        idStudyCase =  intent.getStringExtra(StudyCase.Keys.ID);
        idExam = intent.getStringExtra(Exam.Keys.EXAM);

        textName = (TextView) findViewById(R.id.textView_name_study_case);
        textDesc = (TextView) findViewById(R.id.textView_desc_study_case);

        fileImageView = findViewById(R.id.file_type_image_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS).child(idExam)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        exam = snapshot.getValue(Exam.class);
                        //onStart viene chiamato prima di onCreateOptionsMenu ma qualche volta
                        //il metodo di firebase finisce dopo, quindi metto metodi duplicati
                        //in entrambi i posti
                        if (menu != null &&
                                exam.getProfessors().contains(LoginHelper.getCurrentUser().getAccountId())) {
                            menu.findItem(R.id.action_delete_studyCase).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        fileImageView.setVisibility(View.INVISIBLE);

        studyCaseReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES);
        studyCaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getKey().equals(idStudyCase)) {
                        studyCase = child.getValue(StudyCase.class);
                        textName.setText(studyCase.getNome());
                        textDesc.setText(studyCase.getDescrizione());

                        try {
                            initialiseFileImageView ();
                        } catch (RuntimeException e) {
                            Log.d(TAG, e.getMessage());
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        studyCaseReference.addValueEventListener(studyCaseListener);
    }

    private void initialiseFileImageView() throws RuntimeException {
        FirebaseDbHelper.getStudyCasePathReference(studyCase).listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> storageReferenceList = listResult.getItems();
                        if (storageReferenceList.size() > 1) {
                            throw new RuntimeException("Presente più di un file quando ogni caso di studio" +
                                    "ha solo un file che lo descrive.");
                        }
                        if (storageReferenceList.size() == 0) {
                            fileImageView.setVisibility(View.INVISIBLE);
                            return;
                        }

                        StorageReference reference = storageReferenceList.get(0);
                        reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                studyCaseFile = new ManagerCloudFile(reference, reference.getName(),
                                        storageMetadata.getContentType(),
                                        storageMetadata.getSizeBytes(),
                                        storageMetadata.getUpdatedTimeMillis());
                                FileUtil.setTypeImageView(context, fileImageView, studyCaseFile.getType());

                                addDownloadAction();
                            }
                        });
                    }
                });
    }

    public void addDownloadAction() {
        fileImageView.setVisibility(View.VISIBLE);
        fileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TemporaryFileDownloader(context) {
                    /**
                     * Mostra un messaggio qualora non sia possibile scaricare il file come file temporaneo.
                     *
                     * @param messageRes il messaggio da mostrare
                     */
                    @Override
                    protected void showDownloadFailedMessage(int messageRes) {
                        Toast.makeText(context, 
                                R.string.text_message_study_case_couldnt_find_file,
                                Toast.LENGTH_LONG).show();
                    }

                    /**
                     * Specifica l'azione da compiere quando il file è stato scaricato.
                     *
                     * @param localFile
                     */
                    @Override
                    protected void onSuccessAction(File localFile) {
                        studyCaseLocalFile = localFile;
                        openStudyCaseFile(localFile);
                    }
                }.downloadTempStudyCaseFile(studyCaseFile, studyCase);
            }
        });
    }

    private void openStudyCaseFile(File localFile) {
        try {
            FileUtil.openFileWithViewIntent(context,
                    FileUtil.getUriFromFile(context, localFile),
                    studyCaseFile.getType());
        } catch (FileException e) {
            switch (e.getErrorCode()) {
                case FileException.NO_INTENT_FOUND:
                    Toast.makeText(context,
                            R.string.text_message_downloaded_file_not_supported,
                            Toast.LENGTH_LONG).show();
                    break;
                case FileException.NO_STORAGE_ACCESS:
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_OPEN_FILE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_OPEN_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openStudyCaseFile(studyCaseLocalFile);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_study_case, menu);
        this.menu = menu;

        if (exam != null &&
                exam.getProfessors().contains(LoginHelper.getCurrentUser().getAccountId())) {
            menu.findItem(R.id.action_delete_studyCase).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        if (item.getItemId() == R.id.action_delete_studyCase) {
            showStudyCaseDeletionDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void showStudyCaseDeletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_Dialog_title_delete_studycase)
                .setMessage(R.string.text_message_study_case_deletion)
                .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteStudyCaseGroups(context, studyCase, new OnStudyCaseDeletedListener() {
                            @Override
                            public void onStudyCaseDeleted(StudyCase studyCase) {
                                StudyCaseDetailActivity.this.onStudyCaseDeleted();
                            }
                        });
                    }
                }).setNegativeButton(R.string.text_button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private static void deleteStudyCaseGroups(Context context, StudyCase studyCase, OnStudyCaseDeletedListener listener) {
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Set<String> groupsToDelete = new HashSet<>();
                        List<Group> groups = new ArrayList<>();
                        for (DataSnapshot child: snapshot.getChildren()) {
                            Group group = child.getValue(Group.class);
                            if (group.getStudyCase().equals(studyCase.getId())) {
                                groups.add(group);
                                groupsToDelete.add(group.getId());
                            }
                        }

                        if (groups.isEmpty()) {
                            deleteStudyCase(context, studyCase, listener);
                            return;
                        }

                        for (Group group: groups) {
                            ProjectDetailActivity.deleteProject(context, group, new ProjectDetailActivity.OnProjectDeletedListener() {
                                @Override
                                public void onProjectDeleted(Group project) {
                                    if (project == null) {
                                        Toast.makeText(context,
                                                R.string.text_message_studycase_deletion_error,
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Log.i(TAG, "Deleted group " + group.getName());
                                    groupsToDelete.remove(project.getId());
                                    if (groupsToDelete.isEmpty()) {
                                        Log.i(TAG, "Finished deleting groups.");
                                        deleteStudyCase(context, studyCase, listener);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private static void deleteStudyCase(Context context, StudyCase studyCase, OnStudyCaseDeletedListener listener) {
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_STUDYCASES)
                .child(studyCase.getId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, "Deleted study case");
                deleteStudyCaseFiles(studyCase, listener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,
                        R.string.text_message_studycase_deletion_error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void deleteStudyCaseFiles(StudyCase studyCase, OnStudyCaseDeletedListener listener) {
        FirebaseDbHelper.getStudyCasePathReference(studyCase).listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                listResult.getItems().get(0).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "Deleted Studycase File");
                        listener.onStudyCaseDeleted(studyCase);
                    }
                });
            }
        });
    }

    private void onStudyCaseDeleted() {
        Toast.makeText(context, R.string.text_message_study_case_deleted_successfully,
                Toast.LENGTH_LONG).show();
        finish();
    }

    public void createGroup(View v){
        Intent intent = new Intent(this, NewGroupActivity.class);
        intent.putExtra(StudyCase.Keys.ID, studyCase.getId());
        intent.putExtra(Exam.Keys.EXAM, exam);
        startActivity(intent);
    }

    public interface OnStudyCaseDeletedListener {
        void onStudyCaseDeleted (StudyCase studyCase);
    }
}
