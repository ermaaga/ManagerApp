package it.uniba.di.sms2021.managerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.enitities.Course;
import it.uniba.di.sms2021.managerapp.enitities.Department;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProfileActivity extends AbstractBottomNavigationActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivityTag";
    static final int REQUEST_IMAGE_GET = 1;

    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference departmentsReference;
    private DatabaseReference coursesReference;
    private DatabaseReference currentUserReference;
    private StorageReference storageReference;

    User user;
    String userid;
    String currentUserId;
    User userFromLink;
    boolean fromLink;

    TextView textName;
    TextView textSurname;
    TextView textEmail;
    TextView textDepartments;
    TextView textCourses;

    EditText editName;
    EditText editSurname;

    Button editButton;
    Button saveButton;
    ImageButton editDepartments;
    ImageButton editCourses;
    FloatingActionButton editPhotoButton;

    ImageView photoProfile;

    Uri fullFileUri;
    Bitmap bitmap;

    List<String> departmentsChecked;
    List<String> coursesChecked;
    List<String> currentListDepartment;
    List<String> currentListCourse;
    String[] departmentList;
    String[] courseList;
    String[] departmentListId;
    String[] courseListId;
    boolean[] depIsChecked;
    boolean[] courseIsChecked;

    HashMap childUpdates;

    private ValueEventListener userListener;
    private ValueEventListener userListenerCreate;
    private ValueEventListener departmentsListener;
    private ValueEventListener coursesListener;

    MenuItem iconSave;
    MenuItem iconEdit;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        iconSave = menu.findItem(R.id.action_save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        //Se viene selezionata l'icona di modifica
        if (menuId == R.id.action_edit) {
            //Chiamata al metodo che permette la modifica dei dati di profilo
            editProfile();
            /*//Rimpiazza l'icona di modifica con l'icona "spunta" per salvare le modifiche
            Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_done_24, getApplicationContext().getTheme());
            item.setIcon(myDrawable);*/
            item.setVisible(false);
            iconSave.setVisible(true);
        }
        if(menuId == R.id.action_save){
            saveProfile();
            item.setVisible(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(editPhotoButton.getVisibility()==View.VISIBLE){
            Intent refresh = new Intent(ProfileActivity.this, ProfileActivity.class);
            startActivity(refresh);
            finish();
        } else if (fragmentManager.getBackStackEntryCount() == 0) {
            finish();
            Log.d(TAG, "count==0");
        } else {
            fragmentManager.popBackStack();
            Log.d(TAG, "ultimo log");
        }

        return super.onSupportNavigateUp();
    }

    @Override
    protected int getLayoutId()  {
        return R.layout.activity_profile;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_profile;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textName = findViewById(R.id.value_name_account_text_view);
        textSurname = (TextView) findViewById(R.id.value_surname_account_text_view);
        textEmail = (TextView) findViewById(R.id.value_email_account_text_view);
        textDepartments = (TextView) findViewById(R.id.value_department);
        textCourses = (TextView) findViewById(R.id.value_course);

        editName = (EditText) findViewById(R.id.value_name_account_edit_text);
        editSurname = (EditText) findViewById(R.id.value_surname_account_edit_text);

        photoProfile = (ImageView) findViewById(R.id.image_account);

        editDepartments = (ImageButton) findViewById(R.id.departments_button);
        editCourses = (ImageButton) findViewById(R.id.courses_button);
        editPhotoButton = (FloatingActionButton) findViewById(R.id.button_uplod_photo);

        editPhotoButton.setOnClickListener(this);

        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        departmentsReference = database.getReference(FirebaseDbHelper.TABLE_DEPARTMENTS);
        coursesReference = database.getReference(FirebaseDbHelper.TABLE_COURSES);
        storageReference = FirebaseStorage.getInstance().getReference();

        departmentsChecked = new ArrayList<String>();
        coursesChecked = new ArrayList<String>();

        fromLink= getIntent().getBooleanExtra("fromLinkBoolean", false);
        userFromLink = getIntent().getParcelableExtra(User.KEY);

        //TODO considerare l'utilizzo di LoginHelper.getCurrentUser()
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*Se viene visualizzato il profilo di un altro utente, invece che il profilo corrente,
        * l'id dell'utente viene ottenuto dall'oggetto passato tramite Intent e non viene
        * permessa la modifica del profilo */
        if(fromLink){
            userid = userFromLink.getAccountId();
            if(!userFromLink.getAccountId().equals(currentUserId)){
                editButton.setVisibility(View.GONE);
            }
        }else{
            userid = currentUserId;
        }

        currentUserReference = usersReference.child(userid);
    }

    @Override
    protected void onStart() {
        super.onStart();

        userListenerCreate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user = snapshot.getValue(User.class);
                //Se il nodo "profileImage" è popolato nel database
                if(user.getProfileImage()!= null){
                    //Acquisizione dell'Url dell'immagine dell'utente presente nello Storage
                    storageReference.child("profileimages/"+userid).child(user.getProfileImage()).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set ImageView foto profilo
                                    Glide.with(ProfileActivity.this)
                                            .load(uri)
                                            .into(photoProfile);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG," Failed setImageView");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        currentUserReference.addListenerForSingleValueEvent(userListenerCreate);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user = snapshot.getValue(User.class);

                textName.setText(user.getNome());
                textSurname.setText(user.getCognome());
                textEmail.setText(user.getEmail());

                /*Set testo etichette dei dipartimenti e dei corsi in base
                al numero dei dipartimenti e dei corsi*/
                int sizeDepartments = user.getDipartimenti().size();
                TextView labelDepartments = (TextView) findViewById(R.id.label_departments);
                labelDepartments.setText(getResources().getQuantityString(R.plurals.numberOfDepartments, sizeDepartments));

                int sizeCourses = user.getCorsi().size();
                TextView labelCourses = (TextView) findViewById(R.id.label_courses);
                labelCourses.setText(getResources().getQuantityString(R.plurals.numberOfCourses, sizeCourses));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        currentUserReference.addValueEventListener(userListener);


        departmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textDepartments.setText("");
                //Se non è stata modificata la lista dei dipartimenti
                if(currentListDepartment==null) {
                    // Iterazione tra i vari elementi appartenenti al nodo "departments"
                    for (String dep : user.getDipartimenti()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey().equals(dep)) {
                                Log.d(TAG, "Id of child: " + child.getKey());
                                Department department = child.getValue(Department.class);

                                textDepartments.append(department.getName() + "\n");

                                //Lista dei dipartimenti dell'utente
                                departmentsChecked.add(department.getId());
                            }
                        }
                    }
                }else{
                    //Iterazione tra i vari elementi presenti nella lista dei dipartimenti correnti
                    for (String depart: currentListDepartment){
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey().equals(depart)) {
                                Log.d(TAG, "Id of child: " + child.getKey());
                                Department department = child.getValue(Department.class);

                                textDepartments.append(department.getName() + "\n");

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        departmentsReference.addValueEventListener(departmentsListener);

        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textCourses.setText("");
                //se non è stata modificata la lista dei corsi
                if(currentListCourse==null) {
                    // Iterazione tra i vari elementi presenti nel nodo "courses" dell'utente
                    for (String c : user.getCorsi()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey().equals(c)) {
                                Log.d(TAG, "Id of child: " + child.getKey());
                                Course course = child.getValue(Course.class);

                                textCourses.append(course.getName() + "\n");

                                //Lista dei corsi dell'utente
                                coursesChecked.add(course.getId());
                            }
                        }
                    }
                }else{
                    //Iterazione tra i vari elementi presenti nella lista dei corsi correnti
                    for (String c: currentListCourse){
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey().equals(c)) {
                                Log.d(TAG, "Id of child: " + child.getKey());
                                Course course = child.getValue(Course.class);

                                textCourses.append(course.getName() + "\n");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        coursesReference.addValueEventListener(coursesListener);

        editDepartments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDepartments();
            }
        });

        editCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCourses();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentUserReference.removeEventListener(userListener);
        departmentsReference.removeEventListener(departmentsListener);
        coursesReference.removeEventListener(coursesListener);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.button_uplod_photo){
            selectImage();
        }
    }

    private void selectImage() {
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
            try {
                InputStream inputStream = getContentResolver().openInputStream(fullFileUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                photoProfile.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                //TODO vedere cosa fare
                Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Metodo usato per modificare le TextView in EditText
    public void editProfile() {
        textName.setVisibility(View.GONE);
        textSurname.setVisibility(View.GONE);

        editName.setVisibility(View.VISIBLE);
        editSurname.setVisibility(View.VISIBLE);
        editDepartments.setVisibility(View.VISIBLE);
        editCourses.setVisibility(View.VISIBLE);
        editPhotoButton.setVisibility(View.VISIBLE);

        editName.setText(textName.getText());
        editSurname.setText(textSurname.getText());
    }

    public void saveProfile() {

        childUpdates = new HashMap();

        if(fullFileUri!= null) {

            //Eliminazione della foto profilo eventualmente già presente nello storage
            storageReference.child("profileimages/"+userid).listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference item : listResult.getItems()) {
                                item.delete();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO vedere cosa fare
                        }
                    });

            //Creazione dei metadati del file
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType(FileUtil.getMimeTypeFromUri(ProfileActivity.this, fullFileUri))
                    .build();


            //Caricamento dell'immagine nello Storage
            UploadTask uploadTask = storageReference.child("profileimages/"+userid)
                    .child(FileUtil.getFileNameFromURI(ProfileActivity.this, fullFileUri))
                    .putFile(fullFileUri, metadata);

            //Listener per i cambiamenti di stato, gli errori e il completamento del caricamento.
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
                    Toast.makeText(getApplicationContext(), R.string.text_message_photo_profile_failure, Toast.LENGTH_SHORT).show();
                    // TODO Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "File caricato con successo");
                    childUpdates.put("/profileImage/", FileUtil.getFileNameFromURI(ProfileActivity.this, fullFileUri));
                }
            });
        }

        childUpdates.put("/nome/", editName.getText().toString());
        childUpdates.put("/cognome/", editSurname.getText().toString());
        if(currentListDepartment!=null){
            childUpdates.put("/dipartimenti/", currentListDepartment);
        }
        if(currentListCourse!=null){
            childUpdates.put("/corsi/", currentListCourse);
        }
        usersReference.child(user.getAccountId()).updateChildren(childUpdates);

        Intent refresh = new Intent(ProfileActivity.this, ProfileActivity.class);
        startActivity(refresh);
        finish();
    }

    public void editDepartments() {
        departmentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                departmentList = new String[(int) dataSnapshot.getChildrenCount()];
                depIsChecked= new boolean[(int) dataSnapshot.getChildrenCount()];
                departmentListId = new String[(int) dataSnapshot.getChildrenCount()];
                int i = 0;

                for (DataSnapshot dep : dataSnapshot.getChildren()) {
                    departmentList[i]=dep.getValue(Department.class).getName();
                    departmentListId[i]=dep.getValue(Department.class).getId();

                    boolean found = false;
                    for(String departmentChecked: departmentsChecked){
                        if(found==false) {
                            if (dep.getKey().equals(departmentChecked)) {
                                depIsChecked[i] = true;
                                found=true;
                            } else {
                                depIsChecked[i] = false;
                            }
                        }
                    }
                    i++;
                }

                showDeparmentChooserDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void editCourses() {
        coursesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                courseList = new String[(int) dataSnapshot.getChildrenCount()];
                courseIsChecked= new boolean[(int) dataSnapshot.getChildrenCount()];
                courseListId = new String[(int) dataSnapshot.getChildrenCount()];
                int i = 0;

                for (DataSnapshot course : dataSnapshot.getChildren()) {
                    courseList[i]=course.getValue(Course.class).getName();
                    courseListId[i]=course.getValue(Course.class).getId();

                    boolean found = false;
                    for(String courseChecked: coursesChecked){
                        if(found==false) {
                            if (course.getKey().equals(courseChecked)) {
                                courseIsChecked[i] = true;
                                found=true;
                            } else {
                                courseIsChecked[i] = false;
                            }
                        }
                    }
                    i++;
                }

                showCourseChooserDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showDeparmentChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

        builder.setMultiChoiceItems(departmentList, depIsChecked, new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                depIsChecked[which] = isChecked;
                if (((AlertDialog) dialog).getListView().getCheckedItemCount() == 0) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    Toast.makeText(ProfileActivity.this, R.string.text_message_alert_dialog_department, Toast.LENGTH_SHORT).show();
                }else{
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                    currentListDepartment = new ArrayList<String>();
                    textDepartments.setText("");
                    for (int i = 0; i < depIsChecked.length; i++) {
                        boolean checked = depIsChecked[i];
                        if (checked) {
                            currentListDepartment.add(departmentListId[i]);
                            textDepartments.append(departmentList[i] + "\n");
                        }
                    }
                    departmentsChecked = currentListDepartment;
            }
        });

        builder.setTitle(R.string.text_label_dialog_title_departments);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCourseChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

        builder.setMultiChoiceItems(courseList, courseIsChecked, new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                courseIsChecked[which] = isChecked;
                if (((AlertDialog) dialog).getListView().getCheckedItemCount() == 0) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    Toast.makeText(ProfileActivity.this, R.string.text_message_alert_dialog_course, Toast.LENGTH_SHORT).show();
                }else{
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                currentListCourse = new ArrayList<String>();
                textCourses.setText("");
                for (int i = 0; i < courseIsChecked.length; i++) {
                    boolean checked = courseIsChecked[i];
                    if (checked) {
                        currentListCourse.add(courseListId[i]);
                        textCourses.append(courseList[i] + "\n");
                    }
                }
                coursesChecked = currentListCourse;
            }
        });

        builder.setTitle(R.string.text_label_dialog_title_courses);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
