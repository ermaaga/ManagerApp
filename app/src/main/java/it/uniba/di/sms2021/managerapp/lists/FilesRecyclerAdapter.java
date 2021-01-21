package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

public class FilesRecyclerAdapter  extends ListAdapter<ManagerFile, RecyclerView.ViewHolder> {
    private Context context;
    private OnActionListener listener;

    public FilesRecyclerAdapter(Context context, OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_file, parent, false);
        return new RecyclerView.ViewHolder(itemView) {
            @Override
            @NonNull
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;
        ManagerFile file = getItem(position);

        MaterialCardView cardView = itemView.findViewById(R.id.file_card);
        cardView.setOnClickListener(view -> listener.onClick(file));
        ImageView typeImageView = itemView.findViewById(R.id.file_type_image_view);
        TextView nameTextView = itemView.findViewById(R.id.file_name_text_view);
        TextView sizeTextView = itemView.findViewById(R.id.file_size_text_view);

        nameTextView.setText(file.getName());
        sizeTextView.setText(FileUtil.getFormattedSize(context, file.getSize()));
        FileUtil.setTypeImageView(context, typeImageView, file.getType());
    }

    public interface OnActionListener {
        void onClick (ManagerFile file);
    }

    static class DiffCallback extends DiffUtil.ItemCallback<ManagerFile> {
        @Override
        public boolean areItemsTheSame(@NonNull ManagerFile oldItem, @NonNull ManagerFile newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ManagerFile oldItem, @NonNull ManagerFile newItem) {
            return oldItem.equals(newItem);
        }
    }
}
