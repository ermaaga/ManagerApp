package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.projects.OpinionComparator;

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

        setUserName(holder.userTextView, report);
        holder.dateTextView.setText(report.getDate());

        if(!report.getComment().equals("")) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(report.getComment());
        }

    }

    /**
     * Prima di mostrare la lista viene ordinata per data di invio
     */
    @Override
    public void submitList(@Nullable List<Report> list) {
        Collections.sort(list, new OpinionComparator());

        super.submitList(list);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView repliesTextView;
        Button replyButton;
        MaterialCardView containerCard;
        TextView userTextView;
        TextView dateTextView;
        TextView messageTextView;

        public ViewHolder(View itemView, List<Report> list, ReportsRecyclerAdapter.OnActionListener listener) {
            super(itemView);
            containerCard = itemView.findViewById(R.id.report_item_card);
            userTextView = itemView.findViewById(R.id.user_TextView);
            dateTextView = itemView.findViewById(R.id.date_TextView);
            messageTextView = itemView.findViewById(R.id.message_TextView);

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
                    listener.onClick(list.get(getAdapterPosition()),
                            getAdapterPosition(),
                            containerCard,
                            userTextView,
                            dateTextView,
                            messageTextView);
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
        void onClick(Report report,
                     int pos,
                     MaterialCardView containerCard,
                     TextView userTextView,
                     TextView dateTextView,
                     TextView messageTextView);
    }
}