package it.uniba.di.sms2021.managerapp.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Department;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.lists.DepartmentRecyclerAdapter;

public class DepartmentActivity extends AppCompatActivity {
    private static final String TAG = "DepartmentActivity";

    public static final String USER_DEPARTMENTS = "Department";
    private RecyclerView departmentRecyclerView;
    private DepartmentRecyclerAdapter adapter;

    private FloatingActionButton buttonnext;

    private int userRole;
    private List<String> listdip;
    private DatabaseReference departmentsReference;
    private ValueEventListener departmentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        departmentRecyclerView = findViewById(R.id.departmentRecyclerView);
        buttonnext = (FloatingActionButton) findViewById(R.id.floatingActionButtonNext);

        //viene visualizzato solo se il ruolo è PROFESSOR
        buttonnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listdip = adapter.selectedDepartments();
                nextToDegreeCourses();
            }
        });

        userRole = getIntent().getIntExtra(UserRoleActivity.USER_ROLE, 0);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter = new DepartmentRecyclerAdapter(DepartmentActivity.this, userRole, new DepartmentRecyclerAdapter.OnActionListener() {
            @Override
            public void onSelectionActionProfessor(Boolean isSelected) {
                if(isSelected){
                    buttonnext.setVisibility(View.VISIBLE);
                }else{
                    buttonnext.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSelectionActionStudent(String idDepartment) {
                listdip = new ArrayList<>();
                listdip.add(idDepartment);

                nextToDegreeCourses();
            }
        });

        departmentRecyclerView.setAdapter(adapter);
        departmentRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        departmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        departmentsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_DEPARTMENTS);
        departmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Department> departments = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Department currentDepartment = child.getValue(Department.class);
                    departments.add(currentDepartment);
                }

                adapter.submitList(departments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        departmentsReference.addValueEventListener(departmentsListener);
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        departmentsReference.removeEventListener(departmentsListener);
    }

    public void nextToDegreeCourses(){
    Intent intent = new Intent(DepartmentActivity.this, DegreeCoursesActivity.class);
    intent.putExtra(UserRoleActivity.USER_ROLE, userRole);
    intent.putStringArrayListExtra(USER_DEPARTMENTS, (ArrayList<String>)listdip);
    startActivity(intent);
}

    public void test () {
        DatabaseReference dipartRef = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_DEPARTMENTS);

        for(int i=0; i<10; i++){
            String id = dipartRef.push().getKey();
            dipartRef.child(id).setValue(new Department(id,"dipartiments"+i));
        }
    }

}




