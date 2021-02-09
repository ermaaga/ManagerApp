package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.lists.UserSelectionRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class NewGroupActivity extends AbstractFormActivity {
    private static final String TAG = "NewGroupActivity";
    private FirebaseDatabase database;
    private DatabaseReference groupsRef;
    private Exam exam;

    private EditText name;
    private Button button;

    private RecyclerView userRecyclerView;
    private UserSelectionRecyclerAdapter adapter;
    private DatabaseReference userReference;
    private ValueEventListener examMembersListener;

    @Override
    protected int getLayoutId() { return R.layout.activity_new_group; }
    @Override
    protected int getBottomNavigationMenuItemId() { return R.id.nav_exams; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = findViewById(R.id.name_edit_text);
        button = findViewById(R.id.button_create_new_group);
        userRecyclerView = findViewById(R.id.group_members_recycler_view);

        database = FirebaseDbHelper.getDBInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        exam = getIntent().getParcelableExtra(Exam.Keys.EXAM);
        adapter = new UserSelectionRecyclerAdapter(this, new UserSelectionRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked() {
                //Do nothing
            }
        });
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(adapter);

        userReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        examMembersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> students = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    if (exam.getStudents().contains(child.getKey())) {
                        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (!child.getKey().equals(currentUser)) {
                            students.add(child.getValue(User.class));
                        }
                    }
                }
                adapter.submitList(students);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userReference.addValueEventListener(examMembersListener);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(name.getText().toString())) {
                    Intent intent = getIntent();

                    groupsRef = database.getReference(FirebaseDbHelper.TABLE_GROUPS);

                    DatabaseReference newElement = groupsRef.push();

                    List<String> membri = new ArrayList<>();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    membri.add(userId);
                    for (User student: adapter.getSelectedItems()) {
                        if (!student.getAccountId().equals(userId)) {
                            membri.add(student.getAccountId());
                        }
                    }

                    Group group = new Group(newElement.getKey(), name.getText().toString(), intent.getStringExtra(StudyCase.Keys.ID),
                            exam.getId(), membri);
                    newElement.setValue(group);

                    Toast.makeText(getApplicationContext(), R.string.text_message_group_created , Toast.LENGTH_SHORT).show();
                    NewGroupActivity.super.onBackPressed();
                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        userReference.removeEventListener(examMembersListener);
    }
}