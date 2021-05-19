package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.projects.FileComparator;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

/**
 * Adapter di una recyclerView per una lista di file
 */
public class FilesRecyclerAdapter  extends ListAdapter<ManagerFile, RecyclerView.ViewHolder> {
    private final Context context;
    private final OnActionListener listener;
    private final Project project;

    /**
     * Crea una adapter a partire dal contesto, dal progetto attuale e usando le azioni passate
     * nel listener.
     */
    public FilesRecyclerAdapter(Context context, Project project, OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
        this.context = context;
        this.project = project;
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
        TextView releaseTextView = itemView.findViewById(R.id.file_release_text_view);

        nameTextView.setText(file.getName());
        sizeTextView.setText(FileUtil.getFormattedSize(context, file.getSize()));
        FileUtil.setTypeImageView(context, typeImageView, file.getType());
        actionsImageView.setOnClickListener(view -> showMenu(view, file));

        int release = project.getReleaseNumber(file.getName());
        if (release != 0) {
            releaseTextView.setText(context.getString(R.string.text_label_file_release_number,
                    release));
            releaseTextView.setVisibility(View.VISIBLE);
        } else {
            releaseTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void submitList(@Nullable List<ManagerFile> list) {
        if (list != null) {
            Collections.sort(list, new FileComparator(project.getCurrentReleaseName()));
        }
        super.submitList(list);
    }

    /**
     * Presenta le azioni effettuabile su un singolo file della lista
     */
    public interface OnActionListener {
        void onClick (ManagerFile file);

        /**
         * Setta il file come release del progetto o lo rimuove dalle releases.
         * @param file il file da settare
         * @param addRelease true se bisogna settare il file come release, false se bisogna rimuoverlo
         */
        void onSetRelease(ManagerFile file, boolean addRelease);
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

        //Nasconde l'azione elimina nel caso l'utente non sia un un membro del gruppo
        if (!project.isMember()) {
            popup.getMenu().findItem(R.id.file_delete_action).setVisible(false);
        }

        boolean isRelease = project.getReleaseNumber(file.getName()) != 0;
        if (isRelease) {
            popup.getMenu().findItem(R.id.file_set_release_action)
                    .setTitle(R.string.text_action_remove_release_action);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.file_set_release_action) {
                    listener.onSetRelease(file, !isRelease);
                } else if (item.getItemId() == R.id.file_delete_action) {
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