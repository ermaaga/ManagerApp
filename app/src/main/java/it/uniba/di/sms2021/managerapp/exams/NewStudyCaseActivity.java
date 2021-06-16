package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

public class NewStudyCaseActivity extends AbstractFormActivity implements View.OnClickListener {
    static final int REQUEST_IMAGE_GET = 1;
    private static final String TAG = "NewStudyCaseActivity";
    private static final Long MAX_FILE_SIZE_IN_MB = 1L;

    private FirebaseDatabase database;
    private DatabaseReference studycasesReference;

    Button buttoncreate;
    ImageButton buttonchoose;
    ImageButton buttondelete;

    TextInputEditText name;
    TextInputEditText desc;

    TextInputLayout nameInputlayout;
    TextInputLayout descInputlayout;

    ImageView typeImageView;
    TextView nameTextView;
    TextView sizeTextView;
    TextView uploadTextView;

    String idExam;

    Uri fullFileUri;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_study_case;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);

        buttoncreate = (Button) findViewById(R.id.button_create_study_case);
        buttonchoose = (ImageButton) findViewById(R.id.button_select_file);
        buttondelete = (ImageButton) findViewById(R.id.button_delete);

        buttoncreate.setOnClickListener(this);
        buttonchoose.setOnClickListener(this);
        buttondelete.setOnClickListener(this);

        name = (TextInputEditText) findViewById(R.id.name_edit_text);
        desc = (TextInputEditText) findViewById(R.id.desc_edit_text);

        nameInputlayout = (TextInputLayout) findViewById(R.id.name_input_layout);
        descInputlayout = (TextInputLayout) findViewById(R.id.desc_input_layout);

        typeImageView = (ImageView) findViewById(R.id.file_type_image_view);
        nameTextView = (TextView) findViewById(R.id.file_name_text_view);
        sizeTextView = (TextView) findViewById(R.id.file_size_text_view);
        uploadTextView = (TextView) findViewById(R.id.upload_text_view);

        database=FirebaseDbHelper.getDBInstance();
        studycasesReference=database.getReference(FirebaseDbHelper.TABLE_STUDYCASES);

        Intent intent=getIntent();
        idExam = intent.getStringExtra(Exam.Keys.ID);

    }
    
   private boolean validate(String textname, String textdesc){
        boolean valid = true;

        if(textname.length()==0) {
            valid=false;
            nameInputlayout.setError(getString(R.string.required_field));

        }
        if(textdesc.length()==0) {
            valid=false;
            descInputlayout.setError(getString(R.string.required_field));
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_select_file) {
            selectDocument();
        } else if (id == R.id.button_create_study_case) {
            createNewStudyCase();
        } else if (id == R.id.button_delete) {
            deleteFile();
        }
    }



    private void createNewStudyCase(){
        if(validate(name.getText().toString(),desc.getText().toString())){
            //Ho modificato questa parte per inizializzare il caso di studio con un id.
            DatabaseReference newElement=studycasesReference.push();

            StudyCase studycase=new StudyCase(newElement.getKey(),
            name.getText().toString(),desc.getText().toString(),idExam);
            newElement.setValue(studycase);

            if(fullFileUri!= null) {
                // Create the file metadata
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType(FileUtil.getMimeTypeFromUri(NewStudyCaseActivity.this, fullFileUri))
                        .build();

                UploadTask uploadTask = FirebaseDbHelper.getStudyCaseFileReference(
                        studycase,
                            FileUtil.getFileNameFromURI(this, fullFileUri))
                        .putFile(fullFileUri, metadata);

                // Listen for state changes, errors, and completion of the upload.
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        // TODO implementare schermata di dialogo con progresso
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "Upload is " + progress + "% done");
                    }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Upload is paused");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "File non caricato");
                        Toast.makeText(getApplicationContext(), R.string.text_message_file_study_case_failure, Toast.LENGTH_SHORT).show();
                        // TODO Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "File caricato con successo");
                        // TODO Handle successful uploads on complete
                        // ...
                    }
                });

            }
            Toast.makeText(getApplicationContext(), R.string.text_message_study_case_created, Toast.LENGTH_SHORT).show();
            NewStudyCaseActivity.super.onBackPressed();
        }
    }

    private void selectDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }

    private void deleteFile() {
        fullFileUri=null;
        nameTextView.setVisibility(View.INVISIBLE);
        sizeTextView.setVisibility(View.INVISIBLE);
        typeImageView.setVisibility(View.INVISIBLE);
        buttonchoose.setVisibility(View.VISIBLE);
        uploadTextView.setVisibility(View.VISIBLE);
        buttondelete.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            fullFileUri = data.getData();

            Long size =  FileUtil.getFileSizeFromURI(NewStudyCaseActivity.this, fullFileUri);

            if (size > MAX_FILE_SIZE_IN_MB * 1024 * 1024) {
                Toast.makeText(this, R.string.text_message_study_case_file_too_big,
                        Toast.LENGTH_LONG).show();
                return;
            }

            nameTextView.setText(FileUtil.getFileNameFromURI(NewStudyCaseActivity.this, fullFileUri));
            sizeTextView.setText(FileUtil.getFormattedSize(NewStudyCaseActivity.this, size));
            FileUtil.setTypeImageView(NewStudyCaseActivity.this, typeImageView, FileUtil.getMimeTypeFromUri(NewStudyCaseActivity.this, fullFileUri));

            nameTextView.setVisibility(View.VISIBLE);
            sizeTextView.setVisibility(View.VISIBLE);
            typeImageView.setVisibility(View.VISIBLE);
            buttondelete.setVisibility(View.VISIBLE);
            buttonchoose.setVisibility(View.GONE);
            uploadTextView.setVisibility(View.GONE);
        }
    }

}
