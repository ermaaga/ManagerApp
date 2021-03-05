package it.uniba.di.sms2021.managerapp.lists;

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
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class RepliesRecyclerAdapter extends ListAdapter<Reply, RecyclerView.ViewHolder>{

    public RepliesRecyclerAdapter() {
        super(new ReplyDiffCallback());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reply, parent, false);

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
        Reply reply = getItem(position);

        TextView userTextView = itemView.findViewById(R.id.user_TextView);
        TextView dateTextView = itemView.findViewById(R.id.date_TextView);
        TextView messageTextView = itemView.findViewById(R.id.message_TextView);


        setUserName(userTextView, reply);
        dateTextView.setText(reply.getDate());
        messageTextView.setText(reply.getComment());


    }

    static class ReplyDiffCallback extends DiffUtil.ItemCallback<Reply> {

        @Override
        public boolean areItemsTheSame(@NonNull Reply oldItem, @NonNull Reply newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reply oldItem, @NonNull Reply newItem) {
            return oldItem.equals(newItem);
        }
    }

    private void setUserName(TextView textView, Reply reply) {
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    if (reply.getUserId().equals(child.getKey())) {
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

}