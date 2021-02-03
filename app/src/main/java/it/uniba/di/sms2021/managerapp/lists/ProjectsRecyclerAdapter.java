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
import it.uniba.di.sms2021.managerapp.firebase.Project;

public class ProjectsRecyclerAdapter extends ListAdapter<Project, RecyclerView.ViewHolder>  {
    OnActionListener listener;

    public ProjectsRecyclerAdapter(OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_project, parent, false);
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

        Project project = getItem(position);
        MaterialCardView card = itemView.findViewById(R.id.project_card);
        card.setOnClickListener(view -> listener.onClick(project));

        TextView titleTextView = itemView.findViewById(R.id.project_title_text_view);
        TextView descriptionTextView = itemView.findViewById(R.id.project_exam_text_view);
        titleTextView.setText(String.format("%s - %s", project.getName(), project.getStudyCaseName()));
        descriptionTextView.setText(project.getExamName());
    }

    public interface OnActionListener {
        void onClick (Project project);
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Project> {

        @Override
        public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.equals(newItem);
        }
    }
}
