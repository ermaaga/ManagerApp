package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ReportsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectReportsActivity extends AbstractBottomNavigationActivity {

    private static final String TAG = "ProjectReportsActivity";
    RecyclerView recyclerView;

    ReportsRecyclerAdapter adapter;

    private DatabaseReference reportsReference;

    ValueEventListener reportsListener;

    Project project;
    private String idgroup;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_reports;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.reports_recyclerView);

        project = getIntent().getParcelableExtra(Project.KEY);
        boolean needReply = getIntent().getBooleanExtra(Reply.KEY, false);
        Report replyReport = getIntent().getParcelableExtra(Report.KEY);

        if(needReply){
            //TODO vedere se funziona
            Intent intent = new Intent(getApplicationContext(), OpinionRepliesActivity.class);
            intent.putExtra("originObject", replyReport);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Creo l'adapter che crea gli elementi con i relativi dati.
        adapter = new ReportsRecyclerAdapter(new ReportsRecyclerAdapter.OnActionListener(){
            @Override
            public void onReply(Report report) {
                ReplyFragment bottomSheetFragment = new ReplyFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("originReply", report);
                bundle.putParcelable("project", project);
                bottomSheetFragment.setArguments(bundle);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }

            @Override
            public void onClick(Report report,
                    int pos,
                    MaterialCardView containerCard,
                    TextView userTextView,
                    TextView dateTextView,
                    TextView messageTextView) {
                //Creato intent per inviare l'oggetto della segnalazione all'activity delle risposte corrispondenti
                Intent intent = new Intent(getApplicationContext(), OpinionRepliesActivity.class);
                intent.putExtra("originObject", report);

                //Impostata l'animazione dell'anteprima della segnalazione
                Pair<View, String> p1 = Pair.create(containerCard, "containerTN");
                Pair<View, String> p2 = Pair.create(userTextView, "userTN");
                Pair<View, String> p3 = Pair.create(dateTextView, "dateTN");
                Pair<View, String> p4 = Pair.create(messageTextView, "messageTN");

                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProjectReportsActivity.this, p1, p2, p3, p4);

                startActivity(intent, optionsCompat.toBundle());
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Ottengo i dati con cui riempire la lista.
        reportsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REPORTS).child("reportsList");

        reportsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    //Ottengo solo i dati delle recensioni che ri riferiscono al progetto corrente
                    idgroup = project.getGroup().getId();

                    if(child.getValue(Report.class).getGroupId().equals(idgroup)){
                        reports.add(child.getValue(Report.class));
                    }
                }

                //Ogni volta che le recensioni cambiano, la lista visualizzata cambia.
                adapter.submitList(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reportsReference.addValueEventListener(reportsListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        reportsReference.removeEventListener(reportsListener);
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

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            fragmentManager.popBackStack();
        }

        return super.onSupportNavigateUp();
    }
}