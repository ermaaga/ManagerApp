package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
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
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ReviewsRecyclerAdapter extends ListAdapter<Review, RecyclerView.ViewHolder> {

    //private ReviewsRecyclerAdapter.OnActionListener listener;

    public ReviewsRecyclerAdapter() {
        super(new ReviewDiffCallback());
        //this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_review, parent, false);

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

        RatingBar userRatingBar = itemView.findViewById(R.id.user_rating_stars);

        TextView userTextView = itemView.findViewById(R.id.user_TextView);
        TextView dateTextView = itemView.findViewById(R.id.date_TextView);
        TextView messageTextView = itemView.findViewById(R.id.message_TextView);

        //TODO se ci sono risposte alla recensione far visualizzare "repliesTextView"
        //TextView repliesTextView = itemView.findViewById(R.id.replies_TextView);
        //TODO fare un intent che porta alla schermata per inviare una risposta
        //Button replyButton = itemView.findViewById(R.id.reply_button);

        Review review = getItem(position);

        if (review != null) {
            setUserName(userTextView, review);
            userRatingBar.setRating(review.getRating());
            dateTextView.setText(review.getDate());
            if(review.getComment() == null) {
                messageTextView.setVisibility(View.GONE);
            }else{
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(review.getComment());
            }

        }
    }

    static class ReviewDiffCallback extends DiffUtil.ItemCallback<Review> {

        @Override
        public boolean areItemsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return oldItem.equals(newItem);
        }
    }

    public void setUserName(TextView textView, Review review) {
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    if (review.getUserId().equals(child.getKey())) {
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
