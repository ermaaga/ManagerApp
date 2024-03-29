package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
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
        adapter = new ReviewsRecyclerAdapter(new ReviewsRecyclerAdapter.OnActionListener(){
            @Override
            public void onReply(Review review) {
                ReplyFragment bottomSheetFragment = new ReplyFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("originReply", review);
                bottomSheetFragment.setArguments(bundle);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }

            @Override
            public void onClick(Review review,
                                int pos,
                                MaterialCardView containerCard,
                                TextView userTextView,
                                TextView dateTextView,
                                TextView messageTextView,
                                RatingBar ratingBar) {
                //Creato intent per inviare l'oggetto della segnalazione all'activity delle risposte corrispondenti
                Intent intent = new Intent(getApplicationContext(), OpinionRepliesActivity.class);
                intent.putExtra("originObject", review);

                //Impostata l'animazione dell'anteprima della segnalazione
                Pair<View, String> p1 = Pair.create(containerCard, "containerTN");
                Pair<View, String> p2 = Pair.create(userTextView, "userTN");
                Pair<View, String> p3 = Pair.create(dateTextView, "dateTN");
                Pair<View, String> p4 = Pair.create(messageTextView, "messageTN");
                Pair<View, String> p5 = Pair.create(ratingBar, "ratingTN");

                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProjectReviewsActivity.this, p1, p2, p3, p4, p5);

                startActivity(intent, optionsCompat.toBundle());
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Ottengo i dati con cui riempire la lista.
        reviewsReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REVIEWS);

        reviewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> reviews = new ArrayList<>();
                //Ottengo solo i dati delle recensioni che ri riferiscono al progetto corrente
                idgroup = project.getGroup().getId();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Review review =child.getValue(Review.class);
                    if(review.getGroupId().equals(idgroup)){
                        reviews.add(review);
                    }

                }
                //Ogni volta che le recensioni cambiano, la lista visualizzata cambia.
                adapter.submitList(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reviewsReference.addListenerForSingleValueEvent(reviewsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
