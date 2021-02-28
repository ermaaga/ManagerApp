package it.uniba.di.sms2021.managerapp.lists;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ReportsRecyclerAdapter extends ListAdapter<Report, RecyclerView.ViewHolder> {
    private static final String TAG = "ReportsRecyclerAdapter";

    //private ReviewsRecyclerAdapter.OnActionListener listener;

    public ReportsRecyclerAdapter() {
        super(new ReportDiffCallback());
        //this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_report, parent, false);

        return new RecyclerView.ViewHolder(view) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;

        TextView userTextView = itemView.findViewById(R.id.user_TextView);
        TextView dateTextView = itemView.findViewById(R.id.date_TextView);
        TextView messageTextView = itemView.findViewById(R.id.message_TextView);

        //TODO se ci sono risposte alla recensione far visualizzare "repliesTextView"
        //TextView repliesTextView = itemView.findViewById(R.id.replies_TextView);
        //TODO fare un intent che porta alla schermata per inviare una risposta
        //Button replyButton = itemView.findViewById(R.id.reply_button);

        Report report = getItem(position);

        if (report != null) {
            setUserName(userTextView, report);
            dateTextView.setText(report.getDate());
            if(report.getComment() == null) {
                messageTextView.setVisibility(View.GONE);
            }else{
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(report.getComment());
            }

        }
    }

    static class ReportDiffCallback extends DiffUtil.ItemCallback<Report> {

        @Override
        public boolean areItemsTheSame(@NonNull Report oldItem, @NonNull Report newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Report oldItem, @NonNull Report newItem) {
            return oldItem.equals(newItem);
        }
    }

    public void setUserName(TextView textView, Report report) {
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    if (report.getUserId().equals(child.getKey())) {
                        User user = child.getValue(User.class);
                        textView.setText(user.getNome());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*public class OnActionListener {
    }*/
}