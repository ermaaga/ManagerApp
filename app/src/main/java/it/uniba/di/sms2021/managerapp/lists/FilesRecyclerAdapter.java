package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MenuRes;
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
        ImageView actionsImageView = itemView.findViewById(R.id.file_action_overflow_image_view);

        nameTextView.setText(file.getName());
        sizeTextView.setText(FileUtil.getFormattedSize(context, file.getSize()));
        FileUtil.setTypeImageView(context, typeImageView, file.getType());
        actionsImageView.setOnClickListener(view -> showMenu(view, file));
    }

    public interface OnActionListener {
        void onClick (ManagerFile file);
        void onDelete (ManagerFile file);
        void onShare (ManagerFile file);
        void onDownload (ManagerFile file);
    }

    /**
     * Mostra un menù popup a partire dal bottone premuto.
     * Preso e modificato da: https://material.io/components/menus/android#dropdown-menus
     */
    private void showMenu(View view, ManagerFile file) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.file_action_popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.file_delete_action) {
                    listener.onDelete(file);
                } else if (item.getItemId() == R.id.file_share_action) {
                    listener.onShare(file);
                } else if (item.getItemId() == R.id.file_download_action) {
                    listener.onDownload(file);
                } else {
                    throw new RuntimeException("Id Menù " + item.getItemId() + " non trovato");
                }
                return false;
            }
        });
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                // Respond to popup being dismissed.
            }
        });
        // Show the popup menu.
        popup.show();
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
