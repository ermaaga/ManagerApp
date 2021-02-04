package it.uniba.di.sms2021.managerapp.exams;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.lists.StringRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamsPartecipantsActivity extends AbstractBottomNavigationActivity {

    private RecyclerView partecipantsRecyclerView;
    private StringRecyclerAdapter adapter;

    private Exam exam;

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

        adapter = new StringRecyclerAdapter(new StringRecyclerAdapter.OnActionListener() {
            @Override
            public void onItemClicked(String string) {
                //Do nothing
            }
        });
        partecipantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        partecipantsRecyclerView.setAdapter(adapter);

        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS)
                .addValueEventListener(new ValueEventListener() {
                    List<String> partecipants = new ArrayList<>();
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child: snapshot.getChildren()) {
                            if (exam.getStudents().contains(child.getKey())) {
                                User user = child.getValue(User.class);
                                partecipants.add(user.getFullName());
                            }
                        }
                        adapter.submitList(partecipants);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
