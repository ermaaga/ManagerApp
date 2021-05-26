package it.uniba.di.sms2021.managerapp.exams;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.ProfileActivity;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.lists.UserRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamsPartecipantsActivity extends AbstractBottomNavigationActivity {

    private RecyclerView partecipantsRecyclerView;
    private UserRecyclerAdapter adapter;

    private Exam exam;
    private DatabaseReference userReference;
    private ValueEventListener partecipantsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        partecipantsRecyclerView = findViewById(R.id.partecipants_recycler_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        exam = getIntent().getParcelableExtra(Exam.Keys.EXAM);

        adapter = new UserRecyclerAdapter(new UserRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked(User string) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra(User.KEY, string);
                boolean fromLink = true;
                intent.putExtra("fromLinkBoolean", fromLink);
                startActivity(intent);

            }
        });
        partecipantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        partecipantsRecyclerView.setAdapter(adapter);

        userReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        partecipantsListener = new ValueEventListener() {
            List<User> partecipants = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (exam.getStudents().contains(child.getKey())) {
                        User user = child.getValue(User.class);
                        partecipants.add(user);
                    }
                }
                adapter.submitList(partecipants);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userReference.addValueEventListener(partecipantsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        userReference.removeEventListener(partecipantsListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_exam_partecipants;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
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
}
