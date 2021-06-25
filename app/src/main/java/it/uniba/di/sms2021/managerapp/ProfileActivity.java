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
import it.uniba.di.sms2021.managerapp.exams.ExamsActivity;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.projects.ProjectsActivity;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProfileActivity extends AbstractBottomNavigationActivity {
    private static final String TAG = "ProfileActivityTag";
    static final int REQUEST_IMAGE_GET = 1;
    private static final String PROFILE_ACTIVITY = "ProfileActivity";
    private static final String EXAMS_ACTIVITY = "ExamsActivity";
    private static final String PROJECTS_ACTIVITY = "ProjectsActivity";

    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference departmentsReference;
    private DatabaseReference coursesReference;
    private DatabaseReference currentUserReference;
    private DatabaseReference examsReference;
    private StorageReference storageReference;

    User user;
    String userid;
    String currentUserId;
    User userFromLink;
    boolean fromLink;

    String activityIntent;

    TextView textName;
    TextView textSurname;
    TextView textEmail;
    TextView textDepartments;
    TextView textCourses;

    EditText editName;
    EditText editSurname;

    ImageButton editDepartments;
    ImageButton editCourses;
    FloatingActionButton editPhotoButton;

    ImageView photoProfile;

    Uri fullFileUri;
    Bitmap bitmap;

    List<String> userDepartments;
    List<String> userCourses;
    List<String> departmentsAfterChange;
    List<String> coursesAfterChange;
    List<String> coursesNameList;
    List<String> coursesIdList;
    List<Boolean> courseIsChecked;
    List<Course> allCourses;
    List<Department> allDepartments;
    String[] departmentsNameList;
    String[] coursesNameArray;
    String[] departmentsIdList;
    String[] coursesIdArray;
    boolean[] depIsChecked;
    boolean[] courseIsCheckedArray;

    HashMap childUpdates;

    private ValueEventListener userListener;
    private ValueEventListener userListenerCreate;
    private ValueEventListener departmentsListener;
    private ValueEventListener coursesListener;
    private ValueEventListener examsListener;

    MenuItem iconSave;
    MenuItem iconEdit;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        iconSave = menu.findItem(R.id.action_save);
        iconEdit = menu.findItem(R.id.action_edit);

        /*Se viene visualizzato il profilo di un altro utente, invece che il profilo corrente,
         * non viene permessa la modifica del profilo */
        if(!userid.equals(currentUserId)){
            iconEdit.setVisible(false);
        }
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
            //Rimpiazza l'icona di modifica con l'icona "spunta" per salvare le modifiche
            item.setVisible(false);
            iconSave.setVisible(true);
        }
        /*Se viene selezionata l'icona "spunta" le eventuali modifiche vengono salvate
        e viene aggiornata l'activity.*/
        if(menuId == R.id.action_save){
            activityIntent=PROFILE_ACTIVITY;
            saveProfile(activityIntent);
            item.setVisible(false);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        /*Se ci si trova in modifica profilo, ma non ci sono modifiche apportate,
         * si ritorna in visualizza profilo.
         * Nel caso in cui ci siano modifiche apportate (quindi non ancora salvate),
         * viene visualizzato un dialog di conferma delle modifiche.*/
        if(editPhotoButton.getVisibility()==View.VISIBLE && !thereAreUnsavedChanges()){
            ProfileActivity.this.recreate();
        } else if(editPhotoButton.getVisibility()==View.VISIBLE && thereAreUnsavedChanges()){
            Log.d(TAG, "thereAreUnsavedChanges"+thereAreUnsavedChanges());
            activityIntent=PROFILE_ACTIVITY;
            displaySaveRequestDialog(activityIntent);
        } else if (fragmentManager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            fragmentManager.popBackStack();
        }

        return super.onSupportNavigateUp();
    }

    /*Override del metodo per controllare la navigazione all'interno della bottom navigation
     * in questa specifica activity. In particolare viene coperto il caso
     * in cui ci si sposta tra le activity senza aver salvato le eventuali
     * modifiche del profilo*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_profile) {
            activityIntent=PROFILE_ACTIVITY;
            if(editPhotoButton.getVisibility()==View.VISIBLE && thereAreUnsavedChanges()) {
                displaySaveRequestDialog(activityIntent);
            }else{
                goIntent(activityIntent);
            }
        } else if (itemId == R.id.nav_exams) {
            activityIntent=EXAMS_ACTIVITY;
            if(editPhotoButton.getVisibility()==View.VISIBLE && thereAreUnsavedChanges()){
                displaySaveRequestDialog(activityIntent);
            }else{
                goIntent(activityIntent);
            }
        } else if (itemId == R.id.nav_projects) {
            activityIntent=PROJECTS_ACTIVITY;
            if(editPhotoButton.getVisibility()==View.VISIBLE && thereAreUnsavedChanges()){
                displaySaveRequestDialog(activityIntent);
            }else{
                goIntent(activityIntent);
            }
        }
        return false;
    }

    //Metodo utilizzato per mostrare il dialog di conferma delle modifiche di profilo effettuate
    private void displaySaveRequestDialog(String activityIntent) {
        Log.d(TAG, "displaySaveRequestDialog");
        new AlertDialog.Builder(this)
                .setMessage(R.string.text_message_unsaved_changes)
                .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveProfile(activityIntent);
                    }
                })
                .setNegativeButton(R.string.text_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goIntent(activityIntent);
                    }
                })
                .setNeutralButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    //Metodo utilizzato per verificare se ci sono state modifiche del profilo.
    private boolean thereAreUnsavedChanges() {

        return !editName.getText().toString().equals(user.getNome()) ||
                !editSurname.getText().toString().equals(user.getCognome()) ||
                departmentsAfterChange !=null ||
                coursesAfterChange !=null ||
                //Viene controllato prima se l'immagine è stata modificata e dopo, in caso affermativo, se essa è diversa dalla vecchia.
                (fullFileUri!=null &&
                        !FileUtil.getFileNameFromURI(ProfileActivity.this, fullFileUri).equals(user.getProfileImage()));
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

        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        departmentsReference = database.getReference(FirebaseDbHelper.TABLE_DEPARTMENTS);
        coursesReference = database.getReference(FirebaseDbHelper.TABLE_COURSES);
        examsReference = database.getReference(FirebaseDbHelper.TABLE_EXAMS);
        storageReference = FirebaseStorage.getInstance().getReference();

        userDepartments = new ArrayList<String>();
        userCourses = new ArrayList<String>();

        fromLink= getIntent().getBooleanExtra("fromLinkBoolean", false);
        userFromLink = getIntent().getParcelableExtra(User.KEY);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*Se viene visualizzato il profilo di un altro utente, invece che il profilo corrente,
         * l'id dell'utente viene ottenuto dall'oggetto passato tramite Intent*/
        if(fromLink){
            userid = userFromLink.getAccountId();
        }else{
            userid = currentUserId;
        }

        currentUserReference = usersReference.child(userid);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "on start");

        /*Se proviene dalla modifica dell'immagine, imposta l'immagine di profilo modificata,
         altrimenti la acquisisce dal database*/
        if(fullFileUri!=null){
            try {
                InputStream inputStream = getContentResolver().openInputStream(fullFileUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                photoProfile.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                //TODO vedere cosa fare
                Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_LONG).show();
            }
        }else {
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
                                Log.d(TAG," Failed setImageView"+ exception);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            currentUserReference.addListenerForSingleValueEvent(userListenerCreate);
        }

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
                userDepartments.clear();
                textDepartments.setText("");
                //Se non è stata modificata la lista dei dipartimenti
                if(departmentsAfterChange ==null) {
                    allDepartments = new ArrayList<Department>();

                    /* Iterazione tra i vari elementi appartenenti al nodo "departments"
                     in RealtimeDatabase e selezione dei soli dipartimenti appartenenti
                     all'utente corrente*/
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Department department = child.getValue(Department.class);
                        allDepartments.add(department);
                        for (String dep : user.getDipartimenti()) {
                            if (child.getKey().equals(dep)) {
                                //Aggiunta del dipartimento alla textView
                                textDepartments.append(department.getName() + "\n");

                                //Lista dei dipartimenti dell'utente presenti nel database
                                userDepartments.add(department.getId());
                                Log.d(TAG, "userDepartments"+userDepartments.toString());
                            }
                        }
                    }
                }else{
                    allDepartments = new ArrayList<Department>();
                    //Iterazione tra i vari elementi presenti nella lista dei dipartimenti dopo la modifica
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Department department = child.getValue(Department.class);
                        allDepartments.add(department);
                        for (String depart: departmentsAfterChange){
                            if (child.getKey().equals(depart)) {

                                //Aggiunta del dipartimento alla textView
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
                userCourses.clear();
                textCourses.setText("");
                //Se non è stata modificata la lista dei corsi
                if(coursesAfterChange == null) {
                    allCourses = new ArrayList<Course>();
                    /*Iterazione tra i vari elementi presenti nel nodo "courses"
                    in RealtimeDatabase e selezione dei soli corsi appartenenti
                    all'utente corrente*/
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Course course = child.getValue(Course.class);
                        allCourses.add(course);
                        for (String c : user.getCorsi()) {
                            if (child.getKey().equals(c)) {
                                //Aggiunta del corso alla textView
                                textCourses.append(course.getName() + "\n");
                                //Lista dei corsi dell'utente presenti nel database
                                userCourses.add(course.getId());
                                Log.d(TAG, "userCourses"+userCourses.toString());
                            }
                        }
                    }
                }else{
                    allCourses = new ArrayList<Course>();
                    //Iterazione tra i vari elementi presenti nella lista dei corsi dopo la modifica
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Course course = child.getValue(Course.class);
                        allCourses.add(course);
                        for (String c: coursesAfterChange){
                            if (child.getKey().equals(c)) {
                                //Aggiunta del corso alla textView
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

        editCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCourses();
            }
        });
        editPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
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

    //SCRIVERE COMMENTO
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }

    //SCRIVERE COMMENTO
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

    /*Metodo usato per modificare le TextView in EditText nel momento in cui
    * si decide di modificare il profilo (cliccando l'icona nella toolbar)*/
    public void editProfile() {
        textName.setVisibility(View.GONE);
        textSurname.setVisibility(View.GONE);
        textEmail.setTextColor(getResources().getColor(R.color.grey, null));

        editName.setVisibility(View.VISIBLE);
        editSurname.setVisibility(View.VISIBLE);
        editDepartments.setVisibility(View.VISIBLE);
        editCourses.setVisibility(View.VISIBLE);
        editPhotoButton.setVisibility(View.VISIBLE);

        editName.setText(textName.getText());
        editSurname.setText(textSurname.getText());
    }

    //Salva le modifiche del profilo
    public void saveProfile(String activityIntent) {
        childUpdates = new HashMap();

        childUpdates.put("/nome/", editName.getText().toString());
        childUpdates.put("/cognome/", editSurname.getText().toString());
        //Se ci sono state modifiche dei dipartimenti
        if (departmentsAfterChange != null) {
            childUpdates.put("/dipartimenti/", departmentsAfterChange);
        }
        //Se ci sono state modifiche dei corsi
        if (coursesAfterChange != null) {
            childUpdates.put("/corsi/", coursesAfterChange);
        }
        //Aggiorna i nodi  nel database con i valori specificati.
        usersReference.child(user.getAccountId()).updateChildren(childUpdates);

        //Se l'immagine di profilo non è stata modificata, ritorna a visualizza profilo (ricaricando la pagina).
        if(fullFileUri==null){
            goIntent(activityIntent);
        }else if(fullFileUri!= null) { //Se l'immagine di profilo è stata modificata
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
                //Se il caricamento del file nello storage avviene con successo
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "File caricato con successo");
                    //Il nome del file viene salvato in Realtime Database, aggiornando il nodo "profileImage".
                    childUpdates.put("/profileImage/", FileUtil.getFileNameFromURI(ProfileActivity.this, fullFileUri));
                    usersReference.child(user.getAccountId()).updateChildren(childUpdates);

                    //Ritorna a visualizza profilo (ricaricando la pagina).
                    goIntent(activityIntent);
                }
            });
        }
    }

    //Metodo usato per spostarsi nell'activity passata come parametro
    private void goIntent(String activityIntent) {
        if(activityIntent.equals(PROFILE_ACTIVITY)){
            ProfileActivity.this.recreate();
        }else if(activityIntent.equals(EXAMS_ACTIVITY)){
            startActivity(new Intent(this, ExamsActivity.class));
        }else if(activityIntent.equals(PROJECTS_ACTIVITY)){
            startActivity(new Intent(this, ProjectsActivity.class));
        }
    }

    public void editDepartments() {
        departmentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                departmentsNameList = new String[(int) dataSnapshot.getChildrenCount()];
                departmentsIdList = new String[(int) dataSnapshot.getChildrenCount()];
                depIsChecked= new boolean[(int) dataSnapshot.getChildrenCount()];
                int i = 0;

                for (DataSnapshot dep : dataSnapshot.getChildren()) {
                    /*Popolamento array contenenti rispettivamente i nomi e gli id dei dipartimenti presenti nel database
                    * (nome e id di uno stesso dipartimento avranno stessa posizione nei due diversi array)*/
                    departmentsNameList[i]=dep.getValue(Department.class).getName();
                    departmentsIdList[i]=dep.getValue(Department.class).getId();

                    /*Popolamento array "depIsChecked" per indicare se il dipartimento nella posizione "i" dei precedenti array
                    deve essere spuntato o meno nel dialog della modifica dei dipartimenti (viene spuntato se esso fa parte della lista dei dipartimenti dell'utente)*/
                    boolean found = false;
                    for(String userDepartments: userDepartments){
                        if(found==false) {
                            if (dep.getKey().equals(userDepartments)) {
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

                coursesNameList = new ArrayList<String>();
                coursesIdList = new ArrayList<String>();
                courseIsChecked= new ArrayList<Boolean>();
                int i = 0;

                for (DataSnapshot course : dataSnapshot.getChildren()) {
                    String departementCourse=course.getValue(Course.class).getDepartment();

                    //Controllo per permettere, alla modifica dei corsi, la scelta fra i soli corsi che appartengono ai dipartimento dell'utente.
                    if(userDepartments.contains(departementCourse)){

                        /*Popolamento liste contenenti rispettivamente i nomi e gli id dei corsi presenti nel database
                        * (nome e id di uno stesso corso avranno stessa posizione nelle due diverse liste)*/
                        coursesNameList.add(i, course.getValue(Course.class).getName());
                        coursesIdList.add(i, course.getValue(Course.class).getId());

                        /*Popolamento lista "courseIsChecked" per indicare se il corso nella posizione "i" delle precedenti liste
                        deve essere spuntato o meno nel dialog della modifica dei corsi (viene spuntato se esso fa parte della lista dei corsi dell'utente)*/
                        boolean found = false;
                        for(String userCourses: userCourses){
                            if(found==false) {
                                if (course.getKey().equals(userCourses)) {
                                    courseIsChecked.add(i, true);
                                    found=true;
                                } else {
                                    courseIsChecked.add(i, false);
                                }
                            }
                        }
                        i++;
                    }
                }
                showCourseChooserDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //Mostra il dialog che permette la modifica dei dipartimenti.
    private void showDeparmentChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

        builder.setMultiChoiceItems(departmentsNameList, depIsChecked, new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                depIsChecked[which] = isChecked;
                boolean isDialogEmpty=false;
                //Se non è selezionato alcun dipartimento non viene permessa l'uscita dal dialog (l'utente deve avere almeno un dipartimento)
                if (((AlertDialog) dialog).getListView().getCheckedItemCount() == 0) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    isDialogEmpty = true;
                    Toast.makeText(ProfileActivity.this, R.string.text_message_alert_dialog_department, Toast.LENGTH_SHORT).show();
                }else{
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

                //Controllo per non permettere la rimozione di un dipartimento se l'utente ha solo corsi che appartengono a tale dipartimento.
                if(!isDialogEmpty){
                    for(int i=0; i<depIsChecked.length; i++){
                        if(depIsChecked[i]==false){
                            Department depUnchecked= null;
                            String depNameUnchecked = departmentsNameList[i];
                            for(Department dep : allDepartments){
                                if(dep.getName().equals(depNameUnchecked)){
                                    depUnchecked = dep;
                                    break;
                                }
                            }
                            boolean found = false;
                            for(String userCourse : userCourses){
                                for(Course course : allCourses){
                                    if(course.getId().equals(userCourse)){
                                        if(!course.getDepartment().equals(depUnchecked.getId())){
                                            found=true;
                                        }
                                    }
                                }
                            }
                            if(!found){
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                Toast.makeText(ProfileActivity.this, R.string.text_message_alert_dialog_department_no_remove, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Dopo le modifiche dei dipartimenti viene aggiornata la lista dei dipartimenti dell'utente e la textView che li elenca.
                departmentsAfterChange = new ArrayList<String>();
                textDepartments.setText("");
                for (int i = 0; i < depIsChecked.length; i++) {
                    boolean checked = depIsChecked[i];
                    if (checked) {
                        departmentsAfterChange.add(departmentsIdList[i]);
                        textDepartments.append(departmentsNameList[i] + "\n");
                    }
                }
                userDepartments.clear();
                userDepartments.addAll(departmentsAfterChange);

                /*Nel momento in cui viene eliminato un dipartimento dell'utente,
                 * vengono rimossi automaticamente anche i corsi che gli appartengono dalla lista dei corsi dell'utente*/
                coursesAfterChange = new ArrayList<String>();
                textCourses.setText("");
                for(String userCourse : userCourses){
                    for(Course course : allCourses){
                        if(course.getId().equals(userCourse)){
                            if(userDepartments.contains(course.getDepartment())){
                                textCourses.append(course.getName() + "\n");
                                coursesAfterChange.add(course.getId());
                            }
                        }
                    }
                }
                userCourses.clear();
                userCourses.addAll(coursesAfterChange);
            }
        });

        builder.setTitle(R.string.text_label_dialog_title_departments);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Mostra il dialog che permette la modifica dei corsi
    private void showCourseChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

        //Conversione liste in array, poichè richiesti dal dialog con selezione multipla.
        coursesNameArray = new String[coursesNameList.size()];
        courseIsCheckedArray= new boolean[courseIsChecked.size()];
        coursesIdArray = new String[coursesIdList.size()];

        int i=0;
        for(String id: coursesNameList){
            coursesNameArray[i]=id;
            i++;
        }

        i=0;
        for(String id: coursesIdList){
            coursesIdArray[i]=id;
            i++;
        }

        i=0;
        for(boolean ischecked: courseIsChecked){
            courseIsCheckedArray[i]=ischecked;
            i++;
        }

        builder.setMultiChoiceItems(coursesNameArray, courseIsCheckedArray, new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                courseIsCheckedArray[which]=isChecked;
                //Se non è selezionato alcun corso non viene permessa l'uscita dal dialog (l'utente deve avere almeno un corso)
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

                //Dopo le modifiche dei corsi viene aggiornata la lista dei corsi dell'utente e la textView che li elenca.
                coursesAfterChange = new ArrayList<String>();
                textCourses.setText("");
                for (int i = 0; i < courseIsCheckedArray.length; i++) {
                    boolean checked = courseIsCheckedArray[i];
                    if (checked) {
                        coursesAfterChange.add(coursesIdArray[i]);
                        textCourses.append(coursesNameArray[i] + "\n");
                    }
                }
                userCourses.clear();
                userCourses.addAll(coursesAfterChange);

                /*Nel momento in cui viene eliminato ogni corso di un dipartimento dell'utente,
                * viene rimosso automaticamente anche quest'ultimo dalla lista dei dipartimenti dell'utente*/
                departmentsAfterChange = new ArrayList<String>();
                textDepartments.setText("");
                boolean found;
                for(String userDepartment: userDepartments){
                    found = false;
                    for(String userCourse : userCourses){
                        for(Course course : allCourses) {
                            if (course.getId().equals(userCourse)){
                                String department = course.getDepartment();
                                if(userDepartment.equals(department)){
                                    found=true;
                                }
                            }
                        }
                    }
                    if(found){
                        for(Department d: allDepartments){
                            if(d.getId().equals(userDepartment)){
                                textDepartments.append(d.getName() + "\n");
                                departmentsAfterChange.add(d.getId());
                            }
                        }
                    }
                }
                userDepartments.clear();
                userDepartments.addAll(departmentsAfterChange);
            }
        });

        builder.setTitle(R.string.text_label_dialog_title_courses);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
