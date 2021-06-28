package it.uniba.di.sms2021.managerapp.exams;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.Application;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.enitities.notifications.ExamJoinRequest;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.lists.ExamsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.notifications.NotificationChecker;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;


public class ExamsActivity extends AbstractBottomNavigationActivity {

    RecyclerView recyclerView;
    ExamsRecyclerAdapter adapter;

    FloatingActionButton btn_CreateNewExam;

    private DatabaseReference examsReference;
    private ValueEventListener examsListener;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carica dati dummy solo se Application.LOAD_DUMMY_DATA è true
        Application application = (Application) getApplication();
        if (application.shouldLoadData()) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (application.shouldUploadFiles() && permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            } else {
                loadDummyData(application);
            }
        }

        // initialize components

        initialize();

        recyclerView = findViewById(R.id.exams_recyclerView);


        btn_CreateNewExam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ExamsActivity.this,NewExamActivity.class));
            }
        });

        NotificationChecker.subscribeCheckForNotifications(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //test(); Usato per valori dummy

        //Creo l'adapter che crea gli elementi con i relativi dati.
        adapter = new ExamsRecyclerAdapter(new ExamsRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked(Exam exam) {
                chooseExam(exam);
            }
        });
        recyclerView.setAdapter(adapter);
        //Faccio sì che la lista venga visualizzata come una griglia su due colonne o 3 colonne
        //in base all'orientamento del dispositivo
        int spanCount;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            spanCount = 2;
        } else {
            spanCount = 3;
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        //Ottengo i dati con cui riempire la lista.
        examsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_EXAMS);
        examsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Exam> exams = new ArrayList<>();

                for (DataSnapshot child: snapshot.getChildren()) {
                    Exam exam = child.getValue(Exam.class);
                    if (LoginHelper.getCurrentUser().getCorsi().contains(exam.getDegreeCourse())) {
                        exams.add(exam);
                    }
                }

                adapter.submitList(exams);  //Ogni volta che gli esami cambiano, la lista
                //visualizzata cambia.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        examsReference.addValueEventListener(examsListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        examsReference.removeEventListener(examsListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_exams;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
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

    private void chooseExam (Exam exam) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Se l'utente è uno studente che partecipa già all'esame, o è il professore dell'esame,
        // lo fa entrare. Altrimenti manda una richiesta per partecipare.
        if ((exam.getStudents() != null && exam.getStudents().contains(uid)) ||
                exam.getProfessors().contains(uid)) {
            Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
            intent.putExtra(Exam.Keys.EXAM, exam);
            startActivity(intent);
        } else {
            // Se l'utente ha già richiesto di partecipare all'esame, viene avvisato di ciò.
            User currentUser = LoginHelper.getCurrentUser();
            FirebaseDbHelper.getPendingExamRequests(currentUser.getAccountId())
                    .child(exam.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(ExamsActivity.this,
                                R.string.text_message_exam_request_already_sent,
                                Toast.LENGTH_LONG).show();
                    } else {
                        displayJoinRequestDialog(exam);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    // Mostra una finestra di dialogo che informa l'utente di ciò che sta per fare e chiede se vuole
    // procedere
    private void displayJoinRequestDialog (Exam exam) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.text_message_join_exam)
                .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendJoinRequest(exam);
                    }
                }).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    //Crea una notifica con lo stesso id per ogni professore dell'esame
    private void sendJoinRequest (Exam exam) {
        String notificationId = null;
        User currentUser = LoginHelper.getCurrentUser();

        for (String professorId: exam.getProfessors()) {
            DatabaseReference reference;
            if (notificationId == null) {
                reference = FirebaseDbHelper
                        .getExamJoinRequestReference(professorId).push();
                notificationId = reference.getKey();
            } else {
                reference = FirebaseDbHelper
                        .getExamJoinRequestReference(professorId).child(notificationId);
            }

            // Invia la richiesta al professore

            ExamJoinRequest request = new ExamJoinRequest(notificationId,
                    currentUser.getAccountId(),
                    currentUser.getFullName(),
                    exam,
                    System.currentTimeMillis());
            reference.setValue(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ExamsActivity.this,
                            R.string.text_message_exam_request_sent, Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ExamsActivity.this,
                            R.string.text_message_exam_request_failed, Toast.LENGTH_LONG).show();
                    // Se fallisce l'invio ad un professore rimuove la notifica da tutti i professori
                    // e dalle richieste in sospeso
                    request.removeNotification();
                }
            });
        }

        // Aggiunge la richiesta in sospeso, così che l'utente non possa chiedere nuovamente l'accesso
        FirebaseDbHelper.getPendingExamRequests(currentUser.getAccountId()).child(exam.getId()).setValue(true);
    }

    private void initialize() {
        btn_CreateNewExam = findViewById(R.id.exam_add_floating_action_button);
        if (LoginHelper.getCurrentUser().getRuolo() != User.ROLE_PROFESSOR) {
            btn_CreateNewExam.setVisibility(View.GONE);
        }
    }

    private void loadDummyData(Application application) {
        application.getDataLoader().loadData(application);
        application.stopLoadingData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDummyData((Application) getApplication());
            }
        }
    }
}