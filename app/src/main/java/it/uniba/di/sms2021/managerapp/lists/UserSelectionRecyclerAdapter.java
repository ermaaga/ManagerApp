package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;

public class UserSelectionRecyclerAdapter extends ListAdapter<User, RecyclerView.ViewHolder> {
    private final Context context;
    private final OnActionListener listener;
    private final Set<Integer> itemsSelected;

    @Nullable
    private Set<String> initiallySelectedUsersIds;

    public UserSelectionRecyclerAdapter(Context context, OnActionListener listener) {
        super(new UserDiffCallback());
        this.context = context;
        this.listener = listener;
        itemsSelected = new HashSet<>();
    }

    public UserSelectionRecyclerAdapter(Context context, OnActionListener listener,
                                        Collection<String> initiallySelectedUsersIds) {
        this (context, listener);
        this.initiallySelectedUsersIds = new HashSet<>(initiallySelectedUsersIds);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user,
                parent, false);

        //Nota: volendo si può creare una classe ViewHolder a parte.
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
        // Se ci sono elementi che devono essere selezionati alla prima visualizzazione, li seleziona
        if (initiallySelectedUsersIds != null && initiallySelectedUsersIds.contains(user.getAccountId())) {
            cardView.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
            itemsSelected.add(position);
        }
        // Logica di selezione al tocco
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemsSelected.contains(position)) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white, context.getTheme()));
                    itemsSelected.remove(position);
                } else {
                    cardView.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
                    itemsSelected.add(position);
                }

                listener.onItemClicked();
            }
        });

        TextView fullNameTextView = itemView.findViewById(R.id.fullname_TextView);
        TextView emailTextView = itemView.findViewById(R.id.email_TextView);

        fullNameTextView.setText(user.getFullName());
        emailTextView.setText(user.getEmail());
    }

    public List<User> getSelectedItems () {
        List<User> usersSelected = new ArrayList<>();
        for (int position: itemsSelected) {
            usersSelected.add(getItem(position));
        }

        return usersSelected;
    }

    public int getSelectedItemsCount () {
        return itemsSelected.size();
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
        void onItemClicked ();
    }
}
