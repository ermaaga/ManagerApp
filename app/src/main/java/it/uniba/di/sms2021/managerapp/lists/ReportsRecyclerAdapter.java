package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ReportsRecyclerAdapter extends ListAdapter<Report, ReportsRecyclerAdapter.ViewHolder> {
    private static final String TAG = "ReportsRecyclerAdapter";

    OnActionListener listener;

    Report report;

    public ReportsRecyclerAdapter(OnActionListener listener) {
        super(new ReportDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_report, parent, false);
        List<Report> reports = new ArrayList<>();
        reports = getCurrentList();
        return new ViewHolder(view, reports, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View itemView = holder.itemView;
        report = getItem(position);
        TextView userTextView = itemView.findViewById(R.id.user_TextView);
        TextView dateTextView = itemView.findViewById(R.id.date_TextView);
        TextView messageTextView = itemView.findViewById(R.id.message_TextView);

        setUserName(userTextView, report);
        dateTextView.setText(report.getDate());

        if(!report.getComment().equals("")) {
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText(report.getComment());
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView repliesTextView;
        Button replyButton;
        public ViewHolder(View itemView, List<Report> list, ReportsRecyclerAdapter.OnActionListener listener) {
            super(itemView);
            replyButton = itemView.findViewById(R.id.reply_button);
            replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onReply(list.get(getAdapterPosition()));
                }
            });
            repliesTextView = itemView.findViewById(R.id.replies_TextView);
            repliesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(list.get(getAdapterPosition()));
                }
            });
            setViewReplies(repliesTextView, list);
        }

        private void setViewReplies(TextView repliesTextView, List<Report> list){
            DatabaseReference reportRepliesReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REPLIES_REPORT);
            reportRepliesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child: snapshot.getChildren()) {
                        Reply reply= child.getValue(Reply.class);
                        if (reply.getOriginId().equals(list.get(getAdapterPosition()).getReportId())) {
                            repliesTextView.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
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

    public interface OnActionListener {
        void onReply(Report report);
        void onClick(Report report);
    }
}