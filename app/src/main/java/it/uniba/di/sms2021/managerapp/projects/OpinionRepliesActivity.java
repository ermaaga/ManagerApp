package it.uniba.di.sms2021.managerapp.projects;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Opinion;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.lists.RepliesRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class OpinionRepliesActivity extends AbstractBottomNavigationActivity {
    private static DatabaseReference replyReportReference;
    private static DatabaseReference replyReviewReference;
    private static FirebaseDatabase database;

    private static RepliesRecyclerAdapter adapter;
    private static ValueEventListener repliesListener;

    RecyclerView recyclerView;
    TextView userTextView;
    TextView dateTextView;
    TextView messageTextView;
    RatingBar ratingBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_view_opinion_replies;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.replies_recyclerView);
        userTextView = findViewById(R.id.user_TextView);
        dateTextView = findViewById(R.id.date_TextView);
        messageTextView = findViewById(R.id.message_TextView );
        ratingBar = findViewById(R.id.user_rating_stars );
    }

    @Override
    protected void onStart() {
        super.onStart();

        database = FirebaseDbHelper.getDBInstance();
        replyReportReference = database.getReference(FirebaseDbHelper.TABLE_REPLIES_REPORT);
        replyReviewReference = database.getReference(FirebaseDbHelper.TABLE_REPLIES_REVIEW);

        adapter = new RepliesRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Parcelable origin = getIntent().getParcelableExtra("originObject");

        if(origin instanceof Review) {
            /*Nel caso si stiano visualizzando risposte relative a una recensione,
            si inizializzano i dati della recensione selezionata e delle sue risposte*/

            ratingBar.setVisibility(View.VISIBLE);
            setUserName(userTextView, (Review)origin);
            dateTextView.setText(((Review)origin).getDateOpinion());
            messageTextView.setText(((Review)origin).getComment());
            ratingBar.setRating(((Review)origin).getRating());

            repliesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Reply> replies = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Reply reply = child.getValue(Reply.class);
                        //Ottengo solo le risposte alla recensione o segnalazione corrente
                        if(reply.getOriginId().equals(((Review) origin).getReviewId())){
                            replies.add(reply);
                        }

                    }
                    //Ogni volta che le risposte cambiano, la lista visualizzata cambia.
                    adapter.submitList(replies);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            replyReviewReference.addListenerForSingleValueEvent(repliesListener);

        }else if(origin instanceof Report){
            /*Nel caso si stiano visualizzando risposte relative a una segnalazione,
             si inizializzano i dati della segnalazione selezionata e delle sue risposte*/

            setUserName(userTextView, (Report)origin);
            dateTextView.setText(((Report)origin).getDateOpinion());
            messageTextView.setText(((Report)origin).getComment());

            repliesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Reply> replies = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Reply reply = child.getValue(Reply.class);
                        //Ottengo solo le risposte alla recensione o segnalazione corrente
                        if(reply.getOriginId().equals(((Report) origin).getReportId())){
                            replies.add(reply);
                        }

                    }
                    //Ogni volta che le risposte cambiano, la lista visualizzata cambia.
                    adapter.submitList(replies);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            replyReportReference.addListenerForSingleValueEvent(repliesListener);
        }
    }

    public void setUserName(TextView textView, Opinion origin) {
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    if(origin instanceof Review) {
                        if (((Review)origin).getUserId().equals(child.getKey())) {
                            User user = child.getValue(User.class);
                            textView.setText(user.getNome());
                            break;
                        }
                    }else if(origin instanceof Report){
                        if (((Report)origin).getUserId().equals(child.getKey())) {
                            User user = child.getValue(User.class);
                            textView.setText(user.getNome());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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