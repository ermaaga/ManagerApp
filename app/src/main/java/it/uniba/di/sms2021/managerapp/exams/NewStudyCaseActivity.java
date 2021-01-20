package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

//TODO renderlo un fragment?
public class NewStudyCaseActivity extends AbstractFormActivity implements View.OnClickListener {
    static final int REQUEST_IMAGE_GET = 1;
    private static final String TAG = "NewStudyCaseActivity";

    private FirebaseDatabase database;
    private DatabaseReference studycasesReference;

    StorageReference storageRef;

    Button buttoncreate;
    Button buttonchoose;

    TextInputEditText name;
    TextInputEditText desc;

    String idExam;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_study_case;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buttoncreate = findViewById(R.id.button_create_study_case);
        buttonchoose = findViewById(R.id.button_select_file);

        buttoncreate.setOnClickListener(this);
        buttonchoose.setOnClickListener(this);

        name = (TextInputEditText) findViewById(R.id.name_edit_text);
        desc = (TextInputEditText) findViewById(R.id.desc_edit_text);

        database=FirebaseDbHelper.getDBInstance();
        studycasesReference=database.getReference(FirebaseDbHelper.TABLE_STUDYCASES);

        Intent intent=getIntent();
        idExam = intent.getStringExtra(Exam.Keys.ID);

        storageRef = FirebaseStorage.getInstance().getReference();
        Log.d(TAG, "oncreate");

    }


   private boolean validate(EditText name, EditText desc){
        boolean valid = true;

        if(name.getText().toString().length()==0) {
            valid=false;
            name.setError(getString(R.string.required_field));

        }
        if(desc.getText().toString().length()==0) {
            valid=false;
            desc.setError(getString(R.string.required_field));
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Switch");
        switch (v.getId()) {
            case R.id.button_select_file:
                selectDocument();
                Log.d(TAG, "dentro case Switch");
                break;
            case R.id.button_create_study_case:
              // createNewStudyCase();
                break;
        }
    }

    private void createNewStudyCase(Uri file){
        if(validate(name,desc)){
            Log.d(TAG, "createstudycase");
            //Ho modificato questa parte per inizializzare il caso di studio con un id.
            DatabaseReference newElement=studycasesReference.push();

            StudyCase studycase=new StudyCase(newElement.getKey(),
            name.getText().toString(),desc.getText().toString(),idExam);
            newElement.setValue(studycase);

            // Create the file metadata
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType(FileUtil.getMimeTypeFromUri(NewStudyCaseActivity.this, file))
                    .build();

            // Upload file and metadata to the path 'images/mountains.jpg'
            UploadTask uploadTask = storageRef.child("Exam"+idExam).child("StudyCase"+newElement.getKey())
                    .child(FileUtil.getFileNameFromURI(NewStudyCaseActivity.this, file))
                    .putFile(file, metadata);

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
                    // TODO Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(),R.string.text_message_study_case_created,Toast.LENGTH_SHORT).show();
                    NewStudyCaseActivity.super.onBackPressed();
                    // TODO Handle successful uploads on complete
                    // ...
                }
            });

        }
    }

    private void selectDocument() {
        Log.d(TAG, "Selectdocument");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data"); /*TODO farci qualcosa come mostrare una finestra di dialogo
                                                                         che mostri il progresso dell'upload e che usi il thumbnail*/
            Uri fullFileUri = data.getData();
            Log.d(TAG, "dentro if onactivityresult");
            createNewStudyCase(fullFileUri);
        }
    }

}
