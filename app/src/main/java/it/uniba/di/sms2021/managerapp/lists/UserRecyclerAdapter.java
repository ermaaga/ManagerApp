package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;

public class UserRecyclerAdapter extends ListAdapter<User, RecyclerView.ViewHolder> {
    private OnActionListener listener;

    public UserRecyclerAdapter(OnActionListener listener) {
        super(new UserDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user,
                parent, false);

        //TODO vedere Nota: volendo si può creare una classe ViewHolder a parte.
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
        User user = getItem(position);

        MaterialCardView cardView = itemView.findViewById(R.id.user_item_card);
        cardView.setOnClickListener(view -> listener.onItemClicked(user));

        TextView fullNameTextView = itemView.findViewById(R.id.fullname_TextView);
        TextView emailTextView = itemView.findViewById(R.id.email_TextView);

        fullNameTextView.setText(user.getFullName());
        emailTextView.setText(user.getEmail());
    }

    static class UserDiffCallback extends DiffUtil.ItemCallback<User> {

        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnActionListener {
        void onItemClicked (User string);
    }
}
