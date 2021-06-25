package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.util.Log;
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
import it.uniba.di.sms2021.managerapp.enitities.file.FileComparator;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerFile;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerLocalFile;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

/**
 * Adapter di una recyclerView per una lista di file
 */
public class FilesRecyclerAdapter  extends ListAdapter<ManagerFile, RecyclerView.ViewHolder> {
    private static final String TAG = "FilesRecyclerAdapter";
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
        ImageView typeImageView = itemView.findViewById(R.id.file_type_image_view);
        TextView nameTextView = itemView.findViewById(R.id.file_name_text_view);
        TextView sizeTextView = itemView.findViewById(R.id.file_size_text_view);
        ImageView actionsImageView = itemView.findViewById(R.id.file_action_overflow_image_view);
        TextView releaseTextView = itemView.findViewById(R.id.file_release_text_view);

        nameTextView.setText(file.getName());
        sizeTextView.setText(FileUtil.getFormattedSize(context, file.getSize()));
        FileUtil.setTypeImageView(context, typeImageView, file.getType());

        if (file.getClass() == ManagerCloudFile.class) {
            ManagerCloudFile cloudFile = (ManagerCloudFile) file;
            cardView.setOnClickListener(view -> listener.onClick(cloudFile));
            actionsImageView.setOnClickListener(view -> showMenu(view, cloudFile));
        } else if (file.getClass() == ManagerLocalFile.class) {
            ManagerLocalFile localFile = (ManagerLocalFile) file;
            cardView.setOnClickListener(view -> listener.onClick(localFile));
            actionsImageView.setOnClickListener(view -> showMenu(view, localFile));
        }

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
     * Presenta le azioni effettuabile su un singolo file della lista<br><br>
     *
     * Supporta sia file provenienti da firebase (ManagerCloudFile), sia file scaricati sulla
     * memoria del dispositivo (ManagerLocalFile)
     */
    public interface OnActionListener {
        /**
         * Azione da eseguire quando l'utente clicca su un file presente sul cloud
         */
        void onClick(ManagerCloudFile file);
        /**
         * Azione da eseguire quando l'utente clicca su un file presente nello storage
         */
        void onClick(ManagerLocalFile file);

        /**
         * Setta il file come release del progetto o lo rimuove dalle releases.
         * @param file il file da settare
         * @param addRelease true se bisogna settare il file come release, false se bisogna rimuoverlo
         */
        void onSetRelease(ManagerCloudFile file, boolean addRelease);
        void onDelete (ManagerCloudFile file);
        void onDelete (ManagerLocalFile file);
        void onShare (ManagerCloudFile file);
        void onDownload (ManagerCloudFile file);
    }

    /**
     * Mostra un menù popup a partire dal bottone premuto.<br>
     * Preso e modificato da: https://material.io/components/menus/android#dropdown-menus
     */
    private void showMenu(View view, ManagerCloudFile file) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.cloud_file_action_popup_menu, popup.getMenu());

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
                    Log.e(TAG, "Id Menù " + item.getItemId() + " non trovato");
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

    /**
     * Mostra un menù popup a partire dal bottone premuto.<br>
     * Le azioni disponibili solo solo quelle di visualizzazione e di cancellazione del file locale<br>
     * Preso e modificato da: https://material.io/components/menus/android#dropdown-menus
     */
    private void showMenu (View view, ManagerLocalFile file) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.local_file_action_popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.file_delete_action) {
                    listener.onDelete(file);
                } else {
                    Log.e(TAG, "Id Menù " + item.getItemId() + " non trovato");
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