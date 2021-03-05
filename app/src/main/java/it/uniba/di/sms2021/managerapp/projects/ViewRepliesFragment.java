package it.uniba.di.sms2021.managerapp.projects;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.lists.RepliesRecyclerAdapter;

public class ViewRepliesFragment extends BottomSheetDialogFragment {
    private static final String TAG = "ViewRepliesFragment" ;
    DatabaseReference replyReviewReference;
    DatabaseReference replyReportReference;
    FirebaseDatabase database;

    RecyclerView recyclerView;

    ProjectReviewsActivity activity;

    RepliesRecyclerAdapter adapter;
    ValueEventListener repliesListener;

    public ViewRepliesFragment() {
        //Costruttore pubblico vuoto richiesto
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_view_replies, container, false);
        recyclerView = itemView.findViewById(R.id.replies_recyclerView);
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDbHelper.getDBInstance();
        replyReviewReference = database.getReference(FirebaseDbHelper.TABLE_REPLIES_REVIEW);
        replyReportReference = database.getReference(FirebaseDbHelper.TABLE_REPLIES_REPORT);

        adapter = new RepliesRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Parcelable origin = this.getArguments().getParcelable("originView");

        if(origin instanceof Review) {
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

}