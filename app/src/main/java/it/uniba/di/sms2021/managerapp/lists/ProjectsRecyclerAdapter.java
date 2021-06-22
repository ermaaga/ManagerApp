package it.uniba.di.sms2021.managerapp.lists;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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

import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.Project;

public class ProjectsRecyclerAdapter extends ListAdapter<Project, RecyclerView.ViewHolder>  {
    OnActionListener listener;

    private ProjectsRecyclerViewManager recyclerViewManager;

    public ProjectsRecyclerAdapter(OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    public void setRecyclerViewManager(ProjectsRecyclerViewManager recyclerViewManager) {
        this.recyclerViewManager = recyclerViewManager;
    }

    @Override
    public void submitList(@Nullable List<Project> list) {
        super.submitList(list);

        if (recyclerViewManager != null) {
            if (list == null) {
                recyclerViewManager.setProjectsViewHasData(false);
            } else {
                recyclerViewManager.setProjectsViewHasData(!list.isEmpty());
            }
        }
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
        TextView voteTextView = itemView.findViewById(R.id.project_text_vote);

        titleTextView.setText(String.format("%s - %s", project.getName(), project.getStudyCaseName()));
        descriptionTextView.setText(project.getExamName());

        /*Viene visualizzata la valutazione del progetto nel caso in cui esso abbia una valutazione
          e l'utente corrente ne faccia parte*/
        if(project.getEvaluation()!=null && project.isMember()){
            voteTextView.setText("" + project.getEvaluation().getVote());

            SpannableString content = new SpannableString("" + project.getEvaluation().getVote());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            voteTextView.setText(content);
            voteTextView.setVisibility(View.VISIBLE);
        }
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
