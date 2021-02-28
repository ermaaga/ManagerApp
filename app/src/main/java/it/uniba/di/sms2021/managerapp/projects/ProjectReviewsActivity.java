package it.uniba.di.sms2021.managerapp.projects;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.ReviewsRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectReviewsActivity extends AbstractBottomNavigationActivity {

    private static final String TAG = "ProjectReviewsActivity";
    RecyclerView recyclerView;

    ReviewsRecyclerAdapter adapter;

    private DatabaseReference reviewsReference;

    ValueEventListener reviewsListener;

    Project project;
    private String idgroup;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_reviews;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.reviews_recyclerView);

        project = getIntent().getParcelableExtra(Project.KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Creo l'adapter che crea gli elementi con i relativi dati.
        adapter = new ReviewsRecyclerAdapter();
        recyclerView.setAdapter(adapter);

        //Ottengo i dati con cui riempire la lista.
        reviewsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REVIEWS);

        reviewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> reviews = new ArrayList<>();
                //Ottengo solo i dati delle recensioni che ri riferiscono al progetto corrente
                idgroup = project.getGroup().getId();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if(child.getValue(Review.class).getGroupId().equals(idgroup)){
                        reviews.add(child.getValue(Review.class));
                    }

                }
                //Ogni volta che le recensioni cambiano, la lista visualizzata cambia.
                adapter.submitList(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reviewsReference.addValueEventListener(reviewsListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        reviewsReference.removeEventListener(reviewsListener);
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
