package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

public class StudyCasesRecyclerAdapter extends ListAdapter<StudyCase, RecyclerView.ViewHolder>  {
    OnActionListener listener;

    public StudyCasesRecyclerAdapter(OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_study_case, parent, false);
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

        StudyCase studyCase = getItem(position);
        MaterialCardView card = itemView.findViewById(R.id.study_case_card);
        card.setOnClickListener(view -> listener.onClick(studyCase));

        TextView titleTextView = itemView.findViewById(R.id.study_case_title_text_view);
        TextView descriptionTextView = itemView.findViewById(R.id.study_case_description_text_view);
        titleTextView.setText(studyCase.getNome());
        descriptionTextView.setText(studyCase.getDescrizione());

        ImageView infoImageView = itemView.findViewById(R.id.file_type_image_view);
        infoImageView.setOnClickListener(view -> listener.onInfo(studyCase));
    }

    public interface OnActionListener {
        void onClick (StudyCase studyCase);
        void onInfo (StudyCase studyCase);
    }

    static class DiffCallback extends DiffUtil.ItemCallback<StudyCase> {

        @Override
        public boolean areItemsTheSame(@NonNull StudyCase oldItem, @NonNull StudyCase newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull StudyCase oldItem, @NonNull StudyCase newItem) {
            return oldItem.equals(newItem);
        }
    }
}
