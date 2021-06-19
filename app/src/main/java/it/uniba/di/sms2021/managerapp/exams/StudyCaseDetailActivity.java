package it.uniba.di.sms2021.managerapp.exams;

import android.Manifest;
import android.content.Context;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.TemporaryFileDownloader;
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

    TextView textName;
    TextView textDesc;
    ImageView fileImageView;

    private DatabaseReference studyCaseReference;
    private ValueEventListener studyCaseListener;
    private ManagerCloudFile studyCaseFile;
    private File studyCaseLocalFile;

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
        FirebaseDbHelper.getOldStudyCasePathReference(studyCase).listAll()
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void createGroup(View v){
        Intent intent = new Intent(this, NewGroupActivity.class);
        intent.putExtra(StudyCase.Keys.ID, studyCase.getId());
        intent.putExtra(Exam.Keys.EXAM, idExam);
        startActivity(intent);
    }
}
