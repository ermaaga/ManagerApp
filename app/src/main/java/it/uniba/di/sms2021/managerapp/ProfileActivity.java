package it.uniba.di.sms2021.managerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.enitities.Course;
import it.uniba.di.sms2021.managerapp.enitities.Department;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivityTag";

    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference departmentsReference;
    private DatabaseReference coursesReference;

    User user;

    TextView textName;
    TextView textSurname;
    TextView textEmail;
    EditText editName;
    EditText editSurname;
    Button editButton;
    Button saveButton;
    ImageButton editDepartments;
    ImageButton editCourses;
    TextView textDepartments;
    TextView textCourses;

    List<String> departmentsChecked;
    List<String> coursesChecked;
    String[] departmentList;
    String[] courseList;
    String[] departmentListId;
    String[] courseListId;
    boolean[] depIsChecked;
    boolean[] courseIsChecked;
    private ValueEventListener userListener;
    private ValueEventListener departmentsListener;
    private ValueEventListener coursesListener;
    private DatabaseReference currentUserReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textName = (TextView) findViewById(R.id.value_name_account_text_view);
        textSurname = (TextView) findViewById(R.id.value_surname_account_text_view);
        textEmail = (TextView) findViewById(R.id.value_email_account_text_view);
        editName = (EditText) findViewById(R.id.value_name_account_edit_text);
        editSurname = (EditText) findViewById(R.id.value_surname_account_edit_text);
        editButton = (Button) findViewById(R.id.button_edit_profile);
        saveButton = (Button) findViewById(R.id.button_save_profile);
        editDepartments = (ImageButton) findViewById(R.id.departments_button);
        editCourses = (ImageButton) findViewById(R.id.courses_button);
        textDepartments = (TextView) findViewById(R.id.value_department);
        textCourses = (TextView) findViewById(R.id.value_course);


        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        departmentsReference = database.getReference(FirebaseDbHelper.TABLE_DEPARTMENTS);
        coursesReference = database.getReference(FirebaseDbHelper.TABLE_COURSES);

        departmentsChecked = new ArrayList<String>();
        coursesChecked = new ArrayList<String>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserReference = usersReference.child(userid);

        //TODO considerare l'utilizzo di LoginHelper.getCurrentUser()
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user = snapshot.getValue(User.class);

                textName.setText(user.getNome());
                textSurname.setText(user.getCognome());
                textEmail.setText(user.getEmail());

                int sizeDepartments = user.getDipartimenti().size();
                TextView labelDepartments = (TextView) findViewById(R.id.label_departments);
                labelDepartments.setText(getResources().getQuantityString(R.plurals.numberOfDepartments, sizeDepartments));

                int sizeCourses = user.getDipartimenti().size();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        departmentsReference.addValueEventListener(departmentsListener);

        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Iterazione tra i vari elementi appartenenti al nodo "courses"
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

    //metodo usato per modificare le TextView in EditText
    public void editProfile(View view) {
        textName.setVisibility(View.GONE);
        textSurname.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);

        editName.setVisibility(View.VISIBLE);
        editSurname.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        editDepartments.setVisibility(View.VISIBLE);
        editCourses.setVisibility(View.VISIBLE);

        editName.setText(textName.getText());
        editSurname.setText(textSurname.getText());
    }

    public void saveProfile(View view) {
        HashMap childUpdates = new HashMap();
        childUpdates.put("/nome/", editName.getText().toString());
        childUpdates.put("/cognome/", editSurname.getText().toString());

        usersReference.child(user.getAccountId()).updateChildren(childUpdates);

        Intent refresh = new Intent(this, ProfileActivity.class);
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

                    HashMap childUpdates = new HashMap();
                    List<String> currentList = new ArrayList<String>();
                    textDepartments.setText("");
                    for (int i = 0; i < depIsChecked.length; i++) {
                        boolean checked = depIsChecked[i];
                        if (checked) {
                            currentList.add(departmentListId[i]);
                            textDepartments.append(departmentList[i] + "\n");
                        }
                    }
                    departmentsChecked = currentList;

                    childUpdates.put("/dipartimenti/", currentList);
                    usersReference.child(user.getAccountId()).updateChildren(childUpdates);
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

                HashMap childUpdates = new HashMap();
                List<String> currentList = new ArrayList<String>();
                textCourses.setText("");
                for (int i = 0; i < courseIsChecked.length; i++) {
                    boolean checked = courseIsChecked[i];
                    if (checked) {
                        currentList.add(courseListId[i]);
                        textCourses.append(courseList[i] + "\n");
                    }
                }
                coursesChecked = currentList;

                childUpdates.put("/corsi/", currentList);
                usersReference.child(user.getAccountId()).updateChildren(childUpdates);
            }
        });

        builder.setTitle(R.string.text_label_dialog_title_courses);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
