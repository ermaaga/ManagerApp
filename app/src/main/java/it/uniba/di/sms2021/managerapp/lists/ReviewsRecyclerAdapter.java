package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
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
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.projects.OpinionComparator;

public class ReviewsRecyclerAdapter extends ListAdapter<Review, ReviewsRecyclerAdapter.ViewHolder>{
    private static final String TAG= "ReviewsRecyclerAdapter";
    OnActionListener listener;

    Review review;

    public ReviewsRecyclerAdapter(OnActionListener listener) {
        super(new ReviewDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_review, parent, false);
        List<Review> reviews = new ArrayList<>();
        reviews = getCurrentList();
        return new ViewHolder(view, reviews, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        View itemView = holder.itemView;
        review = getItem(position);

        setUserName(holder.userTextView, review);
        holder.ratingBar.setRating(review.getRating());
        holder.dateTextView.setText(review.getDate());

        if(!review.getComment().equals("")) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(review.getComment());
        }

    }

    /**
     * Prima di mostrare la lista viene ordinata per data di invio
     */
    @Override
    public void submitList(@Nullable List<Review> list) {
        Collections.sort(list, new OpinionComparator());

        super.submitList(list);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView repliesButton;
        Button replyButton;
        TextView userTextView;
        TextView dateTextView;
        TextView messageTextView;
        MaterialCardView containerCard;
        RatingBar ratingBar;

        public ViewHolder(View itemView, List<Review> list, OnActionListener listener) {
            super(itemView);

            userTextView = itemView.findViewById(R.id.user_TextView);
            dateTextView = itemView.findViewById(R.id.date_TextView);
            messageTextView= itemView.findViewById(R.id.message_TextView);
            replyButton = itemView.findViewById(R.id.reply_button);
            containerCard = itemView.findViewById(R.id.review_item_card);
            ratingBar = itemView.findViewById(R.id.user_rating_stars);

            replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onReply(list.get(getAdapterPosition()));
                }
            });

            repliesButton= itemView.findViewById(R.id.review_replies_button);

            repliesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(list.get(getAdapterPosition()),
                            getAdapterPosition(),
                            containerCard,
                            userTextView,
                            dateTextView,
                            messageTextView,
                            ratingBar);
                }
            });

            setViewReplies(repliesButton, list);
        }

        private void setViewReplies(TextView repliesTextView, List<Review> list){
            DatabaseReference reviewRepliesReference = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_REPLIES_REVIEW);
            reviewRepliesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child: snapshot.getChildren()) {
                        Reply reply= child.getValue(Reply.class);
                        if (reply.getOriginId().equals(list.get(getAdapterPosition()).getReviewId())) {
                            repliesTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
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

    private void setUserName(TextView textView, Review review) {
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

    public interface OnActionListener {
        void onReply(Review review);
        void onClick(Review review,
                     int pos,
                     MaterialCardView containerCard,
                     TextView userTextView,
                     TextView dateTextView,
                     TextView messageTextView,
                     RatingBar ratingBar);
    }
}
