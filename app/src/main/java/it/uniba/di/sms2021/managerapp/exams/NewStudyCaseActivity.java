package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
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
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
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
    ImageButton buttonchoose;

    TextInputEditText name;
    TextInputEditText desc;

    MaterialCardView cardView;
    ImageView typeImageView;
    TextView nameTextView;
    TextView sizeTextView;

    String idExam;

    Uri fullFileUri;

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

        buttoncreate = (Button) findViewById(R.id.button_create_study_case);
        buttonchoose = (ImageButton) findViewById(R.id.button_select_file);

        buttoncreate.setOnClickListener(this);
        buttonchoose.setOnClickListener(this);

        name = (TextInputEditText) findViewById(R.id.name_edit_text);
        desc = (TextInputEditText) findViewById(R.id.desc_edit_text);

        cardView = (MaterialCardView) findViewById(R.id.file_card);
        typeImageView = (ImageView) findViewById(R.id.file_type_image_view);
        nameTextView = (TextView) findViewById(R.id.file_name_text_view);
        sizeTextView = (TextView) findViewById(R.id.file_size_text_view);

        database=FirebaseDbHelper.getDBInstance();
        studycasesReference=database.getReference(FirebaseDbHelper.TABLE_STUDYCASES);

        Intent intent=getIntent();
        idExam = intent.getStringExtra(Exam.Keys.ID);

        storageRef = FirebaseStorage.getInstance().getReference();

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
        switch (v.getId()) {
            case R.id.button_select_file:
                selectDocument();
                break;
            case R.id.button_create_study_case:
               createNewStudyCase();
                break;
        }
    }

    private void createNewStudyCase(){
        if(validate(name,desc)){
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

                // Upload file and metadata to the path 'images/mountains.jpg'
                UploadTask uploadTask = storageRef.child("Exam" + idExam).child("StudyCase" + newElement.getKey())
                        .child(FileUtil.getFileNameFromURI(NewStudyCaseActivity.this, fullFileUri))
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data"); /*TODO farci qualcosa come mostrare una finestra di dialogo
                                                                         che mostri il progresso dell'upload e che usi il thumbnail*/
            fullFileUri = data.getData();

            Long size =  FileUtil.getFileSizeFromURI(NewStudyCaseActivity.this, fullFileUri);

            nameTextView.setText(FileUtil.getFileNameFromURI(NewStudyCaseActivity.this, fullFileUri));
            sizeTextView.setText(FileUtil.getFormattedSize(NewStudyCaseActivity.this, size));
            FileUtil.setTypeImageView(NewStudyCaseActivity.this, typeImageView, FileUtil.getMimeTypeFromUri(NewStudyCaseActivity.this, fullFileUri));

            nameTextView.setVisibility(View.VISIBLE);
            sizeTextView.setVisibility(View.VISIBLE);
            typeImageView.setVisibility(View.VISIBLE);
            buttonchoose.setVisibility(View.INVISIBLE);
        }
    }

}