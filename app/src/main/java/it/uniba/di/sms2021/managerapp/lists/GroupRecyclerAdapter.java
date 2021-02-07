package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;

public class GroupRecyclerAdapter extends ListAdapter<Group, RecyclerView.ViewHolder> {

    OnActionListener listener;

    public GroupRecyclerAdapter(OnActionListener listners) {
        super(new DiffCallback());
        this.listener =listners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_exam_groups, parent, false);
        return new RecyclerView.ViewHolder(itemView) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;
        Group group = getItem(position);
        MaterialCardView card = itemView.findViewById(R.id.exam_group_card);
        card.setOnClickListener(view -> listener.onClick(group));

        TextView titleTextViewGroupName = itemView.findViewById(R.id.exam_group_title_text_view);
        TextView groupStudyCaseTextView = itemView.findViewById(R.id.exam_group_study_case_text_view);
        titleTextViewGroupName.setText(group.getName());
        groupStudyCaseTextView.setText(group.getStudyCase());


    }

    public interface OnActionListener {
        void onClick (Group group);
        void onInfo (Group group);
    }



    static class DiffCallback extends DiffUtil.ItemCallback<Group> {

        @Override
        public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.equals(newItem);
        }
    }
}